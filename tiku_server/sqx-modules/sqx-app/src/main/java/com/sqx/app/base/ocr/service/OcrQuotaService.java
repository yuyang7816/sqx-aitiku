package com.sqx.app.base.ocr.service;

import com.sqx.app.base.ocr.domain.vo.AppOcrQuotaVO;

/**
 * OCR 每日次数配额：搜题前校验、扣减与跨日重置
 */
public interface OcrQuotaService {

    /**
     * 校验并扣减 1 次 OCR 搜题配额（VIP 无限次不扣减）
     *
     * @param userId 当前登录用户 ID
     * @return 扣减后的配额快照
     */
    AppOcrQuotaVO consumeSearchQuota(Long userId);
}
