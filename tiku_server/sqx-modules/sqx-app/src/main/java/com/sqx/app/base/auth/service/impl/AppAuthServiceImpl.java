package com.sqx.app.base.auth.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.sqx.app.base.auth.domain.bo.*;
import com.sqx.app.base.auth.domain.vo.AppWechatLoginVO;
import com.sqx.app.base.auth.domain.vo.LoginVO;
import com.sqx.app.base.auth.domain.vo.WxLoginVo;
import com.sqx.app.base.auth.service.AppAuthService;
import com.sqx.app.base.auth.support.LoginRulesSupport;
import com.sqx.app.base.auth.support.MpLoginConfigSupport;
import com.sqx.app.base.constants.SysConfigConstant;
import com.sqx.app.base.enums.AppError;
import com.sqx.app.base.user.db.service.IUserService;
import com.sqx.app.base.user.domain.entity.UserEntity;
import com.sqx.app.base.user.enums.UserStatus;
import com.sqx.app.base.user.enums.UserType;
import com.sqx.app.base.utils.RequestUtil;
import com.sqx.app.base.utils.SysConfigUtil;
import com.sqx.app.base.utils.wx.WxJsLoginRes;
import com.sqx.app.base.utils.wx.WxMpOAuthRes;
import com.sqx.app.base.utils.wx.WxUserPhone;
import com.sqx.app.base.utils.wx.WxUtil;
import com.sqx.app.base.verifyCode.service.VerifyCodeService;
import com.sqx.app.referral.user.db.service.IReferralUserCommissionService;
import com.sqx.app.referral.user.domain.entity.ReferralUserCommission;
import com.sqx.app.wallet.wallet.db.service.IUserWalletService;
import com.sqx.app.wallet.wallet.domain.entity.UserWallet;
import com.sqx.common.core.domain.Result;
import com.sqx.common.core.exception.BizException;
import com.sqx.common.core.utils.GenUtil;
import com.sqx.common.core.utils.Md5Utils;
import com.sqx.common.shiro.utils.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * @author chengming
 * @since 2025/4/22
 */
@Slf4j
@Service
public class AppAuthServiceImpl implements AppAuthService {

    @Resource
    private IUserService iUserService;
    @Resource
    private JwtUtil jwtUtil;
    @Resource
    private VerifyCodeService verifyCodeService;
    @Resource
    private WxUtil wxUtil;
    @Resource
    private IUserWalletService iUserWalletService;
    @Resource
    private IReferralUserCommissionService iReferralUserCommissionService;
    @Resource
    private LoginRulesSupport loginRulesSupport;

    /** 开发环境 Mock 微信登录，跳过 jscode2session（仍读取 sys_config 中小程序 AppId/Secret） */
    @Value("${sqx.wechat.mock:false}")
    private boolean wechatMock;

    /** Mock 登录固定 openId（开发联调） */
    @Value("${sqx.wechat.mock-openid:oDevMockWxLoginOpenId}")
    private String wechatMockOpenid;

    @Override
    public Result<LoginVO> phonePasswordLogin(PhonePasswordLoginBO phonePasswordLoginBO) {
        UserEntity userEntity = getUserByPhone(phonePasswordLoginBO.getPhoneNo());
        if (Objects.isNull(userEntity)) {
            throw new BizException(AppError.PHONE_NOT_REGISTER);
        }

        if (1 != userEntity.getStatus()) {
            throw new BizException(AppError.USER_BANNED);
        }

        String encryptPassword = Md5Utils.md5s(phonePasswordLoginBO.getPassword());

        if (!Objects.equals(encryptPassword, userEntity.getPassword())) {
            throw new BizException(AppError.PHONE_OR_PASSWORD_ERROR);
        }

        LoginVO vo = buildLoginResult(userEntity);
        return Result.success(vo);
    }

    @Override
    public Result<LoginVO> phoneCodeLogin(PhoneCodeLoginBO phoneCodeLoginBO) {
        return loginByPhoneAndCode(phoneCodeLoginBO.getPhoneNo(), phoneCodeLoginBO.getVerifyCode());
    }

