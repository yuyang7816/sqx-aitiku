package com.sqx.app.base.ocr.strategy;

/**
 * OCR 文字识别策略
 */
public interface IOcrPlatformStrategy {

    /**
     * 从图片 URL 识别文本
     */
    String recognizeText(String imageUrl);
}
