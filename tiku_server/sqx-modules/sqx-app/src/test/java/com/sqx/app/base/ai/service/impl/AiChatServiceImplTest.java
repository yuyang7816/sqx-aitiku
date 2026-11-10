package com.sqx.app.base.ai.service.impl;

import com.sqx.app.base.ai.domain.bo.AppAiChatBO;
import com.sqx.app.base.ai.domain.bo.AppAiChatHistoryItemBO;
import com.sqx.app.base.ai.domain.vo.AppAiChatResultVO;
import com.sqx.app.base.ai.domain.vo.AppAiQuotaVO;
import com.sqx.app.base.ai.enums.AiError;
import com.sqx.app.base.ai.factory.AiChatFactory;
import com.sqx.app.base.ai.service.AiQuotaService;
import com.sqx.app.base.ai.strategy.IAiChatPlatformStrategy;
import com.sqx.app.base.ai.support.AiSensitiveWordFilter;
import com.sqx.common.core.exception.BizException;
import com.sqx.app.base.ai.support.AiConfigReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiChatServiceImplTest {

    @Mock
    private AiConfigReader aiConfigReader;

    @Mock
    private AiQuotaService aiQuotaService;

    @Mock
    private AiChatFactory aiChatFactory;

    @Mock
    private IAiChatPlatformStrategy platformStrategy;

    @Mock
    private AiSensitiveWordFilter aiSensitiveWordFilter;

    @InjectMocks
    private AiChatServiceImpl aiChatService;

    @Test
    void chat_whenMessageEmpty_shouldThrow() {
        AppAiChatBO bo = new AppAiChatBO();
        bo.setMessage("  ");

        BizException ex = assertThrows(BizException.class, () -> aiChatService.chat(1L, bo));
        assertEquals(AiError.MESSAGE_EMPTY.getCode(), ex.getErrorCode().getCode());
        verify(aiQuotaService, never()).consumeChatQuota(any());
    }

    @Test
    void chat_whenInputSensitive_shouldNotConsumeQuota() {
        org.mockito.Mockito.doThrow(new BizException(AiError.SENSITIVE_INPUT))
                .when(aiSensitiveWordFilter).validateChatInput(any(AppAiChatBO.class));

        AppAiChatBO bo = new AppAiChatBO();
        bo.setMessage("\u6d89\u53ca\u8d4c\u535a");

        BizException ex = assertThrows(BizException.class, () -> aiChatService.chat(1L, bo));
        assertEquals(AiError.SENSITIVE_INPUT.getCode(), ex.getErrorCode().getCode());
        verify(aiQuotaService, never()).consumeChatQuota(any());
    }

    @Test
    void chat_whenReplySensitive_shouldThrowAfterPlatform() {
        AppAiQuotaVO quota = new AppAiQuotaVO();
        quota.setAiDailyLimit(5);
        quota.setAiUsedToday(1);
        when(aiQuotaService.consumeChatQuota(1L)).thenReturn(quota);
        when(aiChatFactory.getPlatform()).thenReturn(platformStrategy);
        when(platformStrategy.chat(any(AppAiChatBO.class))).thenReturn("\u56de\u590d\u542b\u8272\u60c5\u4fe1\u606f");
        org.mockito.Mockito.doThrow(new BizException(AiError.SENSITIVE_REPLY))
                .when(aiSensitiveWordFilter).validateReply("\u56de\u590d\u542b\u8272\u60c5\u4fe1\u606f");

        AppAiChatBO bo = new AppAiChatBO();
        bo.setMessage("\u6b63\u5e38\u63d0\u95ee");

        BizException ex = assertThrows(BizException.class, () -> aiChatService.chat(1L, bo));
        assertEquals(AiError.SENSITIVE_REPLY.getCode(), ex.getErrorCode().getCode());
    }

    @Test
    void chat_whenQuotaExceeded_shouldNotCallPlatform() {
        when(aiQuotaService.consumeChatQuota(1L)).thenThrow(new BizException(AiError.QUOTA_EXCEEDED));

        AppAiChatBO bo = new AppAiChatBO();
        bo.setMessage("\u8bf7\u5e2e\u6211\u8bb2\u89e3");

        BizException ex = assertThrows(BizException.class, () -> aiChatService.chat(1L, bo));
        assertEquals(AiError.QUOTA_EXCEEDED.getCode(), ex.getErrorCode().getCode());
        verify(aiChatFactory, never()).getPlatform();
    }

    @Test
    void chat_whenFirstTurn_shouldReturnReplyAndQuota() {
        AppAiQuotaVO quota = new AppAiQuotaVO();
        quota.setAiDailyLimit(5);
        quota.setAiUsedToday(1);
        quota.setAiUnlimited(false);
        when(aiQuotaService.consumeChatQuota(1L)).thenReturn(quota);
        when(aiChatFactory.getPlatform()).thenReturn(platformStrategy);
        when(platformStrategy.chat(any(AppAiChatBO.class)))
                .thenReturn("\u6536\u5230\uff0c\u6211\u5148\u7ed3\u5408\u8fd9\u9053\u9898\u7684\u8003\u70b9\u548c\u6613\u9519\u70b9\uff0c\u5e2e\u4f60\u4ece\u601d\u8def\u5230\u6b65\u9aa4\u5b8c\u6574\u8bb2\u4e00\u904d\u3002");

        AppAiChatBO bo = new AppAiChatBO();
        bo.setTopic("x + 3 = 2");
        bo.setMessage("\u8bf7\u5e2e\u6211\u8bb2\u89e3\u8fd9\u9053\u9898\u76ee\uff1ax + 3 = 2");

        AppAiChatResultVO result = aiChatService.chat(1L, bo);

        assertNotNull(result.getReply());
        assertTrue(result.getReply().contains("\u8003\u70b9"));
        assertEquals(1, result.getQuota().getAiUsedToday());
        assertEquals(5, result.getQuota().getAiDailyLimit());
    }

    @Test
    void chat_whenMultiTurnWithHistory_shouldReturnFollowUpReply() {
        AppAiQuotaVO quota = new AppAiQuotaVO();
        quota.setAiDailyLimit(5);
        quota.setAiUsedToday(2);
        quota.setAiUnlimited(false);
        when(aiQuotaService.consumeChatQuota(1L)).thenReturn(quota);
        when(aiChatFactory.getPlatform()).thenReturn(platformStrategy);
        when(platformStrategy.chat(any(AppAiChatBO.class)))
                .thenReturn("\u56e0\u4e3a\u79fb\u9879\u4f9d\u636e\u7684\u662f\u7b49\u5f0f\u7684\u57fa\u672c\u6027\u8d28\uff1a\u7b49\u5f0f\u4e24\u8fb9\u540c\u65f6\u52a0\u6216\u51cf\u540c\u4e00\u4e2a\u6570\uff0c\u7b49\u5f0f\u4ecd\u7136\u6210\u7acb\u3002");

        AppAiChatHistoryItemBO userHistory = new AppAiChatHistoryItemBO();
        userHistory.setRole("user");
        userHistory.setText("\u8bf7\u5e2e\u6211\u89e3\u7b54\u8fd9\u9053\u65b9\u7a0b\u9898\uff1ax + 3 = 2 \u600e\u4e48\u89e3");
        AppAiChatHistoryItemBO aiHistory = new AppAiChatHistoryItemBO();
        aiHistory.setRole("ai");
        aiHistory.setText("\u5148\u79fb\u9879\u5f97\u5230 x = -1");

        AppAiChatBO bo = new AppAiChatBO();
        bo.setTopic("x + 3 = 2");
        bo.setMessage("\u4e3a\u4ec0\u4e48\u8981\u53d8\u53f7\uff1f");
        bo.setHistory(Arrays.asList(userHistory, aiHistory));

        AppAiChatResultVO result = aiChatService.chat(1L, bo);

        assertNotNull(result.getReply());
        assertEquals(2, result.getQuota().getAiUsedToday());
        verify(platformStrategy).chat(bo);
    }

    @Test
    void chat_whenDoubaoRequestFails_shouldFallbackToLocalTemplate() {
        AppAiQuotaVO quota = new AppAiQuotaVO();
        quota.setAiDailyLimit(5);
        quota.setAiUsedToday(1);
        quota.setAiUnlimited(false);
        when(aiQuotaService.consumeChatQuota(1L)).thenReturn(quota);
        when(aiConfigReader.getPlatform()).thenReturn("doubao");
        when(aiChatFactory.getPlatform()).thenReturn(platformStrategy);
        doThrow(new BizException("请求失败")).when(platformStrategy).chat(any(AppAiChatBO.class));

        AppAiChatBO bo = new AppAiChatBO();
        bo.setTopic("计算 (-2) + (-3) 的结果是 ( )");
        bo.setMessage("请帮我讲解这道题目：计算 (-2) + (-3) 的结果是 ( )");

        AppAiChatResultVO result = aiChatService.chat(1L, bo);

        assertNotNull(result.getReply());
        assertTrue(result.getReply().contains("考点"));
        assertEquals(1, result.getQuota().getAiUsedToday());
    }
}