    @Override
    public Result<LoginVO> smsLogin(AppSmsLoginBO appSmsLoginBO) {
        loginRulesSupport.validateAgreementAccepted(appSmsLoginBO.getAgreedProtocol());
        return loginByPhoneAndCode(appSmsLoginBO.getPhone(), appSmsLoginBO.getCode());
    }

    @Override
    public Result<AppWechatLoginVO> wechatLogin(AppWechatLoginBO appWechatLoginBO) {
        loginRulesSupport.validateAgreementAccepted(appWechatLoginBO.getAgreedProtocol());
        WxJsLoginRes wxJsLoginRes = fetchWechatSession(appWechatLoginBO.getCode(), resolveWechatLoginChannel(appWechatLoginBO.getChannel()));
        return loginByWechatSession(wxJsLoginRes);
    }

    @Override
    public Result<AppWechatLoginVO> mpOAuthLogin(AppWechatLoginBO appWechatLoginBO) {
        MpLoginConfigSupport.assertEnabled();
        loginRulesSupport.validateAgreementAccepted(appWechatLoginBO.getAgreedProtocol());
        WxMpOAuthRes oauthRes = fetchWechatMpSession(appWechatLoginBO.getCode());
        return loginByMpOAuthSession(oauthRes);
    }

    @Override
    public Result<AppWechatLoginVO> bindWechatPhone(Long userId, AppWechatBindPhoneBO bindPhoneBO) {
        if (userId == null) {
            throw new BizException(AppError.USER_NOT_FOUND);
        }
        UserEntity current = iUserService.getById(userId);
        if (current == null) {
            throw new BizException(AppError.USER_NOT_FOUND);
        }
        if (1 != current.getStatus()) {
            throw new BizException(AppError.USER_BANNED);
        }
        if (StringUtils.isNotBlank(current.getPhone())) {
            return Result.success(buildWechatLoginResult(current, false));
        }
        String maOpenId = StringUtils.trimToEmpty(current.getMaOpenId());
        if (StringUtils.isBlank(maOpenId)) {
            throw new BizException(AppError.WECHAT_PHONE_OPENID_MISSING);
        }

        WxUserPhone wxUserPhone;
        try {
            wxUserPhone = wxUtil.getUserPhoneNumber(bindPhoneBO.getPhoneCode(), maOpenId, RequestUtil.getClientType());
        } catch (BizException ex) {
            throw ex;
        } catch (Exception ex) {
            log.warn("微信绑定手机号失败: {}", ex.getMessage());
            throw new BizException(AppError.WECHAT_PHONE_BIND_FAILED);
        }
        String phone = normalizePhoneNumber(wxUserPhone);
        if (StringUtils.isBlank(phone)) {
            throw new BizException(AppError.WECHAT_PHONE_BIND_FAILED);
        }

        UserEntity targetUser = linkPhoneToWechatUser(current, phone);
        return Result.success(buildWechatLoginResult(targetUser, false));
    }

    /**
     * 绑定手机号：同号合并为同一用户（H5/小程序/App 短信或微信授权同手机号共用账号）
     */
    UserEntity linkPhoneToWechatUser(UserEntity current, String phone) {
        UserEntity phoneUser = getUserByPhone(phone);
        if (phoneUser != null && !Objects.equals(phoneUser.getUserId(), current.getUserId())) {
            if (StringUtils.isBlank(phoneUser.getMaOpenId()) && StringUtils.isNotBlank(current.getMaOpenId())) {
                phoneUser.setMaOpenId(current.getMaOpenId());
            }
            if (StringUtils.isBlank(phoneUser.getMpOpenId()) && StringUtils.isNotBlank(current.getMpOpenId())) {
                phoneUser.setMpOpenId(current.getMpOpenId());
            }
            iUserService.updateById(phoneUser);
            return phoneUser;
        }
        if (StringUtils.isBlank(current.getPhone())) {
            current.setPhone(phone);
            iUserService.updateById(current);
        }
        return current;
    }

    private String normalizePhoneNumber(WxUserPhone wxUserPhone) {
        if (wxUserPhone == null) {
            return "";
        }
        String phone = StringUtils.trimToEmpty(wxUserPhone.getPurePhoneNumber());
        if (StringUtils.isBlank(phone)) {
            phone = StringUtils.trimToEmpty(wxUserPhone.getPhoneNumber());
        }
        return phone.replaceAll("\\D", "");
    }

