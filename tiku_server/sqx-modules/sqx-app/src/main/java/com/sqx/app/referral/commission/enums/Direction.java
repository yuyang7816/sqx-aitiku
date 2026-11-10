package com.sqx.app.referral.commission.enums;

/**
 * @author chengming
 * @since 2026/2/6
 */
public enum Direction {

    INCR(1),
    DECR(2);

    private Integer value;

    Direction(Integer value) {
        this.value = value;
    }

    public Integer getValue() {
        return value;
    }
}
