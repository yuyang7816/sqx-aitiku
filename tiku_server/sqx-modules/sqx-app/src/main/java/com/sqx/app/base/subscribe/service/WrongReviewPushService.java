package com.sqx.app.base.subscribe.service;

/**
 * 待复习错题提醒推送（站内信 + 微信订阅消息）
 */
public interface WrongReviewPushService {

    /**
     * 扫描全部有待复习错题的用户并推送
     *
     * @return 新推送用户数
     */
    int pushAllDue();

    /**
     * 向指定用户推送待复习错题提醒
     */
    boolean pushToUser(Long userId);
}
