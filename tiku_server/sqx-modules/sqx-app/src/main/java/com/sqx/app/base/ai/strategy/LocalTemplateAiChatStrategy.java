package com.sqx.app.base.ai.strategy;

import com.sqx.app.base.ai.domain.bo.AppAiChatBO;
import com.sqx.app.base.ai.support.AiChatReplySupport;

/**
 * 本地模板 AI：不调用外部 LLM，用于开发/测试
 */
public class LocalTemplateAiChatStrategy implements IAiChatPlatformStrategy {

    @Override
    public String chat(AppAiChatBO chatBO) {
        return AiChatReplySupport.buildReply(chatBO);
    }
}
