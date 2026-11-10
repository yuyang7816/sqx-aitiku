package com.sqx.app.base.user.delete.service;

/**
 * 用户端账号注销（POST /app/user/delete）
 */
public interface UserAccountDeletionService {

    /**
     * 合规注销：清除用户业务数据并匿名化 tb_user，注销后不可再登录
     */
    void deleteAccount(Long userId);
}
