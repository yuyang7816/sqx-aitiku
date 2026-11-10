package com.sqx.app.base.ocr.support;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 题干相似度评分单元测试
 */
class QuestionSimilaritySupportTest {

    @Test
    void score_whenQueryContainedInTitle_shouldReturnOne() {
        double score = QuestionSimilaritySupport.score("负数", "下列各数中，属于负数的是（ ）");
        assertEquals(1D, score);
    }

    @Test
    void score_whenPartialOverlap_shouldReturnBetweenZeroAndOne() {
        double score = QuestionSimilaritySupport.score("计算 (-2) + (-3)", "计算 (-2) + (-3) 的结果是（ ）");
        assertTrue(score >= 0.5D);
    }

    @Test
    void keywordForLike_shouldTrimAndNormalize() {
        String keyword = QuestionSimilaritySupport.keywordForLike("  下列各数中，属于负数的是（ ）  ");
        assertTrue(keyword.contains("下列各数中"));
    }
}
