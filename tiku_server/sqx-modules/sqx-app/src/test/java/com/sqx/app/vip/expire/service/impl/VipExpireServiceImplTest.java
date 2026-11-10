package com.sqx.app.vip.expire.service.impl;

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
class VipExpireServiceImplTest {

    @Mock
    private IUserService iUserService;
    @Mock
    private INotificationService notificationService;
    @Mock
    private IMessagesService messagesService;
    @Mock
    private SubscribePushService subscribePushService;

    @InjectMocks
    private VipExpireServiceImpl vipExpireService;

    @BeforeEach
    void initEntityCache() {
        Configuration configuration = new Configuration();
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(configuration, "test");
        TableInfoHelper.initTableInfo(assistant, UserEntity.class);
        TableInfoHelper.initTableInfo(assistant, Messages.class);
    }

    @Test
    void downgradeIfExpired_whenNotVip_shouldSkip() {
        UserEntity user = buildVipUser(100L, 0, LocalDateTime.now().minusDays(1));
        when(iUserService.getById(100L)).thenReturn(user);

        assertFalse(vipExpireService.downgradeIfExpired(100L));

        verify(iUserService, never()).update(any());
        verify(notificationService, never()).send(any(NotificationRequest.class), any(String.class));
    }

    @Test
    void downgradeIfExpired_whenVipNotYetExpired_shouldSkip() {
        UserEntity user = buildVipUser(100L, 1, LocalDateTime.now().plusDays(3));
        when(iUserService.getById(100L)).thenReturn(user);

        assertFalse(vipExpireService.downgradeIfExpired(100L));

        verify(iUserService, never()).update(any());
        verify(notificationService, never()).send(any(NotificationRequest.class), any(String.class));
    }

    @Test
    void downgradeIfExpired_whenExpired_shouldDowngradeAndPushNoticeMember() {
        LocalDateTime expireAt = LocalDateTime.of(2026, 8, 10, 12, 0);
        UserEntity user = buildVipUser(100L, 1, expireAt);
        when(iUserService.getById(100L)).thenReturn(user);
        when(iUserService.update(any())).thenReturn(true);
        when(messagesService.count(any(LambdaQueryWrapper.class))).thenReturn(0L);

        assertTrue(vipExpireService.downgradeIfExpired(100L));

        verify(iUserService).update(any());
        ArgumentCaptor<NotificationRequest> captor = ArgumentCaptor.forClass(NotificationRequest.class);
        verify(notificationService).send(captor.capture(), eq(NotificationChannel.INSTATION.code()));
        NotificationRequest request = captor.getValue();
        assertEquals(Long.valueOf(100L), request.getUserId());
        assertEquals(VipExpireServiceImpl.NOTICE_MEMBER_URL, request.getUrl());
        assertEquals("会员已到期（2026年8月10日）", request.getTitle());
        assertTrue(request.getContent().contains("2026年8月10日"));
        assertEquals(Integer.valueOf(2), request.getType());
    }

    @Test
    void downgradeIfExpired_whenNoticeAlreadyPushed_shouldSkipDuplicateNotice() {
        LocalDateTime expireAt = LocalDateTime.of(2026, 8, 10, 12, 0);
        UserEntity user = buildVipUser(100L, 1, expireAt);
        when(iUserService.getById(100L)).thenReturn(user);
        when(iUserService.update(any())).thenReturn(true);
        when(messagesService.count(any(LambdaQueryWrapper.class))).thenReturn(1L);

        assertTrue(vipExpireService.downgradeIfExpired(100L));

        verify(iUserService).update(any());
        verify(notificationService, never()).send(any(NotificationRequest.class), any(String.class));
    }

    @Test
    void refreshIfExpired_whenExpired_shouldReturnReloadedUser() {
        LocalDateTime expireAt = LocalDateTime.now().minusHours(1);
        UserEntity expiredUser = buildVipUser(200L, 1, expireAt);
        UserEntity downgradedUser = buildVipUser(200L, 0, expireAt);
        when(iUserService.getById(200L)).thenReturn(expiredUser, expiredUser, downgradedUser);
        when(iUserService.update(any())).thenReturn(true);
        when(messagesService.count(any(LambdaQueryWrapper.class))).thenReturn(0L);

        UserEntity refreshed = vipExpireService.refreshIfExpired(200L);

        assertEquals(Integer.valueOf(0), refreshed.getIsVip());
        verify(iUserService).update(any());
    }

    @Test
    void downgradeAllExpired_shouldProcessOnlyExpiredUsers() {
        VipExpireServiceImpl service = spy(vipExpireService);
        UserEntity expired = buildVipUser(10L, 1, LocalDateTime.now().minusDays(1));
        UserEntity active = buildVipUser(20L, 1, LocalDateTime.now().plusDays(5));
        doReturn(Arrays.asList(expired, active)).when(service).listExpiredVipUsers(any(LocalDateTime.class));
        doReturn(true, false).when(service).downgradeIfExpired(any(Long.class), any(LocalDateTime.class));

        int downgraded = service.downgradeAllExpired();

        assertEquals(1, downgraded);
    }

    @Test
    void downgradeAllExpired_whenNoExpiredUsers_shouldReturnZero() {
        VipExpireServiceImpl service = spy(vipExpireService);
        doReturn(Collections.emptyList()).when(service).listExpiredVipUsers(any(LocalDateTime.class));

        assertEquals(0, service.downgradeAllExpired());

        verify(service, never()).downgradeIfExpired(any(Long.class), any(LocalDateTime.class));
    }

    @Test
    void isExpired_whenExpireAtMissing_shouldReturnFalse() {
        UserEntity user = buildVipUser(300L, 1, null);
        assertFalse(vipExpireService.isExpired(user, LocalDateTime.now()));
    }

    private UserEntity buildVipUser(Long userId, int isVip, LocalDateTime expireAt) {
        UserEntity user = new UserEntity();
        user.setUserId(userId);
        user.setIsVip(isVip);
        user.setUserType(isVip == 1 ? 2 : 1);
        user.setVipExpireAt(expireAt);
        return user;
    }
}
