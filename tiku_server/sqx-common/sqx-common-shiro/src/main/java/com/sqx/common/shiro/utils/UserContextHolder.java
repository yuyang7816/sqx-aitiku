package com.sqx.common.shiro.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

/**
 * @author chengming
 * @since 2025/6/20
 */
@Slf4j
public class UserContextHolder {

    /**
     * 获取app用户id
     * @return app用户id
     */
    public static Long getAppUserId() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (null == attributes) {
            log.error("用户未登录");
            return null;
        }

        Object attrValue = attributes.getAttribute("userId", RequestAttributes.SCOPE_REQUEST);
        if (null == attrValue) {
            log.error("用户未登录");
            return null;
        }

        return Long.parseLong(attrValue.toString());
    }
}
