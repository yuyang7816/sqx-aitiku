package com.sqx.app.vip.expire.job;

import com.sqx.app.vip.expire.service.VipExpireService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 会员到期定时降级：每日扫描 vipExpireAt 已过期用户
 */
@Slf4j
@Component
public class VipExpireDowngradeJob {

    @Resource
    private VipExpireService vipExpireService;

    @Scheduled(cron = "0 0 1 * * ?")
    public void downgradeExpiredMembers() {
        log.info("开始定时扫描会员到期降级");
        int downgraded = vipExpireService.downgradeAllExpired();
        log.info("定时会员到期降级完成，处理 {} 人", downgraded);
    }
}
