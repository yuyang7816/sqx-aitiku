package com.sqx.app.base.analysis.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sqx.app.base.analysis.domain.vo.AppAnalysisReportVO;
import com.sqx.app.base.analysis.service.AnalysisReportService;
import com.sqx.app.base.analysis.service.MonthlyReportPushService;
import com.sqx.app.base.messages.db.service.IMessagesService;
import com.sqx.app.base.messages.domain.entity.Messages;
import com.sqx.app.base.notification.core.enums.NotificationChannel;
import com.sqx.app.base.notification.core.model.NotificationRequest;
import com.sqx.app.base.notification.core.service.INotificationService;
import com.sqx.app.base.subscribe.service.SubscribePushService;
import com.sqx.system.db.entity.UserPracticeRecord;
import com.sqx.system.db.service.IUserPracticeRecordService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MonthlyReportPushServiceImpl implements MonthlyReportPushService {

    public static final String NOTICE_REPORT_URL = "notice-report";
    private static final int MESSAGE_TYPE_USER = 2;
    private static final DateTimeFormatter PUSH_TITLE_MONTH_FORMAT =
            DateTimeFormatter.ofPattern("yyyy年M月", Locale.CHINA);

    @Resource
    private AnalysisReportService analysisReportService;
    @Resource
    private INotificationService notificationService;
    @Resource
    private IMessagesService messagesService;
    @Resource
    private IUserPracticeRecordService userPracticeRecordService;
    @Resource
    private SubscribePushService subscribePushService;

    @Override
    public int pushForMonth(YearMonth month) {
        if (month == null) {
            return 0;
        }
        List<Long> userIds = listActiveUserIds(month);
        int pushed = 0;
        for (Long userId : userIds) {
            try {
                if (pushToUser(userId, month)) {
                    pushed++;
                }
            } catch (Exception ex) {
                log.error("推送用户 {} 的 {} 月报失败: {}", userId, month, ex.getMessage(), ex);
            }
        }
        return pushed;
    }

    @Override
    public boolean pushToUser(Long userId, YearMonth month) {
        if (userId == null || month == null) {
            return false;
        }
        String title = buildPushTitle(month);
        if (alreadyPushed(userId, title)) {
            log.debug("用户 {} 的 {} 月报通知已存在，跳过", userId, month);
            return false;
        }
        AppAnalysisReportVO report = analysisReportService.getReport(userId, month);
        NotificationRequest request = new NotificationRequest();
        request.setUserId(userId);
        request.setTitle(title);
        request.setContent(buildPushContent(report));
        request.setUrl(NOTICE_REPORT_URL);
        request.setType(MESSAGE_TYPE_USER);
        notificationService.send(request, NotificationChannel.INSTATION.code());
        subscribePushService.sendMonthlyReport(userId, month, title, request.getContent());
        log.info("已推送用户 {} 的 {} 月报通知", userId, month);
        return true;
    }

    List<Long> listActiveUserIds(YearMonth month) {
        LambdaQueryWrapper<UserPracticeRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(UserPracticeRecord::getUserId)
                .between(UserPracticeRecord::getSubmitTime,
                        month.atDay(1).atStartOfDay(),
                        month.atEndOfMonth().atTime(LocalTime.MAX))
                .isNotNull(UserPracticeRecord::getUserId);
        List<UserPracticeRecord> records = userPracticeRecordService.list(wrapper);
        if (records == null || records.isEmpty()) {
            return Collections.emptyList();
        }
        return records.stream()
                .map(UserPracticeRecord::getUserId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }

    boolean alreadyPushed(Long userId, String title) {
        LambdaQueryWrapper<Messages> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Messages::getUserId, userId)
                .eq(Messages::getUrl, NOTICE_REPORT_URL)
                .eq(Messages::getTitle, title);
        return messagesService.count(wrapper) > 0;
    }

    String buildPushTitle(YearMonth month) {
        return month.format(PUSH_TITLE_MONTH_FORMAT) + "学习报告已生成";
    }

    String buildPushContent(AppAnalysisReportVO report) {
        if (report == null) {
            return "点击可继续查看完整学情报告。";
        }
        String subtitle = StringUtils.trimToEmpty(report.getRankSubtitle());
        if (StringUtils.isBlank(subtitle)) {
            return "点击可继续查看完整学情报告。";
        }
        if (subtitle.endsWith("。") || subtitle.endsWith("！") || subtitle.endsWith("!")) {
            return subtitle + "点击可继续查看完整学情报告。";
        }
        return subtitle + "，点击可继续查看完整学情报告。";
    }
}
