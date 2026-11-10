package com.sqx.app.base.subscribe.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sqx.app.base.messages.db.service.IMessagesService;
import com.sqx.app.base.notification.core.enums.NotificationChannel;
import com.sqx.app.base.notification.core.model.NotificationRequest;
import com.sqx.app.base.notification.core.service.INotificationService;
import com.sqx.app.base.subscribe.service.SubscribePushService;
import com.sqx.system.db.entity.UserQuestionWrong;
import com.sqx.system.db.service.IUserQuestionWrongService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WrongReviewPushServiceImplTest {

    @Mock
    private IUserQuestionWrongService iUserQuestionWrongService;
    @Mock
    private INotificationService notificationService;
    @Mock
    private IMessagesService messagesService;
    @Mock
    private SubscribePushService subscribePushService;

    @InjectMocks
    private WrongReviewPushServiceImpl wrongReviewPushService;

    @Test
    void pushToUser_whenNoStaleWrong_shouldSkip() {
        when(iUserQuestionWrongService.count(any(LambdaQueryWrapper.class))).thenReturn(0L);
        assertFalse(wrongReviewPushService.pushToUser(100L));
        verify(notificationService, never()).send(any(NotificationRequest.class), any(String.class));
    }

    @Test
    void pushToUser_whenHasStaleWrong_shouldPushInstationAndSubscribe() {
        when(iUserQuestionWrongService.count(any(LambdaQueryWrapper.class))).thenReturn(3L);
        when(messagesService.count(any(LambdaQueryWrapper.class))).thenReturn(0L);

        assertTrue(wrongReviewPushService.pushToUser(100L));

        ArgumentCaptor<NotificationRequest> captor = ArgumentCaptor.forClass(NotificationRequest.class);
        verify(notificationService).send(captor.capture(), eq(NotificationChannel.INSTATION.code()));
        NotificationRequest request = captor.getValue();
        assertEquals(Long.valueOf(100L), request.getUserId());
        assertEquals(WrongReviewPushServiceImpl.NOTICE_REVIEW_URL, request.getUrl());
        assertEquals("待复习错题提醒（3题）", request.getTitle());
        verify(subscribePushService).sendWrongReview(eq(100L), eq(3), any(String.class), any(String.class));
    }

    @Test
    void pushToUser_whenAlreadyPushed_shouldSkipDuplicate() {
        when(iUserQuestionWrongService.count(any(LambdaQueryWrapper.class))).thenReturn(2L);
        when(messagesService.count(any(LambdaQueryWrapper.class))).thenReturn(1L);

        assertFalse(wrongReviewPushService.pushToUser(200L));
        verify(notificationService, never()).send(any(NotificationRequest.class), any(String.class));
    }
}
