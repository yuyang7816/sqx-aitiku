package com.sqx.app.vip.pay.enums;

import com.sqx.common.core.enums.IErrorCode;

/**
 * 会员支付业务错误码
 */
public enum PayError implements IErrorCode {

    PLAN_NOT_FOUND(1050015, "会员套餐不存在或已下架"),
    PAY_METHOD_UNSUPPORTED(1050016, "暂不支持该支付方式"),
    WECHAT_OPEN_ID_MISSING(1050017, "未绑定微信，请使用微信登录后再支付"),
    PAY_CREATE_FAILED(1050018, "创建支付订单失败，请稍后重试"),
    ORDER_NOT_FOUND(1050019, "支付订单不存在"),
    ;

    private final Integer code;
    private final String message;

    PayError(Integer code, String message) {
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
