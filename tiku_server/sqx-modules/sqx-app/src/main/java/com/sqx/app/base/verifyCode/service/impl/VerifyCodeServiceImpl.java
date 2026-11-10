package com.sqx.app.base.verifyCode.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.sqx.app.base.enums.AppError;
import com.sqx.app.base.template.service.SmsTemplateService;
import com.sqx.app.base.verifyCode.constans.VerifyCodeConstant;
import com.sqx.app.base.verifyCode.db.service.IVerifyCodeService;
import com.sqx.app.base.verifyCode.domain.entity.VerifyCode;
import com.sqx.app.base.verifyCode.service.VerifyCodeService;
import com.sqx.common.core.exception.BizException;
import com.sqx.common.mail.service.IMailService;
import com.sqx.common.redis.utils.ConfigUtil;
import com.sqx.common.redis.utils.RedisUtil;
import com.sqx.common.sms.SmsService;
import com.sqx.common.sms.enums.SmsError;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 验证码 服务实现类
 *
 * @author sqx
 * @since 2025-04-22 15:31:50
 */
@Service
public class VerifyCodeServiceImpl implements VerifyCodeService {

    private static final Logger log = LoggerFactory.getLogger(VerifyCodeServiceImpl.class);

    @Autowired
    private IVerifyCodeService iVerifyCodeService;
    @Autowired
    private IMailService mailService;
    @Autowired
    private SmsService smsService;
    @Autowired
    private SmsTemplateService smsTemplateService;

    /** 开发环境 Mock 短信，跳过真实通道 */
    @Value("${sqx.sms.mock:false}")
    private boolean smsMock;

    /** Mock 固定验证码（开发联调） */
    @Value("${sqx.sms.mock-code:123456}")
    private String smsMockCode;

    @Override
    public Boolean verify(String phone, String code) {
        try {
            verifyForLogin(phone, code);
            return true;
        } catch (BizException ex) {
            if (ex.getErrorCode() == AppError.VERIFY_CODE_ERROR
                    || ex.getErrorCode() == AppError.VERIFY_CODE_EXPIRED) {
                return false;
            }
            throw ex;
        }
    }

    @Override
    public void verifyForLogin(String phone, String code) {
        String key = VerifyCodeConstant.CACHE_KEY + phone;
        if (!RedisUtil.hasKey(key)) {
            throw new BizException(AppError.VERIFY_CODE_EXPIRED);
        }

        String cacheCode = RedisUtil.getCacheObject(key);
        if (!Objects.equals(cacheCode, code)) {
            throw new BizException(AppError.VERIFY_CODE_ERROR);
        }

        RedisUtil.deleteObject(key);

        iVerifyCodeService.update(new VerifyCode(), new LambdaUpdateWrapper<VerifyCode>()
                .eq(VerifyCode::getPhone, phone)
                .eq(VerifyCode::getCode, cacheCode)
                .set(VerifyCode::getStatus, 2)
        );
    }

    @Override
    public void sendAppVerifyCode(String phone) {
        assertSendInterval(phone);

        String code = smsMock ? smsMockCode : createAppRandomCode();
        String content = resolveSmsContent(code, VerifyCodeConstant.DEFAULT_BIZ_CODE);

        if (smsMock) {
            log.info("[SMS Mock] 手机号 {} 验证码 {}", phone, code);
        } else {
            sendProductionSms(phone, content);
        }

        persistVerifyCode(phone, code);
    }

    @Override
    public VerifyCode sendPhoneCode(String phone, String bizCode) {
        String code = createRandomCode();
        String content = resolveSmsContent(code, bizCode);
        smsService.sendSms(phone, content);

        persistVerifyCode(phone, code);

        VerifyCode verifyCode = new VerifyCode();
        verifyCode.setPhone(phone);
        verifyCode.setCode(code);
        verifyCode.setStatus(1);
        verifyCode.setExpireTime(LocalDateTime.now().plusMinutes(10));
        return verifyCode;
    }

