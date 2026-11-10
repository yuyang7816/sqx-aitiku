package com.sqx.app.base.messages.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sqx.app.base.messages.db.service.IMessagesService;
import com.sqx.app.base.messages.domain.entity.Messages;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.sqx.app.base.messages.domain.vo.AppMessageItemVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;

/**
 * GET /app/messages 用户端消息列表单元测试
 */
@ExtendWith(MockitoExtension.class)
class MessagesServiceImplAppMessagesTest {

    @Mock
    private IMessagesService iMessagesService;

    @InjectMocks
    private MessagesServiceImpl messagesService;

    @Test
    void pageAppMessages_shouldReturnRecordsForCurrentUserOnly() {
        Messages unread = buildMessage(1L, "notice-review", "待复习错题提醒", 0);
        Messages readMsg = buildMessage(2L, "notice-member", "会员即将到期", 1);

        Page<Messages> entityPage = new Page<>(1, 10);
        entityPage.setTotal(2);
        entityPage.setRecords(Arrays.asList(unread, readMsg));
        when(iMessagesService.page(any(Page.class), any())).thenReturn(entityPage);

        IPage<AppMessageItemVO> result = messagesService.pageAppMessages(100L, 1L, 10L, null);

        assertEquals(2, result.getTotal());
        AppMessageItemVO first = result.getRecords().get(0);
        assertEquals(1L, first.getMessageId());
        assertEquals("notice-review", first.getId());
        assertEquals("待复习错题提醒", first.getTitle());
        assertFalse(first.getRead());

        AppMessageItemVO second = result.getRecords().get(1);
        assertEquals("notice-member", second.getId());
        assertTrue(second.getRead());

        ArgumentCaptor<Page> pageCaptor = ArgumentCaptor.forClass(Page.class);
        verify(iMessagesService).page(pageCaptor.capture(), any());
        assertEquals(1L, pageCaptor.getValue().getCurrent());
        assertEquals(10L, pageCaptor.getValue().getSize());
    }

    @Test
    void pageAppMessages_whenReadFilterTrue_shouldOnlyQueryReadMessages() {
        Page<Messages> entityPage = new Page<>(1, 10);
        entityPage.setTotal(0);
        entityPage.setRecords(Collections.emptyList());
        when(iMessagesService.page(any(Page.class), any())).thenReturn(entityPage);

        IPage<AppMessageItemVO> result = messagesService.pageAppMessages(100L, null, null, true);

        assertEquals(0, result.getTotal());
        verify(iMessagesService).page(any(Page.class), any());
    }

    @Test
    void pageAppMessages_whenReadFilterFalse_shouldOnlyReturnUnreadMessages() {
        Messages unread = buildMessage(3L, "notice-review", "待复习", 0);
        Page<Messages> entityPage = new Page<>(1, 1);
        entityPage.setTotal(1);
        entityPage.setRecords(Collections.singletonList(unread));
        when(iMessagesService.page(any(Page.class), any())).thenReturn(entityPage);

        IPage<AppMessageItemVO> result = messagesService.pageAppMessages(100L, 1L, 1L, false);

        assertEquals(1, result.getTotal());
        assertEquals(1, result.getRecords().size());
        assertFalse(result.getRecords().get(0).getRead());
        assertEquals("notice-review", result.getRecords().get(0).getId());
    }

    @Test
    void resolveJumpId_whenUrlBlank_shouldFallbackToMsgPrefix() {
        Messages message = buildMessage(99L, null, "测试", 0);

        assertEquals("msg-99", messagesService.resolveJumpId(message));
    }

    @Test
    void resolveJumpId_whenUrlPresent_shouldTrimAndUseUrl() {
        Messages message = buildMessage(99L, "  notice-report  ", "月报", 0);

        assertEquals("notice-report", messagesService.resolveJumpId(message));
    }

    @Test
    void formatMessageTime_whenJustCreated_shouldReturnJustNow() {
        String time = messagesService.formatMessageTime(LocalDateTime.now().minusSeconds(30));

        assertEquals("刚刚", time);
    }

    @Test
    void formatMessageTime_whenSameDay_shouldReturnTodayPrefix() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime sameDayPast = now.minusMinutes(90);
        if (!sameDayPast.toLocalDate().equals(now.toLocalDate())) {
            sameDayPast = now.withHour(12).withMinute(0).withSecond(0).withNano(0);
        }
        String time = messagesService.formatMessageTime(sameDayPast);
        String suffix = sameDayPast.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"));
        assertTrue(time.startsWith("今天 "));
        assertTrue(time.endsWith(suffix));
    }

    @Test
    void markAllAppMessagesRead_shouldUpdateUnreadMessagesForCurrentUser() {
        when(iMessagesService.update(any(Messages.class), any(UpdateWrapper.class))).thenReturn(true);

        messagesService.markAllAppMessagesRead(100L);

        verify(iMessagesService, times(1)).update(any(Messages.class), any(UpdateWrapper.class));
    }

    @Test
    void toAppMessageItemVO_shouldMapAllBusinessFields() {
        Messages message = buildMessage(5L, "notice-vip-guide", "AI 次数提醒", 0);
        message.setContent("今日剩余 2 次");
        message.setType(2);
        message.setDataId(888L);
        message.setImageUrls("https://example.com/a.png");

        AppMessageItemVO vo = messagesService.toAppMessageItemVO(message);

        assertEquals(5L, vo.getMessageId());
        assertEquals("notice-vip-guide", vo.getId());
        assertEquals("AI 次数提醒", vo.getTitle());
        assertEquals("今日剩余 2 次", vo.getContent());
        assertEquals("notice-vip-guide", vo.getUrl());
        assertEquals(Integer.valueOf(2), vo.getType());
        assertEquals(Long.valueOf(888L), vo.getDataId());
        assertEquals("https://example.com/a.png", vo.getImageUrls());
        assertFalse(vo.getRead());
    }

    @Test
    void resolveIsRead_shouldMapBooleanToIntegerFlag() {
        assertEquals(Integer.valueOf(0), messagesService.resolveIsRead(null));
        assertEquals(Integer.valueOf(0), messagesService.resolveIsRead(false));
        assertEquals(Integer.valueOf(1), messagesService.resolveIsRead(true));
    }

    private Messages buildMessage(Long id, String url, String title, int isRead) {
        Messages message = new Messages();
        message.setId(id);
        message.setUserId(100L);
        message.setUrl(url);
        message.setTitle(title);
        message.setContent("内容");
        message.setIsRead(isRead);
        message.setCreateTime(LocalDateTime.now());
        return message;
    }
}