    /**
     * 解析微信登录渠道：ma 小程序 / app 移动应用
     */
    private String resolveWechatLoginChannel(String channel) {
        if (StringUtils.isBlank(channel)) {
            return "ma";
        }
        return channel.trim().toLowerCase();
    }

    /**
     * 调用微信 jscode2session（小程序或移动应用）
     */
    WxJsLoginRes fetchWechatSession(String code, String channel) {
        if (isMockWechatCode(code)) {
            if (!wechatMock) {
                throw new BizException(AppError.WECHAT_SIMULATOR_CODE);
            }
            String appId = "app".equals(channel)
                    ? requireWechatConfig(SysConfigConstant.Sys.SYS_USER_APP_APPID)
                    : requireWechatConfig(SysConfigConstant.Sys.SYS_USER_MA_APPID);
            return buildMockWechatSession(appId);
        }

        String appId;
        String secret;
        if ("app".equals(channel)) {
            appId = requireWechatConfig(SysConfigConstant.Sys.SYS_USER_APP_APPID);
            secret = requireWechatConfig(SysConfigConstant.Sys.SYS_USER_APP_SECRET);
        } else {
            appId = requireWechatConfig(SysConfigConstant.Sys.SYS_USER_MA_APPID);
            secret = requireWechatConfig(SysConfigConstant.Sys.SYS_USER_MA_SECRET);
        }
        return wxUtil.wxJsLogin(appId, secret, code);
    }

    private boolean isMockWechatCode(String code) {
        if (StringUtils.isBlank(code)) {
            return false;
        }
        String trimmed = code.trim();
        return "the code is a mock one".equals(trimmed)
                || "e2e-wechat-mock-code".equals(trimmed);
    }

    private WxJsLoginRes buildMockWechatSession(String appId) {
        WxJsLoginRes wxJsLoginRes = new WxJsLoginRes();
        wxJsLoginRes.setOpenid(StringUtils.defaultIfBlank(wechatMockOpenid, "oDevMockWxLoginOpenId"));
        wxJsLoginRes.setSessionKey("mock-session-key-" + appId);
        return wxJsLoginRes;
    }

    /**
     * 调用微信 jscode2session（默认小程序，兼容旧调用）
     */
    WxJsLoginRes fetchWechatSession(String code) {
        return fetchWechatSession(code, "ma");
    }

    WxMpOAuthRes fetchWechatMpSession(String code) {
        String appId = requireWechatConfig(SysConfigConstant.Sys.SYS_USER_MP_APPID);
        String secret = requireWechatConfig(SysConfigConstant.Sys.SYS_USER_MP_SECRET);
        return wxUtil.wxMpOAuth2(appId, secret, code);
    }

    Result<AppWechatLoginVO> loginByMpOAuthSession(WxMpOAuthRes oauthRes) {
        UserEntity userEntity = iUserService.getByMpOpenId(oauthRes.getOpenid());
        boolean firstLogin = false;
        if (Objects.isNull(userEntity)) {
            userEntity = registerWechatMpUser(oauthRes.getOpenid());
            firstLogin = true;
        }

        if (1 != userEntity.getStatus()) {
            throw new BizException(AppError.USER_BANNED);
        }

        AppWechatLoginVO vo = buildWechatLoginResult(userEntity, firstLogin);
        return Result.success(vo);
    }

    private UserEntity registerWechatMpUser(String mpOpenId) {
        UserEntity userInfo = new UserEntity();
        userInfo.setUserName("微信用户");
        userInfo.setMpOpenId(mpOpenId);
        userInfo.setStatus(UserStatus.ENABLE.getCode());
        userInfo.setPlatform("MP");
        userInfo.setUserType(UserType.USER.getCode());
        userInfo.setIsVip(0);
        userInfo.setInvitationCode(GenUtil.genInviteCode(buildInviteSeed(mpOpenId)));
        userInfo.setCreateTime(LocalDateTime.now());

        iUserService.save(userInfo);
        registerBiz(userInfo.getUserId());
        return userInfo;
    }

