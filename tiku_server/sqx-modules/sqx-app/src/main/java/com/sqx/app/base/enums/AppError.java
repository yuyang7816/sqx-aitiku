package com.sqx.app.base.enums;

import com.sqx.common.core.enums.IErrorCode;

/**
 * @author chengming
 * @since 2025/5/14
 *
 * app 异常
 */
public enum AppError implements IErrorCode {
    PHONE_NOT_REGISTER(350001, "手机号未注册"),
    USER_BANNED(350002, "账号已被封禁，无法登录"),
    PHONE_OR_PASSWORD_ERROR(350003, "手机号或密码错误"),
    VERIFY_CODE_ERROR(350004, "验证码不正确"),
    PHONE_REGISTERED(350005, "手机号已注册"),
    USER_NOT_FOUND(350006, "用户信息不存在"),
    UNSUPPORTED_CODE(350007, "不支持的业务代码"),
    CONFIG_NOT_FOUND(350013, "系统配置不存在"),
    INFO_NOT_FOUND(350008, "信息不存在"),
    SMS_TEMPLATE_NOT_FOUND(350009, "信息不存在"),
    VERIFY_CODE_SEND_TOO_FREQUENT(350010, "发送过于频繁，请60秒后再试"),
    VERIFY_CODE_EXPIRED(350011, "验证码已过期，请重新获取"),
    WECHAT_CODE_INVALID(350012, "微信登录凭证无效或已过期"),
    WECHAT_SIMULATOR_CODE(350022, "开发者工具模拟器无法完成正式微信登录，请使用真机调试或手机预览"),
    VERIFY_CODE_SEND_FAILED(350017, "验证码发送失败，请稍后重试"),
    WECHAT_NOT_CONFIGURED(350018, "微信登录暂未开通，请联系管理员"),
    WECHAT_MP_LOGIN_DISABLED(350023, "公众号登录未开启"),
    WECHAT_PHONE_BIND_FAILED(350024, "获取手机号失败，请重新授权"),
    WECHAT_PHONE_OPENID_MISSING(350025, "请先完成微信登录后再绑定手机号"),
    AGREEMENT_NOT_ACCEPTED(350019, "请先同意协议"),
    USER_ACCOUNT_DELETED(350014, "账号已注销"),
    USER_DELETE_REQUEST_NOT_FOUND(350020, "未找到进行中的注销申请"),
    USER_DELETE_REQUEST_NOT_PENDING(350021, "注销申请不在冷静期中"),
    SUBSCRIBE_TEMPLATE_NOT_CONFIGURED(350015, "微信订阅消息模板未配置"),
    SUBSCRIBE_OPENID_MISSING(350016, "当前账号未绑定微信小程序，无法发送订阅消息"),

    ;

    private final Integer code;
    private final String message;

    AppError(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public Integer getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
