package com.sqx.app.base.ocr.service;

import com.sqx.app.base.ocr.domain.bo.AppOcrSearchBO;
import com.sqx.app.base.ocr.domain.vo.AppOcrSearchResultVO;

/**
 * OCR 搜题服务
 */
public interface OcrSearchService {

    AppOcrSearchResultVO search(Long userId, AppOcrSearchBO searchBO);
}
