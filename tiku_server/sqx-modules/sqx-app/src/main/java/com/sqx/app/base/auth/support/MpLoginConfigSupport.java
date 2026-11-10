package com.sqx.app.base.auth.support;

import com.sqx.app.base.constants.SysConfigConstant;
import com.sqx.app.base.enums.AppError;
import com.sqx.app.base.utils.SysConfigUtil;
import com.sqx.common.core.exception.BizException;
import org.apache.commons.lang3.StringUtils;

/**
 * 公众号 OAuth 登录开关（sys_config.sys_user_mp_login_enabled，默认关闭）
 */
public final class MpLoginConfigSupport {

    private MpLoginConfigSupport() {
    }

    public static boolean isEnabled() {
        try {
            String value = SysConfigUtil.getConfigValue(SysConfigConstant.Sys.SYS_USER_MP_LOGIN_ENABLED);
            return parseEnabled(value);
        } catch (BizException ex) {
            if (ex.getErrorCode() == AppError.CONFIG_NOT_FOUND) {
                return false;
            }
            throw ex;
        }
    }

    public static void assertEnabled() {
        if (!isEnabled()) {
            throw new BizException(AppError.WECHAT_MP_LOGIN_DISABLED);
        }
    }

    static boolean parseEnabled(String raw) {
        if (StringUtils.isBlank(raw)) {
            return false;
        }
        String value = raw.trim().toLowerCase();
        return "1".equals(value) || "true".equals(value) || "yes".equals(value) || "on".equals(value);
    }
}
