package com.sqx.app.vip.renew.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.sqx.app.base.messages.db.service.IMessagesService;
import com.sqx.app.base.messages.domain.entity.Messages;
import com.sqx.app.base.notification.core.enums.NotificationChannel;
import com.sqx.app.base.notification.core.model.NotificationRequest;
import com.sqx.app.base.notification.core.service.INotificationService;
import com.sqx.app.base.subscribe.service.SubscribePushService;
import com.sqx.app.base.user.db.service.IUserService;
import com.sqx.app.base.user.domain.entity.UserEntity;
import com.sqx.app.vip.renew.support.VipRenewConfigReader;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
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
class VipRenewNoticeServiceImplTest {

    @Mock
    private IUserService iUserService;
    @Mock
    private INotificationService notificationService;
    @Mock
    private IMessagesService messagesService;
    @Mock
    private VipRenewConfigReader vipRenewConfigReader;
    @Mock
    private SubscribePushService subscribePushService;

    @InjectMocks
    private VipRenewNoticeServiceImpl vipRenewNoticeService;

    @BeforeEach
    void initEntityCache() {
        Configuration configuration = new Configuration();
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(configuration, "test");
        TableInfoHelper.initTableInfo(assistant, UserEntity.class);
        TableInfoHelper.initTableInfo(assistant, Messages.class);
    }

    @Test
    void pushToUser_whenWithinNoticeWindow_shouldSendNoticeMember() {
        LocalDateTime now = LocalDateTime.of(2026, 8, 12, 9, 0);
        LocalDateTime expireAt = LocalDateTime.of(2026, 8, 15, 12, 0);
        UserEntity user = buildVipUser(100L, expireAt);
        when(messagesService.count(any(LambdaQueryWrapper.class))).thenReturn(0L);

        boolean pushed = vipRenewNoticeService.pushToUser(user, now, 3);

        assertTrue(pushed);
        ArgumentCaptor<NotificationRequest> captor = ArgumentCaptor.forClass(NotificationRequest.class);
        verify(notificationService).send(captor.capture(), eq(NotificationChannel.INSTATION.code()));
        NotificationRequest request = captor.getValue();
        assertEquals(Long.valueOf(100L), request.getUserId());
        assertEquals(VipRenewNoticeServiceImpl.NOTICE_MEMBER_URL, request.getUrl());
        assertEquals("会员即将到期提醒（2026年8月15日）", request.getTitle());
        assertEquals("你的会员还有 3 天到期，续费后可继续使用无限次 AI 搜题与答疑。", request.getContent());
        assertEquals(Integer.valueOf(2), request.getType());
    }

    @Test
    void pushToUser_whenAlreadyPushed_shouldSkipDuplicate() {
        LocalDateTime now = LocalDateTime.of(2026, 8, 12, 9, 0);
        LocalDateTime expireAt = LocalDateTime.of(2026, 8, 15, 12, 0);
        UserEntity user = buildVipUser(100L, expireAt);
        when(messagesService.count(any(LambdaQueryWrapper.class))).thenReturn(1L);

        assertFalse(vipRenewNoticeService.pushToUser(user, now, 3));

        verify(notificationService, never()).send(any(NotificationRequest.class), any(String.class));
    }

    @Test
    void pushToUser_whenNotVip_shouldSkip() {
        LocalDateTime now = LocalDateTime.of(2026, 8, 12, 9, 0);
        UserEntity user = buildVipUser(100L, LocalDateTime.of(2026, 8, 15, 12, 0));
        user.setIsVip(0);

        assertFalse(vipRenewNoticeService.pushToUser(user, now, 3));

        verify(notificationService, never()).send(any(NotificationRequest.class), any(String.class));
    }

    @Test
    void pushToUser_whenAlreadyExpired_shouldSkip() {
        LocalDateTime now = LocalDateTime.of(2026, 8, 12, 9, 0);
        UserEntity user = buildVipUser(100L, LocalDateTime.of(2026, 8, 11, 12, 0));

        assertFalse(vipRenewNoticeService.pushToUser(user, now, 3));

        verify(notificationService, never()).send(any(NotificationRequest.class), any(String.class));
    }

    @Test
    void pushToUser_whenExpireBeyondNoticeWindow_shouldSkip() {
        LocalDateTime now = LocalDateTime.of(2026, 8, 12, 9, 0);
        UserEntity user = buildVipUser(100L, LocalDateTime.of(2026, 8, 20, 12, 0));

        assertFalse(vipRenewNoticeService.pushToUser(user, now, 3));

        verify(notificationService, never()).send(any(NotificationRequest.class), any(String.class));
    }

    @Test
    void pushAllDue_shouldPushOnlyUsersWithinWindow() {
        VipRenewNoticeServiceImpl service = spy(vipRenewNoticeService);
        LocalDateTime now = LocalDateTime.of(2026, 8, 12, 9, 0);
        UserEntity dueUser = buildVipUser(10L, LocalDateTime.of(2026, 8, 14, 12, 0));
        UserEntity farUser = buildVipUser(20L, LocalDateTime.of(2026, 8, 30, 12, 0));
        when(vipRenewConfigReader.getNoticeDaysBeforeExpire()).thenReturn(3);
        doReturn(Arrays.asList(dueUser, farUser)).when(service).listDueVipUsers(any(LocalDateTime.class), eq(3));
        doReturn(true, false).when(service).pushToUser(any(UserEntity.class), any(LocalDateTime.class), eq(3));

        int pushed = service.pushAllDue();

        assertEquals(1, pushed);
    }

    @Test
    void pushAllDue_whenNoDueUsers_shouldReturnZero() {
        VipRenewNoticeServiceImpl service = spy(vipRenewNoticeService);
        when(vipRenewConfigReader.getNoticeDaysBeforeExpire()).thenReturn(3);
        doReturn(Collections.emptyList()).when(service).listDueVipUsers(any(LocalDateTime.class), eq(3));

        assertEquals(0, service.pushAllDue());

        verify(service, never()).pushToUser(any(UserEntity.class), any(LocalDateTime.class), any(Integer.class));
    }

    @Test
    void pushToUserById_whenUserDue_shouldLoadAndPush() {
        LocalDateTime expireAt = LocalDateTime.now().plusDays(2);
        UserEntity user = buildVipUser(300L, expireAt);
        when(iUserService.getById(300L)).thenReturn(user);
        when(vipRenewConfigReader.getNoticeDaysBeforeExpire()).thenReturn(3);
        when(messagesService.count(any(LambdaQueryWrapper.class))).thenReturn(0L);

        assertTrue(vipRenewNoticeService.pushToUser(300L));

        verify(notificationService).send(any(NotificationRequest.class), eq(NotificationChannel.INSTATION.code()));
    }

    private UserEntity buildVipUser(Long userId, LocalDateTime expireAt) {
        UserEntity user = new UserEntity();
        user.setUserId(userId);
        user.setIsVip(1);
        user.setUserType(2);
        user.setVipExpireAt(expireAt);
        return user;
    }
}
