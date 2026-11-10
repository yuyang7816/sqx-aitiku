package com.sqx.app.base.ocr.support;

import org.apache.commons.lang3.StringUtils;

import java.util.HashSet;
import java.util.Set;

/**
 * 题干相似度评分（字符 bigram + 包含关系）
 */
public final class QuestionSimilaritySupport {

    private QuestionSimilaritySupport() {
    }

    public static double score(String query, String title) {
        if (StringUtils.isBlank(query) || StringUtils.isBlank(title)) {
            return 0D;
        }
        String q = normalize(query);
        String t = normalize(title);
        if (StringUtils.isBlank(q) || StringUtils.isBlank(t)) {
            return 0D;
        }
        if (t.contains(q) || q.contains(t)) {
            return 1D;
        }
        Set<String> qGrams = bigrams(q);
        Set<String> tGrams = bigrams(t);
        if (qGrams.isEmpty() || tGrams.isEmpty()) {
            return 0D;
        }
        int intersection = 0;
        for (String gram : qGrams) {
            if (tGrams.contains(gram)) {
                intersection++;
            }
        }
        int union = qGrams.size() + tGrams.size() - intersection;
        return union <= 0 ? 0D : (double) intersection / union;
    }

    /**
     * 提取用于 SQL LIKE 的关键词片段
     */
    public static String keywordForLike(String text) {
        String normalized = normalize(text);
        if (StringUtils.isBlank(normalized)) {
            return null;
        }
        int maxLen = Math.min(30, normalized.length());
        return normalized.substring(0, maxLen);
    }

    private static String normalize(String text) {
        return text.replaceAll("\\s+", "")
                .replaceAll("[\\p{Punct}\\p{IsPunctuation}]", "")
                .toLowerCase();
    }

    private static Set<String> bigrams(String text) {
        Set<String> grams = new HashSet<>();
        if (text.length() == 1) {
            grams.add(text);
            return grams;
        }
        for (int i = 0; i < text.length() - 1; i++) {
            grams.add(text.substring(i, i + 2));
        }
        return grams;
    }
}
