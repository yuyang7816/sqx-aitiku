package com.sqx.app.base.user.delete.support;

import com.sqx.app.base.user.domain.entity.UserEntity;
import com.sqx.app.base.user.enums.UserStatus;
import org.apache.commons.lang3.StringUtils;

/**
 * 已注销账号判定（手机号前缀 deleted_ 且状态禁用）
 */
public final class UserDeletedSupport {

    public static final String DELETED_PHONE_PREFIX = "deleted_";

    private UserDeletedSupport() {
    }

    public static boolean isDeleted(UserEntity userEntity) {
        if (userEntity == null) {
            return false;
        }
        return UserStatus.DISABLE.getCode().equals(userEntity.getStatus())
                && StringUtils.startsWith(userEntity.getPhone(), DELETED_PHONE_PREFIX);
    }

    public static String buildDeletedPhone(Long userId) {
        return DELETED_PHONE_PREFIX + userId + "_" + System.currentTimeMillis();
    }
}
