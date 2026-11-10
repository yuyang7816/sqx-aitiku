package com.sqx.app.base.ai.support;

import com.sqx.app.base.ai.domain.bo.AppAiChatBO;
import com.sqx.app.base.ai.domain.bo.AppAiChatHistoryItemBO;
import com.sqx.app.base.ai.enums.AiError;
import com.sqx.common.core.exception.BizException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * AI 对话敏感词过滤（入参 topic/message/history 与 AI 回复）
 */
@Component
public class AiSensitiveWordFilter {

    @Resource
    private AiConfigReader aiConfigReader;

    /**
     * 校验用户输入是否含敏感词，违规抛 BizException
     */
    public void validateChatInput(AppAiChatBO chatBO) {
        if (chatBO == null) {
            return;
        }
        if (containsSensitiveWord(chatBO.getTopic()) || containsSensitiveWord(chatBO.getMessage())) {
            throw new BizException(AiError.SENSITIVE_INPUT);
        }
        List<AppAiChatHistoryItemBO> history = chatBO.getHistory() == null
                ? Collections.emptyList()
                : chatBO.getHistory();
        for (AppAiChatHistoryItemBO item : history) {
            if (containsSensitiveWord(AiChatReplySupport.resolveHistoryText(item))) {
                throw new BizException(AiError.SENSITIVE_INPUT);
            }
        }
    }

    /**
     * 校验 AI 回复是否含敏感词，违规抛 BizException
     */
    public void validateReply(String reply) {
        if (containsSensitiveWord(reply)) {
            throw new BizException(AiError.SENSITIVE_REPLY);
        }
    }

    boolean containsSensitiveWord(String text) {
        if (StringUtils.isBlank(text)) {
            return false;
        }
        String normalized = normalize(text);
        for (String word : aiConfigReader.getSensitiveWords()) {
            if (StringUtils.isBlank(word)) {
                continue;
            }
            if (normalized.contains(normalize(word))) {
                return true;
            }
        }
        return false;
    }

    private static String normalize(String text) {
        return text.trim().toLowerCase(Locale.ROOT);
    }
}