    @Override
    public VerifyCode sendMailCode(String email, String bizCode) {
        String subject = ConfigUtil.getConfigValue("sys_server_name");

        String code = createRandomCode();
        String content = resolveSmsContent(code, bizCode);
        mailService.sendSimpleMail(email, subject, content);

        persistVerifyCode(email, code);

        VerifyCode verifyCode = new VerifyCode();
        verifyCode.setPhone(email);
        verifyCode.setCode(code);
        verifyCode.setStatus(1);
        verifyCode.setExpireTime(LocalDateTime.now().plusMinutes(10));
        return verifyCode;
    }

    /**
     * 生产短信发送：网关失败映射为 350017，避免内部 450xxx 暴露给前端
     */
    private void sendProductionSms(String phone, String content) {
        try {
            smsService.sendSms(phone, content);
        } catch (BizException ex) {
            if (isSmsGatewayError(ex)) {
                log.error("[SMS] 发送失败 phone={} reason={}", phone, ex.getMessage());
                throw new BizException(AppError.VERIFY_CODE_SEND_FAILED);
            }
            throw ex;
        }
    }

    private boolean isSmsGatewayError(BizException ex) {
        if (ex.getErrorCode() == null) {
            return false;
        }
        Integer code = ex.getErrorCode().getCode();
        return code != null && code >= SmsError.PASSWORD_ERROR.getCode()
                && code <= SmsError.SMS_SEND_ERROR.getCode();
    }

    /**
     * 校验 60 秒内是否已发送（基于 verify_code 表最近一条记录）
     */
    private void assertSendInterval(String phone) {
        VerifyCode latest = iVerifyCodeService.getOne(new LambdaQueryWrapper<VerifyCode>()
                .eq(VerifyCode::getPhone, phone)
                .orderByDesc(VerifyCode::getCreateTime)
                .last("LIMIT 1"));
        if (latest == null || latest.getCreateTime() == null) {
            return;
        }
        long elapsedSeconds = Duration.between(latest.getCreateTime(), LocalDateTime.now()).getSeconds();
        if (elapsedSeconds < VerifyCodeConstant.SEND_INTERVAL_SECONDS) {
            throw new BizException(AppError.VERIFY_CODE_SEND_TOO_FREQUENT);
        }
    }

    /**
     * 写入 Redis 缓存并落库
     */
    private void persistVerifyCode(String phone, String code) {
        String cacheKey = VerifyCodeConstant.CACHE_KEY + phone;
        if (RedisUtil.hasKey(cacheKey)) {
            RedisUtil.deleteObject(cacheKey);
        }
        RedisUtil.setCacheObject(cacheKey, code, Duration.ofMinutes(10));

        VerifyCode verifyCode = new VerifyCode();
        verifyCode.setPhone(phone);
        verifyCode.setCode(code);
        verifyCode.setStatus(1);
        verifyCode.setExpireTime(LocalDateTime.now().plusMinutes(10));
        iVerifyCodeService.save(verifyCode);
    }

    /**
     * 解析短信正文：优先模板，缺失时使用默认登录文案
     */
    private String resolveSmsContent(String code, String bizCode) {
        String serverName = ConfigUtil.getConfigValue("sys_server_name");
        if (StringUtils.isBlank(serverName)) {
            serverName = "XXAI题库";
        }
        try {
            String contentTemplate = smsTemplateService.getContentByBizCode(bizCode);
            return String.format(contentTemplate, serverName, code);
        } catch (BizException ex) {
            if (ex.getErrorCode() == AppError.SMS_TEMPLATE_NOT_FOUND) {
                return String.format(VerifyCodeConstant.REGISTER_CONTENT, serverName, code);
            }
            throw ex;
        }
    }

    /** 用户端接口：4~6 位数字验证码 */
    private String createAppRandomCode() {
        int length = ThreadLocalRandom.current().nextInt(4, 7);
        int min = (int) Math.pow(10, length - 1);
        int max = (int) Math.pow(10, length) - 1;
        return String.valueOf(ThreadLocalRandom.current().nextInt(min, max + 1));
    }

    /** 管理端/邮件：6 位数字验证码 */
    private String createRandomCode() {
        int code = ThreadLocalRandom.current().nextInt(100000, 1000000);
        return String.valueOf(code);
    }
}
