package com.sqx.app.base.user.delete.job;

import com.sqx.app.base.user.delete.service.UserAccountDeletionRequestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 冷静期到期后自动执行账号注销
 */
@Slf4j
@Component
public class UserAccountDeletionJob {

    @Resource
    private UserAccountDeletionRequestService userAccountDeletionRequestService;

    @Scheduled(cron = "0 10 2 * * ?")
    public void executeDueDeletions() {
        log.info("开始扫描到期账号注销申请");
        int executed = userAccountDeletionRequestService.executeDueDeletions();
        log.info("到期账号注销处理完成，执行 {} 条", executed);
    }
}
