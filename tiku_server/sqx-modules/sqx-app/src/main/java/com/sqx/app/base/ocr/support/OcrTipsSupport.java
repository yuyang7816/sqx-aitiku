package com.sqx.app.base.ocr.support;

import com.sqx.app.base.question.list.support.QuestionTagsSupport;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 从解析与标签生成 OCR 结果页 tips
 */
public final class OcrTipsSupport {

    private static final int MAX_TIPS = 3;

    private OcrTipsSupport() {
    }

    public static List<String> buildTips(String analysis, String tagsJson) {
        List<String> tips = new ArrayList<>();
        List<String> tags = QuestionTagsSupport.parseTags(tagsJson);
        for (String tag : tags) {
            if (tips.size() >= MAX_TIPS) {
                break;
            }
            if (StringUtils.isNotBlank(tag)) {
                tips.add((tips.size() + 1) + ". " + tag.trim());
            }
        }
        if (tips.size() >= MAX_TIPS || StringUtils.isBlank(analysis)) {
            return tips.isEmpty() ? defaultTips() : tips;
        }
        String[] parts = analysis.split("[。；;.\\n]+");
        for (String part : parts) {
            if (tips.size() >= MAX_TIPS) {
                break;
            }
            String trimmed = part.trim();
            if (trimmed.length() >= 6) {
                tips.add((tips.size() + 1) + ". " + trimmed);
            }
        }
        if (tips.isEmpty()) {
            return defaultTips();
        }
        return tips;
    }

    private static List<String> defaultTips() {
        return Collections.singletonList("1. 先读题干关键词，再对照选项或计算步骤。");
    }
}
