package com.sqx.app.wallet.wallet.enums;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * @author chengming
 * @since 2026/1/16
 */
public enum Operator {
    /**
     * 增加
     */
    INCR("incr"),

    /**
     * 减少
     */
    DECR("decr")
    ;

    private final String value;

    Operator(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public static Operator fromValue(String value) {
        for (Operator operator : Operator.values()) {
            if (operator.getValue().equals(value)) {
                return operator;
            }
        }

        return null;
    }
}
