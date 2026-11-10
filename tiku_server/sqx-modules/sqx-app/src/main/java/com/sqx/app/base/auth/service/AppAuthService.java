package com.sqx.app.base.auth.service;

import com.sqx.app.base.auth.domain.bo.*;
import com.sqx.app.base.auth.domain.vo.AppWechatLoginVO;
import com.sqx.app.base.auth.domain.vo.LoginVO;
import com.sqx.app.base.auth.domain.vo.WxLoginVo;
import com.sqx.app.base.utils.wx.WxUserPhone;
import com.sqx.common.core.domain.Result;

/**
 * @author chengming
 * @since 2025/4/22
 */
public interface AppAuthService {
    Result<LoginVO> phonePasswordLogin(PhonePasswordLoginBO phonePasswordLoginBO);

    Result<LoginVO> phoneCodeLogin(PhoneCodeLoginBO phoneCodeLoginBO);

    /**
     * 用户端短信验证码登录（POST /app/auth/sms）
     */
    Result<LoginVO> smsLogin(AppSmsLoginBO appSmsLoginBO);

    /**
     * 用户端微信 OAuth 登录（POST /app/auth/wechat）
     */
    Result<AppWechatLoginVO> wechatLogin(AppWechatLoginBO appWechatLoginBO);

    /**
     * 用户端微信公众号 OAuth 登录（POST /app/auth/mp，H5 微信内置浏览器）
     */
    Result<AppWechatLoginVO> mpOAuthLogin(AppWechatLoginBO appWechatLoginBO);

    /**
     * 小程序登录后绑定手机号（微信 getuserphonenumber 官方 API）
     */
    Result<AppWechatLoginVO> bindWechatPhone(Long userId, AppWechatBindPhoneBO bindPhoneBO);

    Result<LoginVO> phoneRegister(PhoneRegisterBO phoneRegisterBO);

    Result<Void> resetPassword(ResetPasswordBO resetPasswordBO);


    Result<WxLoginVo> wxLogin(String code);

    Result<LoginVO> wxJsRegister(WxJsRegisterBo wxJsRegisterBo);

    Result<Object> decryptPhone(DecryptPhoneBo decryptPhoneBo);

    WxUserPhone getWxUserPhoneNumber(String code, String openId);
}
