package com.sqx.common.core.utils;

import org.apache.commons.lang3.StringUtils;

/**
 * 检测乱码/占位文本（如仅由 ?、？ 组成），避免在界面展示无效数据。
 */
public final class TextGarbledUtils {

    private static final String GARBLED_PATTERN = "^[?？]+$";

    private TextGarbledUtils() {
    }

    public static boolean isGarbledText(String text) {
        if (StringUtils.isBlank(text)) {
            return true;
        }
        return text.trim().matches(GARBLED_PATTERN);
    }

    public static boolean hasGarbledText(String... texts) {
        if (texts == null) {
            return false;
        }
        for (String text : texts) {
            if (isGarbledText(text)) {
                return true;
            }
        }
        return false;
    }
}
