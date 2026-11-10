package com.sqx.app.base.subscribe.job;

import com.sqx.app.base.subscribe.service.WrongReviewPushService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
public class WrongReviewPushJob {

    @Resource
    private WrongReviewPushService wrongReviewPushService;

    @Scheduled(cron = "0 0 9 * * ?")
    public void pushDailyWrongReviewReminders() {
        log.info("开始定时推送待复习错题提醒");
        int pushed = wrongReviewPushService.pushAllDue();
        log.info("定时推送待复习错题提醒完成，新推送 {} 条", pushed);
    }
}
