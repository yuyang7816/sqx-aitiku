package com.sqx.common.shiro.service;

import java.util.Set;

/**
 * 权限缓存失效时的 DB 回退加载器
 */
public interface UserPermissionResolver {

    Set<String> resolvePermissions(long userId);
}
