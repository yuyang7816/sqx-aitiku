package com.sqx.app.base.analysis.job;

import com.sqx.app.base.analysis.service.MonthlyReportPushService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.YearMonth;

@Slf4j
@Component
public class MonthlyReportPushJob {

    @Resource
    private MonthlyReportPushService monthlyReportPushService;

    @Scheduled(cron = "0 0 8 1 * ?")
    public void pushLastMonthReports() {
        YearMonth lastMonth = YearMonth.now().minusMonths(1);
        log.info("开始定时推送 {} 月度学习报告站内消息", lastMonth);
        int pushed = monthlyReportPushService.pushForMonth(lastMonth);
        log.info("定时推送 {} 月度学习报告完成，新推送 {} 条", lastMonth, pushed);
    }
}
