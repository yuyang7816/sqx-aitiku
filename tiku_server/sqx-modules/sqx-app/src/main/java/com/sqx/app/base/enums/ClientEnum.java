package com.sqx.app.base.enums;

import com.sqx.common.core.exception.BizException;
import lombok.extern.slf4j.Slf4j;

/**
 * @author chengming
 * @since 2025/12/19
 * <p>
 * 客户端枚举
 */
@Slf4j
public enum ClientEnum {
    USER("1", "用户端", "sys_h5_domain"),
    ;

    private final String code;
    private final String desc;
    private final String domain;

    ClientEnum(String code, String desc, String domain) {
        this.code = code;
        this.desc = desc;
        this.domain = domain;
    }

    public static ClientEnum fromCode(String code) {
        for (ClientEnum c : ClientEnum.values()) {
            if (c.code.equals(code)) {
                return c;
            }
        }

        log.error("不支持的客户端: {}", code);
        throw new BizException("不支持的客户端");
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public String getDomain() {
        return domain;
    }
}
