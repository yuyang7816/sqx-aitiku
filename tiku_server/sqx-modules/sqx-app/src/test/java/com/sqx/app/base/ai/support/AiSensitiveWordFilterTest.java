package com.sqx.app.base.ai.support;

import com.sqx.app.base.ai.domain.bo.AppAiChatBO;
import com.sqx.app.base.ai.domain.bo.AppAiChatHistoryItemBO;
import com.sqx.app.base.ai.enums.AiError;
import com.sqx.common.core.exception.BizException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiSensitiveWordFilterTest {

    @Mock
    private AiConfigReader aiConfigReader;

    @InjectMocks
    private AiSensitiveWordFilter aiSensitiveWordFilter;

    @BeforeEach
    void setUp() {
        when(aiConfigReader.getSensitiveWords()).thenReturn(Arrays.asList("\u8d4c\u535a", "\u8272\u60c5", "badword"));
    }

    @Test
    void containsSensitiveWord_whenHitInMessage_shouldReturnTrue() {
        assertTrue(aiSensitiveWordFilter.containsSensitiveWord("\u8bf7\u95ee\u8d4c\u535a\u76f8\u5173\u9898\u76ee\u600e\u4e48\u89e3"));
    }

    @Test
    void containsSensitiveWord_whenCaseInsensitiveLatin_shouldReturnTrue() {
        assertTrue(aiSensitiveWordFilter.containsSensitiveWord("This is BADWORD content"));
    }

    @Test
    void containsSensitiveWord_whenCleanText_shouldReturnFalse() {
        assertFalse(aiSensitiveWordFilter.containsSensitiveWord("\u8bf7\u5e2e\u6211\u8bb2\u89e3\u65b9\u7a0b x + 3 = 2"));
    }

    @Test
    void validateChatInput_whenMessageSensitive_shouldThrow() {
        AppAiChatBO bo = new AppAiChatBO();
        bo.setMessage("\u8fd9\u91cc\u6d89\u53ca\u8272\u60c5\u5185\u5bb9");

        BizException ex = assertThrows(BizException.class, () -> aiSensitiveWordFilter.validateChatInput(bo));
        assertEquals(AiError.SENSITIVE_INPUT.getCode(), ex.getErrorCode().getCode());
    }

    @Test
    void validateChatInput_whenHistorySensitive_shouldThrow() {
        AppAiChatHistoryItemBO historyItem = new AppAiChatHistoryItemBO();
        historyItem.setRole("user");
        historyItem.setText("\u4e4b\u524d\u95ee\u8fc7\u8d4c\u535a\u95ee\u9898");

        AppAiChatBO bo = new AppAiChatBO();
        bo.setMessage("\u7ee7\u7eed\u8ffd\u95ee");
        bo.setHistory(Collections.singletonList(historyItem));

        BizException ex = assertThrows(BizException.class, () -> aiSensitiveWordFilter.validateChatInput(bo));
        assertEquals(AiError.SENSITIVE_INPUT.getCode(), ex.getErrorCode().getCode());
    }

    @Test
    void validateReply_whenSensitive_shouldThrow() {
        BizException ex = assertThrows(BizException.class,
                () -> aiSensitiveWordFilter.validateReply("\u56de\u590d\u91cc\u51fa\u73b0\u4e86\u8d4c\u535a\u5b57\u6837"));
        assertEquals(AiError.SENSITIVE_REPLY.getCode(), ex.getErrorCode().getCode());
    }

    @Test
    void validateReply_whenClean_shouldPass() {
        assertDoesNotThrow(() -> aiSensitiveWordFilter.validateReply("\u8fd9\u9053\u65b9\u7a0b\u5148\u79fb\u9879\u518d\u5408\u5e76\u540c\u7c7b\u9879\u5373\u53ef\u3002"));
    }
}
