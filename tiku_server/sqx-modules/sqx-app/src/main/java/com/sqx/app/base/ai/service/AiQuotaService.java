package com.sqx.app.base.ai.service;

import com.sqx.app.base.ai.domain.vo.AppAiQuotaVO;

/**
 * AI 每日次数配额
 */
public interface AiQuotaService {

    /**
     * 校验并扣减一次 AI 答疑次数（VIP 不扣减）
     */
    AppAiQuotaVO consumeChatQuota(Long userId);
}
