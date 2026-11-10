package com.sqx.app.base.user.delete.domain.enums;

import org.apache.commons.lang3.StringUtils;

/**
 * 账号注销申请状态
 */
public enum DeletionRequestStatus {

    PENDING("pending", "冷静期中"),
    CANCELLED("cancelled", "用户已撤销"),
    ADMIN_CANCELLED("admin_cancelled", "管理员已撤销"),
    COMPLETED("completed", "已执行注销"),
    ;

    private final String code;
    private final String label;

    DeletionRequestStatus(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public static DeletionRequestStatus fromCode(String code) {
        if (StringUtils.isBlank(code)) {
            return null;
        }
        for (DeletionRequestStatus status : values()) {
            if (status.code.equalsIgnoreCase(code.trim())) {
                return status;
            }
        }
        return null;
    }
}
