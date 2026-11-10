package com.sqx.app.base.ai.strategy;

import com.sqx.app.base.ai.domain.bo.AppAiChatBO;

/**
 * AI 答疑供应商策略
 */
public interface IAiChatPlatformStrategy {

    /**
     * 根据题目上下文、当前消息与历史生成 AI 回复
     */
    String chat(AppAiChatBO chatBO);
}
