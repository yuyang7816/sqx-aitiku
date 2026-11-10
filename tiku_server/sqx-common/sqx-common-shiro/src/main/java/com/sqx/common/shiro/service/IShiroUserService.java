package com.sqx.common.shiro.service;

import com.sqx.common.shiro.model.AuthUser;

/**
 * @author chengming
 * @since 2025/12/19
 */
public interface IShiroUserService {

    /**
     * 获取权限用户信息
     * @param userId 用户ID
     */
    AuthUser loadByUserId(Long userId);
}
