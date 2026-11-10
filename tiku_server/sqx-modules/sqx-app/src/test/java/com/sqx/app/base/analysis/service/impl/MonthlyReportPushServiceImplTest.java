package com.sqx.app.base.analysis.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sqx.app.base.analysis.domain.vo.AppAnalysisReportVO;
import com.sqx.app.base.analysis.service.AnalysisReportService;
import com.sqx.app.base.messages.db.service.IMessagesService;
import com.sqx.app.base.notification.core.enums.NotificationChannel;
import com.sqx.app.base.notification.core.model.NotificationRequest;
import com.sqx.app.base.notification.core.service.INotificationService;
import com.sqx.app.base.subscribe.service.SubscribePushService;
import com.sqx.system.db.service.IUserPracticeRecordService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.YearMonth;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MonthlyReportPushServiceImplTest {

    @Mock
    private AnalysisReportService analysisReportService;
    @Mock
    private INotificationService notificationService;
    @Mock
    private IMessagesService messagesService;
    @Mock
    private IUserPracticeRecordService userPracticeRecordService;
    @Mock
    private SubscribePushService subscribePushService;

    @InjectMocks
    private MonthlyReportPushServiceImpl monthlyReportPushService;

    @Test
    void pushToUser_whenNotYetPushed_shouldSendInstationNoticeReport() {
        YearMonth month = YearMonth.of(2026, 8);
        AppAnalysisReportVO report = new AppAnalysisReportVO();
        report.setRankSubtitle("\u4f60\u5728\u6570\u5b66\u548c\u82f1\u8bed\u65b9\u9762\u7684\u8fdb\u6b65\u975e\u5e38\u663e\u8457");
        when(messagesService.count(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(analysisReportService.getReport(eq(100L), eq(month))).thenReturn(report);
        boolean pushed = monthlyReportPushService.pushToUser(100L, month);
        assertTrue(pushed);
        ArgumentCaptor<NotificationRequest> captor = ArgumentCaptor.forClass(NotificationRequest.class);
        verify(notificationService).send(captor.capture(), eq(NotificationChannel.INSTATION.code()));
        NotificationRequest request = captor.getValue();
        assertEquals(Long.valueOf(100L), request.getUserId());
        assertEquals("2026\u5e748\u6708\u5b66\u4e60\u62a5\u544a\u5df2\u751f\u6210", request.getTitle());
        assertEquals("\u4f60\u5728\u6570\u5b66\u548c\u82f1\u8bed\u65b9\u9762\u7684\u8fdb\u6b65\u975e\u5e38\u663e\u8457\uff0c\u70b9\u51fb\u53ef\u7ee7\u7eed\u67e5\u770b\u5b8c\u6574\u5b66\u60c5\u62a5\u544a\u3002", request.getContent());
        assertEquals(MonthlyReportPushServiceImpl.NOTICE_REPORT_URL, request.getUrl());
        assertEquals(Integer.valueOf(2), request.getType());
    }

    @Test
    void pushToUser_whenAlreadyPushed_shouldSkip() {
        YearMonth month = YearMonth.of(2026, 7);
        when(messagesService.count(any(LambdaQueryWrapper.class))).thenReturn(1L);
        boolean pushed = monthlyReportPushService.pushToUser(200L, month);
        assertFalse(pushed);
        verify(analysisReportService, never()).getReport(any(), any());
        verify(notificationService, never()).send(any(NotificationRequest.class), any(String.class));
    }

    @Test
    void pushForMonth_shouldPushOnlyActiveUsersAndSkipDuplicates() {
        MonthlyReportPushServiceImpl service = spy(monthlyReportPushService);
        YearMonth month = YearMonth.of(2026, 6);
        doReturn(Arrays.asList(10L, 20L)).when(service).listActiveUserIds(month);
        when(messagesService.count(any(LambdaQueryWrapper.class))).thenReturn(0L, 1L);
        AppAnalysisReportVO report = new AppAnalysisReportVO();
        report.setRankSubtitle("\u672c\u6708\u5df2\u5b8c\u6210 15 \u9053\u9898\uff0c\u575a\u6301\u5c31\u662f\u8fdb\u6b65");
        when(analysisReportService.getReport(eq(10L), eq(month))).thenReturn(report);
        int pushed = service.pushForMonth(month);
        assertEquals(1, pushed);
        verify(notificationService).send(any(NotificationRequest.class), eq(NotificationChannel.INSTATION.code()));
    }

    @Test
    void buildPushTitle_shouldFormatChineseMonthTitle() {
        assertEquals("2026\u5e748\u6708\u5b66\u4e60\u62a5\u544a\u5df2\u751f\u6210",
                monthlyReportPushService.buildPushTitle(YearMonth.of(2026, 8)));
    }

    @Test
    void buildPushContent_whenSubtitleEndsWithPunctuation_shouldNotDuplicateComma() {
        AppAnalysisReportVO report = new AppAnalysisReportVO();
        report.setRankSubtitle("\u672c\u6708\u5c1a\u672a\u5f00\u59cb\u7ec3\u4e60\uff0c\u73b0\u5728\u51fa\u53d1\u4e5f\u4e0d\u665a\u3002");
        String content = monthlyReportPushService.buildPushContent(report);
        assertEquals("\u672c\u6708\u5c1a\u672a\u5f00\u59cb\u7ec3\u4e60\uff0c\u73b0\u5728\u51fa\u53d1\u4e5f\u4e0d\u665a\u3002\u70b9\u51fb\u53ef\u7ee7\u7eed\u67e5\u770b\u5b8c\u6574\u5b66\u60c5\u62a5\u544a\u3002", content);
    }

    @Test
    void pushForMonth_whenNoActiveUsers_shouldReturnZero() {
        MonthlyReportPushServiceImpl service = spy(monthlyReportPushService);
        YearMonth month = YearMonth.of(2026, 5);
        doReturn(Collections.emptyList()).when(service).listActiveUserIds(month);
        assertEquals(0, service.pushForMonth(month));
        verify(notificationService, never()).send(any(NotificationRequest.class), any(String.class));
    }

    @Test
    void alreadyPushed_shouldQueryByUserUrlAndTitle() {
        when(messagesService.count(any(LambdaQueryWrapper.class))).thenReturn(0L);
        assertFalse(monthlyReportPushService.alreadyPushed(99L, "2026\u5e745\u6708\u5b66\u4e60\u62a5\u544a\u5df2\u751f\u6210"));
        verify(messagesService).count(any(LambdaQueryWrapper.class));
    }
}