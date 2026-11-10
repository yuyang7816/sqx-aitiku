package com.sqx.app.vip.expire.service;

import com.sqx.app.base.user.domain.entity.UserEntity;

/**
 * 会员到期检查与降级（定时任务 + 请求路径复用）
 */
public interface VipExpireService {

    /**
     * 请求路径：若 vipExpireAt 已过期则降级 isVip 并推送 notice-member，返回最新用户快照
     */
    UserEntity refreshIfExpired(Long userId);

    UserEntity refreshIfExpired(UserEntity user);

    /**
     * 定时任务：批量处理所有已过期但仍标记为 VIP 的用户
     *
     * @return 成功降级用户数
     */
    int downgradeAllExpired();

    /**
     * 单用户到期降级（幂等：已非 VIP 或尚未到期则跳过）
     */
    boolean downgradeIfExpired(Long userId);
}
