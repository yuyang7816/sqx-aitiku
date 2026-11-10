package com.sqx.app.base.user.badge.service;

import com.sqx.app.base.user.badge.domain.vo.AppUserBadgeListVO;

/**
 * 用户端勋章列表（基于练习记录里程碑判定解锁）
 */
public interface UserBadgeService {

    /**
     * 返回当前用户全部勋章及解锁状态
     */
    AppUserBadgeListVO listBadges(Long userId);
}
