package com.sqx.app.base.pay.enums;

import com.sqx.common.core.exception.BizException;

import java.util.Objects;

/**
 * @author chengming
 * @since 2025/4/21
 * 支付业务类型
 */
public enum BusinessType {
    VIP("vip", "会员")
    ;

    private final String code;
    private final String desc;

    BusinessType(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static BusinessType fromCode(String code) {
        for (BusinessType businessType : values()) {
            if (Objects.equals(businessType.getCode(), code)) {
                return businessType;
            }
        }

        throw new BizException("不支持的业务类型");
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
