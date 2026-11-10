package com.sqx.app.base.ocr.support;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * OCR tips 生成单元测试
 */
class OcrTipsSupportTest {

    @Test
    void buildTips_shouldSplitAnalysisIntoSentences() {
        List<String> tips = OcrTipsSupport.buildTips("先判断符号。再比较绝对值。最后选答案。", "[\"有理数\"]");
        assertFalse(tips.isEmpty());
        assertTrue(tips.get(0).startsWith("1."));
    }
}
