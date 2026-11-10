package com.sqx.app.base.subscribe.service.impl;

import com.sqx.app.base.enums.AppError;
import com.sqx.app.base.notification.core.enums.NotificationChannel;
import com.sqx.app.base.notification.core.model.NotificationRequest;
import com.sqx.app.base.notification.core.service.INotificationService;
import com.sqx.app.base.subscribe.constants.SubscribeMessageScene;
import com.sqx.app.base.subscribe.support.SubscribeMessageBuilder;
import com.sqx.app.base.subscribe.support.SubscribeMessageConfigReader;
import com.sqx.app.base.user.db.service.IUserService;
import com.sqx.app.base.user.domain.entity.UserEntity;
import com.sqx.app.base.utils.wx.WxMaSubscribeMessage;
import com.sqx.common.core.exception.BizException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.YearMonth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubscribePushServiceImplTest {

    @Mock
    private SubscribeMessageConfigReader subscribeMessageConfigReader;
    @Mock
    private SubscribeMessageBuilder subscribeMessageBuilder;
    @Mock
    private INotificationService notificationService;
    @Mock
    private IUserService iUserService;

    @InjectMocks
    private SubscribePushServiceImpl subscribePushService;

    @Test
    void sendMonthlyReport_whenTemplateConfigured_shouldDispatchMaChannel() {
        when(subscribeMessageConfigReader.isConfigured(SubscribeMessageScene.MONTHLY_REPORT)).thenReturn(true);
        when(subscribeMessageConfigReader.getTemplateId(SubscribeMessageScene.MONTHLY_REPORT)).thenReturn("tmpl-monthly");
        UserEntity user = buildUser(100L, "openid-100");
        when(iUserService.getById(100L)).thenReturn(user);
        WxMaSubscribeMessage message = new WxMaSubscribeMessage();
        message.setToUser("openid-100");
        when(subscribeMessageBuilder.buildMonthlyReport(any(), any(), any(), any(), any(), any())).thenReturn(message);

        assertTrue(subscribePushService.sendMonthlyReport(
                100L,
                YearMonth.of(2026, 8),
                "2026年8月学习报告已生成",
                "本月进步显著"));

        ArgumentCaptor<NotificationRequest> captor = ArgumentCaptor.forClass(NotificationRequest.class);
        verify(notificationService).send(captor.capture(), eq(NotificationChannel.MA.code()));
        assertEquals(message, captor.getValue().getWxMaSubscribeMessage());
    }

    @Test
    void sendMonthlyReport_whenTemplateMissing_shouldSkip() {
        when(subscribeMessageConfigReader.isConfigured(SubscribeMessageScene.MONTHLY_REPORT)).thenReturn(false);
        assertFalse(subscribePushService.sendMonthlyReport(
                100L, YearMonth.of(2026, 8), "title", "content"));
        verify(notificationService, never()).send(any(NotificationRequest.class), any(String.class));
    }

    @Test
    void sendVipExpire_whenOpenIdMissing_shouldSkip() {
        when(subscribeMessageConfigReader.isConfigured(SubscribeMessageScene.VIP_EXPIRE)).thenReturn(true);
        UserEntity user = buildUser(200L, null);
        when(iUserService.getById(200L)).thenReturn(user);

        assertFalse(subscribePushService.sendVipExpire(
                200L, "会员到期", LocalDateTime.now(), "请续费"));
        verify(notificationService, never()).send(any(NotificationRequest.class), any(String.class));
    }

    @Test
    void sendTest_whenTemplateNotConfigured_shouldThrow() {
        when(subscribeMessageConfigReader.getTemplateId(SubscribeMessageScene.WRONG_REVIEW)).thenReturn(null);
        BizException ex = assertThrows(BizException.class,
                () -> subscribePushService.sendTest(1L, "wrong_review"));
        assertEquals(AppError.SUBSCRIBE_TEMPLATE_NOT_CONFIGURED.getCode(), ex.getErrorCode().getCode());
    }

    @Test
    void sendTest_whenOpenIdMissing_shouldThrow() {
        when(subscribeMessageConfigReader.getTemplateId(SubscribeMessageScene.MONTHLY_REPORT)).thenReturn("tmpl");
        when(iUserService.getById(1L)).thenReturn(buildUser(1L, ""));

        BizException ex = assertThrows(BizException.class,
                () -> subscribePushService.sendTest(1L, "monthly_report"));
        assertEquals(AppError.SUBSCRIBE_OPENID_MISSING.getCode(), ex.getErrorCode().getCode());
    }

    @Test
    void getTemplates_shouldReturnConfiguredIds() {
        when(subscribeMessageConfigReader.getTemplateId(SubscribeMessageScene.MONTHLY_REPORT)).thenReturn("a");
        when(subscribeMessageConfigReader.getTemplateId(SubscribeMessageScene.VIP_EXPIRE)).thenReturn("b");
        when(subscribeMessageConfigReader.getTemplateId(SubscribeMessageScene.WRONG_REVIEW)).thenReturn(null);

        assertEquals("a", subscribePushService.getTemplates().getMonthlyReportTemplateId());
        assertEquals("b", subscribePushService.getTemplates().getVipExpireTemplateId());
        assertEquals(null, subscribePushService.getTemplates().getWrongReviewTemplateId());
    }

    private UserEntity buildUser(Long userId, String maOpenId) {
        UserEntity user = new UserEntity();
        user.setUserId(userId);
        user.setMaOpenId(maOpenId);
        return user;
    }
}
