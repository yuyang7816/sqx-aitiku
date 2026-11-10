package com.sqx.app.base.ai.service;

import com.sqx.app.base.ai.domain.bo.AppAiChatBO;
import com.sqx.app.base.ai.domain.vo.AppAiChatResultVO;

/**
 * AI 答疑对话
 */
public interface AiChatService {

    AppAiChatResultVO chat(Long userId, AppAiChatBO chatBO);
}
