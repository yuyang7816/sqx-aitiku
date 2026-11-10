package com.sqx.app.vip.renew.job;

import com.sqx.app.vip.renew.service.VipRenewNoticeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 会员续费提醒定时任务：每日扫描到期前 N 天会员并推送 notice-member
 */
@Slf4j
@Component
public class VipRenewNoticeJob {

    @Resource
    private VipRenewNoticeService vipRenewNoticeService;

    @Scheduled(cron = "0 30 9 * * ?")
    public void pushRenewNotices() {
        log.info("开始定时扫描会员续费提醒");
        int pushed = vipRenewNoticeService.pushAllDue();
        log.info("定时会员续费提醒完成，新推送 {} 人", pushed);
    }
}