    /**
     * 读取微信小程序配置；缺失或为空时返回友好业务错误（生产部署须在管理端填写 AppId/Secret）
     */
    String requireWechatConfig(String paramKey) {
        try {
            String value = SysConfigUtil.getConfigValue(paramKey);
            if (StringUtils.isBlank(value)) {
                throw new BizException(AppError.WECHAT_NOT_CONFIGURED);
            }
            return value;
        } catch (BizException ex) {
            if (ex.getErrorCode() == AppError.CONFIG_NOT_FOUND) {
                throw new BizException(AppError.WECHAT_NOT_CONFIGURED);
            }
            throw ex;
        }
    }

    /**
     * 根据微信会话绑定或创建用户并签发 JWT
     */
    Result<AppWechatLoginVO> loginByWechatSession(WxJsLoginRes wxJsLoginRes) {
        UserEntity userEntity = iUserService.getByOpenId(wxJsLoginRes.getOpenid());
        boolean firstLogin = false;
        if (Objects.isNull(userEntity)) {
            userEntity = registerWechatUser(wxJsLoginRes.getOpenid());
            firstLogin = true;
        }

        if (1 != userEntity.getStatus()) {
            throw new BizException(AppError.USER_BANNED);
        }

        AppWechatLoginVO vo = buildWechatLoginResult(userEntity, firstLogin);
        return Result.success(vo);
    }

    /**
     * 微信小程序 openId 注册（无手机号，首次登录 profile.ready=false）
     */
    private UserEntity registerWechatUser(String openId) {
        UserEntity userInfo = new UserEntity();
        userInfo.setUserName("微信用户");
        userInfo.setMaOpenId(openId);
        userInfo.setStatus(UserStatus.ENABLE.getCode());
        userInfo.setPlatform("MA");
        userInfo.setUserType(UserType.USER.getCode());
        userInfo.setIsVip(0);
        userInfo.setInvitationCode(GenUtil.genInviteCode(buildInviteSeed(openId)));
        userInfo.setCreateTime(LocalDateTime.now());

        iUserService.save(userInfo);
        registerBiz(userInfo.getUserId());
        return userInfo;
    }

    /**
     * 无手机号时用 openId 哈希生成邀请码种子（满足 genInviteCode 至少 5 位数字）
     */
    private String buildInviteSeed(String openId) {
        long seed = Math.abs((long) openId.hashCode());
        return "1" + String.format("%010d", seed % 10_000_000_000L);
    }

    private AppWechatLoginVO buildWechatLoginResult(UserEntity userEntity, boolean firstLogin) {
        LoginVO loginVO = buildLoginResult(userEntity);

        AppWechatLoginVO vo = new AppWechatLoginVO();
        vo.setToken(loginVO.getToken());
        vo.setExpire(loginVO.getExpire());
        vo.setUser(loginVO.getUser());
        vo.setFirstLogin(firstLogin);
        vo.setProfile(loginVO.getProfile());
        vo.setNeedBindPhone(StringUtils.isBlank(userEntity.getPhone()));

        return vo;
    }

    /**
     * 手机号 + 验证码登录（未注册自动注册）
     */
    private Result<LoginVO> loginByPhoneAndCode(String phone, String code) {
        verifyCodeService.verifyForLogin(phone, code);

        String normalizedPhone = StringUtils.trimToEmpty(phone);
        UserEntity userEntity = getUserByPhone(normalizedPhone);
        if (Objects.isNull(userEntity)) {
            UserEntity userInfo = registerPhone(normalizedPhone, null, null, null, null, null);
            LoginVO vo = buildLoginResult(userInfo);
            return Result.success(vo);
        }

        if (1 != userEntity.getStatus()) {
            throw new BizException(AppError.USER_BANNED);
        }

        LoginVO vo = buildLoginResult(userEntity);
        return Result.success(vo);
    }

