package com.sqx.common.shiro.service;


import java.util.Set;

/**
 * @author chengming
 * @since 2024/7/11
 */
public interface ShiroService {
    /**
     * 获取用户权限列表
     */
    Set<String> getUserPermissions(long userId);
}
