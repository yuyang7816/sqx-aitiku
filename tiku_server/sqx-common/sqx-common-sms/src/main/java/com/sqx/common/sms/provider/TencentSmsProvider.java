package com.sqx.common.sms.provider;

import com.github.qcloudsms.SmsSingleSender;
import com.github.qcloudsms.SmsSingleSenderResult;
import com.github.qcloudsms.httpclient.HTTPException;
import com.sqx.common.core.exception.BizException;
import com.sqx.common.redis.utils.ConfigUtil;
import com.sqx.common.sms.enums.SmsError;
import com.sqx.common.sms.enums.SmsProviderEnum;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * @author chengming
 * @since 2025/6/11
 */
@Slf4j
public class TencentSmsProvider implements ISmsProvider {

    @Override
    public String getProviderName() {
        return SmsProviderEnum.TENCENT.getDesc();
    }

    @Override
    public void sendSms(String phone, String content) {
        SmsSingleSenderResult result;
        try {
            String clientId = ConfigUtil.getConfigValue("31");
            String clientSecret = ConfigUtil.getConfigValue("32");

            /*
             * 发送短信验证码的状态、
             *
             * 在h5登录环境中 传的状态不是以下三种状态
             */
            SmsSingleSender ssender = new SmsSingleSender(Integer.parseInt(clientId), clientSecret);
            result = ssender.send(0, "86", phone, content, "", "");

            log.info("腾讯短信平台返回结果: {}", result);
            if (0 == result.result) {
                log.info("短信发送成功");
                return;
            }

            throw new BizException(SmsError.SMS_SEND_ERROR);
        } catch (HTTPException | IOException e) {
            // HTTP 响应码错误
            e.printStackTrace();
            throw new BizException(SmsError.SMS_SEND_ERROR);
        }
    }
}
