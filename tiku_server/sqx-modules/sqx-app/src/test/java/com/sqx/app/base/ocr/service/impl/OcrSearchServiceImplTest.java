package com.sqx.app.base.ocr.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.sqx.app.base.ocr.domain.bo.AppOcrSearchBO;
import com.sqx.app.base.ocr.domain.vo.AppOcrMatchItemVO;
import com.sqx.app.base.ocr.domain.vo.AppOcrQuotaVO;
import com.sqx.app.base.ocr.domain.vo.AppOcrSearchResultVO;
import com.sqx.app.base.ocr.enums.OcrError;
import com.sqx.app.base.ocr.factory.OcrFactory;
import com.sqx.app.base.ocr.service.OcrQuotaService;
import com.sqx.app.base.ocr.strategy.IOcrPlatformStrategy;
import com.sqx.app.base.ocr.support.OcrConfigReader;
import com.sqx.common.core.exception.BizException;
import com.sqx.system.db.entity.Question;
import com.sqx.system.db.service.IQuestionService;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * POST /app/ocr/search 单元测试
 */
@ExtendWith(MockitoExtension.class)
class OcrSearchServiceImplTest {

    @Mock
    private IQuestionService iQuestionService;

    @Mock
    private OcrConfigReader ocrConfigReader;

    @Mock
    private OcrFactory ocrFactory;

    @Mock
    private OcrQuotaService ocrQuotaService;

    @Mock
    private IOcrPlatformStrategy ocrPlatformStrategy;

    @InjectMocks
    private OcrSearchServiceImpl ocrSearchService;

    private AppOcrQuotaVO defaultQuota;

    @BeforeEach
    void initMybatisPlusEntityCache() {
        Configuration configuration = new Configuration();
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(configuration, "test");
        TableInfoHelper.initTableInfo(assistant, Question.class);
        defaultQuota = new AppOcrQuotaVO();
        defaultQuota.setOcrDailyLimit(5);
        defaultQuota.setOcrUsedToday(1);
        defaultQuota.setOcrUnlimited(false);
    }

    @Test
    void validateInput_whenEmpty_shouldThrow() {
        AppOcrSearchBO bo = new AppOcrSearchBO();
        BizException ex = assertThrows(BizException.class, () -> ocrSearchService.validateInput(bo));
        assertEquals(OcrError.SEARCH_INPUT_EMPTY.getCode(), ex.getErrorCode().getCode());
    }

    @Test
    void search_byText_shouldReturnSortedMatches() {
        when(ocrQuotaService.consumeSearchQuota(1L)).thenReturn(defaultQuota);
        when(ocrConfigReader.getSimilarityThreshold()).thenReturn(0.08D);
        when(ocrConfigReader.getMaxResults()).thenReturn(10);
        Question q1 = buildQuestion(101L, "rational-numbers-q01", "下列各数中，属于负数的是（ ）", "A");
        Question q2 = buildQuestion(102L, "rational-numbers-q02", "计算 (-2) + (-3) 的结果是（ ）", "B");
        when(iQuestionService.list(any(Wrapper.class))).thenReturn(Arrays.asList(q1, q2));

        AppOcrSearchBO bo = new AppOcrSearchBO();
        bo.setText("下列各数中，属于负数的是");
        AppOcrSearchResultVO result = ocrSearchService.search(1L, bo);

        assertEquals("下列各数中，属于负数的是", result.getRecognizedText());
        assertFalse(result.getMatches().isEmpty());
        AppOcrMatchItemVO top = result.getMatches().get(0);
        assertEquals("rational-numbers-q01", top.getQuestionCode());
        assertEquals("A", top.getAnswer());
        assertNotNull(top.getTips());
        assertEquals(5, result.getQuota().getOcrDailyLimit());
        assertEquals(1, result.getQuota().getOcrUsedToday());
        verify(ocrFactory, never()).getPlatform();
    }

    @Test
    void search_whenQuotaExceeded_shouldThrowBeforeSearch() {
        when(ocrQuotaService.consumeSearchQuota(1L)).thenThrow(new BizException(OcrError.QUOTA_EXCEEDED));

        AppOcrSearchBO bo = new AppOcrSearchBO();
        bo.setText("测试题干");
        BizException ex = assertThrows(BizException.class, () -> ocrSearchService.search(1L, bo));
        assertEquals(OcrError.QUOTA_EXCEEDED.getCode(), ex.getErrorCode().getCode());
        verify(iQuestionService, never()).list(any(Wrapper.class));
    }

    @Test
    void search_byImageUrl_shouldUseOcrStrategy() {
        when(ocrQuotaService.consumeSearchQuota(1L)).thenReturn(defaultQuota);
        when(ocrFactory.getPlatform()).thenReturn(ocrPlatformStrategy);
        when(ocrPlatformStrategy.recognizeText("http://localhost/upload/a.png"))
                .thenReturn("计算 (-2) + (-3) 的结果是");
        when(ocrConfigReader.getSimilarityThreshold()).thenReturn(0.08D);
        when(ocrConfigReader.getMaxResults()).thenReturn(10);
        Question q2 = buildQuestion(102L, "rational-numbers-q02", "计算 (-2) + (-3) 的结果是（ ）", "B");
        when(iQuestionService.list(any(Wrapper.class))).thenReturn(Collections.singletonList(q2));

        AppOcrSearchBO bo = new AppOcrSearchBO();
        bo.setImageUrl("http://localhost/upload/a.png");
        AppOcrSearchResultVO result = ocrSearchService.search(1L, bo);

        assertTrue(result.getRecognizedText().contains("计算"));
        assertEquals(1, result.getMatches().size());
        assertEquals("B", result.getMatches().get(0).getAnswer());
        verify(ocrFactory).getPlatform();
    }

    @Test
    void search_whenTextProvided_shouldPreferTextOverImage() {
        when(ocrQuotaService.consumeSearchQuota(1L)).thenReturn(defaultQuota);
        when(ocrConfigReader.getSimilarityThreshold()).thenReturn(0.08D);
        when(ocrConfigReader.getMaxResults()).thenReturn(10);
        Question q1 = buildQuestion(101L, "rational-numbers-q01", "下列各数中，属于负数的是（ ）", "A");
        when(iQuestionService.list(any(Wrapper.class))).thenReturn(Collections.singletonList(q1));

        AppOcrSearchBO bo = new AppOcrSearchBO();
        bo.setText("属于负数");
        bo.setImageUrl("http://localhost/upload/a.png");
        AppOcrSearchResultVO result = ocrSearchService.search(1L, bo);

        assertEquals("属于负数", result.getRecognizedText());
        verify(ocrFactory, never()).getPlatform();
    }

    private Question buildQuestion(Long id, String code, String title, String answer) {
        Question question = new Question();
        question.setId(id);
        question.setQuestionCode(code);
        question.setTitle(title);
        question.setRightAnswer(answer);
        question.setAnalysis("解析：" + title);
        question.setTagsJson("[\"有理数\"]");
        question.setState(1);
        question.setSortOrder(1);
        return question;
    }
}