    @Override
    public Result<LoginVO> phoneRegister(PhoneRegisterBO phoneRegisterBO) {
        verifyCodeService.verifyForLogin(phoneRegisterBO.getPhoneNo(), phoneRegisterBO.getCode());

        UserEntity userInfo = registerPhone(phoneRegisterBO.getPhoneNo(), phoneRegisterBO.getPassword(), phoneRegisterBO.getUsername(), phoneRegisterBO.getSysPhone(), phoneRegisterBO.getPlatform(), phoneRegisterBO.getSuperiorInvitationCode());

        LoginVO vo = buildLoginResult(userInfo);
        return Result.success(vo);
    }

    @Override
    public Result<Void> resetPassword(ResetPasswordBO resetPasswordBO) {
        verifyCodeService.verifyForLogin(resetPasswordBO.getPhone(), resetPasswordBO.getCode());

        if (!checkPhoneExist(resetPasswordBO.getPhone())) {
            throw new BizException(AppError.PHONE_NOT_REGISTER);
        }

        UpdateWrapper<UserEntity> updateWrapper = new UpdateWrapper<>();
        updateWrapper.lambda()
                .eq(UserEntity::getPhone, resetPasswordBO.getPhone())
                .set(UserEntity::getPassword, Md5Utils.md5s(resetPasswordBO.getPassword()));

        iUserService.update(new UserEntity(), updateWrapper);

        return Result.success();
    }

    private boolean checkPhoneExist(String phone) {
        QueryWrapper<UserEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(UserEntity::getPhone, phone);
        return iUserService.exists(queryWrapper);
    }

    /**
     * 手机号注册
     * @param phoneNo
     * @param sysPhone
     * @param platform
     * @param superiorInvitationCode
     * @return
     */
    private UserEntity registerPhone(String phoneNo, String password, String username, Integer sysPhone, String platform, String superiorInvitationCode) {
        UserEntity userEntity = getUserByPhone(phoneNo);
        if (Objects.nonNull(userEntity)) {
            throw new BizException(AppError.PHONE_REGISTERED);
        }

        UserEntity userInfo = new UserEntity();
        userInfo.setUserName(StringUtils.isNotBlank(username) ? username : phoneNo);
        userInfo.setPhone(phoneNo);
        userInfo.setSysPhone(sysPhone);
        userInfo.setStatus(UserStatus.ENABLE.getCode());
        userInfo.setPlatform(platform);
        userInfo.setUserType(UserType.USER.getCode());
        userInfo.setIsVip(0);
        userInfo.setInvitationCode(GenUtil.genInviteCode(phoneNo));
        userInfo.setSuperiorInvitationCode(superiorInvitationCode);

        if (StringUtils.isNotBlank(password)) {
            String encryptPassword = Md5Utils.md5s(password);
            userInfo.setPassword(encryptPassword);
        }

        iUserService.save(userInfo);

        /* 其他业务 */
        registerBiz(userInfo.getUserId());

        return userInfo;
    }

    private void registerBiz(Long userId) {
        UserWallet userWallet = new UserWallet();
        userWallet.setUserId(userId);
        userWallet.setAmount(BigDecimal.ZERO);
        iUserWalletService.save(userWallet);

        ReferralUserCommission referralUserCommission = new ReferralUserCommission();
        referralUserCommission.setUserId(userId);
        referralUserCommission.setCommission(BigDecimal.ZERO);
        iReferralUserCommissionService.save(referralUserCommission);
    }

    @Override
    public Result<WxLoginVo> wxLogin(String code) {
        String appId = SysConfigUtil.getConfigValue(SysConfigConstant.Sys.SYS_USER_MA_APPID);
        String secret = SysConfigUtil.getConfigValue(SysConfigConstant.Sys.SYS_USER_MA_SECRET);

        WxJsLoginRes wxJsLoginRes = wxUtil.wxJsLogin(appId, secret, code);

        WxLoginVo vo = new WxLoginVo();
        vo.setSessionKey(wxJsLoginRes.getSessionKey());
        vo.setUnionid(wxJsLoginRes.getUnionid());
        vo.setErrmsg(wxJsLoginRes.getErrmsg());
        vo.setOpenid(wxJsLoginRes.getOpenid());
        vo.setErrmsg(wxJsLoginRes.getErrmsg());

        // 是否绑定手机号
        UserEntity userEntity = iUserService.getByOpenId(wxJsLoginRes.getOpenid());
        if (Objects.isNull(userEntity) || StringUtils.isBlank(userEntity.getPhone())) {
            vo.setBindPhone(false);
        } else {
            LoginVO loginVO = buildLoginResult(userEntity);
            vo.setLoginVO(loginVO);
            vo.setUserEntity(userEntity);
            vo.setBindPhone(true);
        }

        return Result.success(vo);
    }

