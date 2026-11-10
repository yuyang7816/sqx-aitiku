package com.sqx.app.vip.renew.service;

/**
 * 会员续费提醒：到期前 N 天推送 notice-member 站内消息
 */
public interface VipRenewNoticeService {

    /**
     * 扫描即将到期会员并推送续费提醒
     *
     * @return 新推送人数
     */
    int pushAllDue();

    /**
     * 为指定用户推送续费提醒（若仍在提醒窗口内且未重复推送）
     *
     * @param userId 用户 ID
     * @return 是否成功推送
     */
    boolean pushToUser(Long userId);
}