    @Override
    public Result<LoginVO> wxJsRegister(WxJsRegisterBo wxJsRegisterBo) {
        UserEntity userEntity = iUserService.getByOpenId(wxJsRegisterBo.getOpenId());
        if (Objects.nonNull(userEntity)) {
            if (StringUtils.isBlank(userEntity.getPhone())) {
                userEntity.setPhone(wxJsRegisterBo.getPhone());
                iUserService.updateById(userEntity);
            }

            // 已注册过, 直接登录
            LoginVO vo = buildLoginResult(userEntity);
            return Result.success(vo);
        }

        UserEntity userByPhone = getUserByPhone(wxJsRegisterBo.getPhone());
        if (Objects.nonNull(userByPhone)) {

            if (StringUtils.isBlank(userByPhone.getMaOpenId())) {
                userByPhone.setMaOpenId(wxJsRegisterBo.getOpenId());
                iUserService.updateById(userByPhone);
            }

            // 已注册过, 直接登录
            LoginVO vo = buildLoginResult(userByPhone);
            return Result.success(vo);
        }

        UserEntity userInfo = new UserEntity();
        userInfo.setUserName(wxJsRegisterBo.getUsername());
        userInfo.setAvatar(wxJsRegisterBo.getAvatar());
        userInfo.setPhone(wxJsRegisterBo.getPhone());
        userInfo.setStatus(UserStatus.ENABLE.getCode());
        userInfo.setPlatform("JS");
        userInfo.setUserType(UserType.USER.getCode());
        userInfo.setIsVip(0);
        userInfo.setInvitationCode(GenUtil.genInviteCode(wxJsRegisterBo.getPhone()));
        userInfo.setSuperiorInvitationCode(wxJsRegisterBo.getSuperiorInvitationCode());
        userInfo.setMaOpenId(wxJsRegisterBo.getOpenId());
        userInfo.setCreateTime(LocalDateTime.now());

        iUserService.save(userInfo);

        /* 其他业务 */
        registerBiz(userInfo.getUserId());

        LoginVO vo = buildLoginResult(userInfo);
        return Result.success(vo);
    }

    @Override
    public Result<Object> decryptPhone(DecryptPhoneBo decryptPhoneBo) {
        String decryptS5 = wxUtil.decryptS5(decryptPhoneBo.getDecryptData(), decryptPhoneBo.getKey(), decryptPhoneBo.getIv());
        return Result.success(JSONObject.parseObject(decryptS5));
    }

    @Override
    public WxUserPhone getWxUserPhoneNumber(String code, String openId) {
        WxUserPhone wxUserPhone = wxUtil.getUserPhoneNumber(code, openId, RequestUtil.getClientType());

        return wxUserPhone;
    }

    /**
     * 构建登录返回值
     * @param userEntity 用户信息
     * @return
     */
    private LoginVO buildLoginResult(UserEntity userEntity) {
        LoginVO vo = new LoginVO();

        //生成token
        String token = jwtUtil.generateToken(userEntity.getUserId());
        vo.setToken(token);
        vo.setExpire(jwtUtil.getExpire());
        userEntity.setPassword(null);
        vo.setUser(userEntity);
        vo.setProfile(loginRulesSupport.buildProfileState(userEntity));
        return vo;

    }

    /**
     * 手机号获取用户信息
     * @param phoneNo 手机号
     * @return
     */
    private UserEntity getUserByPhone(String phoneNo) {
        if (StringUtils.isBlank(phoneNo)) {
            return null;
        }
        QueryWrapper<UserEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(UserEntity::getPhone, StringUtils.trimToEmpty(phoneNo));

        return iUserService.getOne(queryWrapper);
    }
}
