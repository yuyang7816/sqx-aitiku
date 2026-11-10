package com.sqx.app.base.ocr.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.sqx.app.base.ocr.domain.bo.AppOcrSearchBO;
import com.sqx.app.base.ocr.domain.vo.AppOcrMatchItemVO;
import com.sqx.app.base.ocr.domain.vo.AppOcrQuotaVO;
import com.sqx.app.base.ocr.domain.vo.AppOcrSearchResultVO;
import com.sqx.app.base.ocr.enums.OcrError;
import com.sqx.app.base.ocr.factory.OcrFactory;
import com.sqx.app.base.ocr.service.OcrQuotaService;
import com.sqx.app.base.ocr.service.OcrSearchService;
import com.sqx.app.base.ocr.strategy.IOcrPlatformStrategy;
import com.sqx.app.base.ocr.support.OcrConfigReader;
import com.sqx.app.base.ocr.support.OcrTipsSupport;
import com.sqx.app.base.ocr.support.QuestionSimilaritySupport;
import com.sqx.common.core.exception.BizException;
import com.sqx.system.db.entity.Question;
import com.sqx.system.db.service.IQuestionService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * OCR 搜题：文本或图片 OCR 后匹配题库并按相似度排序
 */
@Service
public class OcrSearchServiceImpl implements OcrSearchService {

    private static final int STATE_ENABLED = 1;

    @Resource
    private IQuestionService iQuestionService;

    @Resource
    private OcrConfigReader ocrConfigReader;

    @Resource
    private OcrFactory ocrFactory;

    @Resource
    private OcrQuotaService ocrQuotaService;

    @Override
    public AppOcrSearchResultVO search(Long userId, AppOcrSearchBO searchBO) {
        validateInput(searchBO);
        AppOcrQuotaVO quota = ocrQuotaService.consumeSearchQuota(userId);
        String recognizedText = resolveRecognizedText(searchBO);
        List<AppOcrMatchItemVO> matches = matchQuestions(recognizedText);
        AppOcrSearchResultVO result = new AppOcrSearchResultVO();
        result.setRecognizedText(recognizedText);
        result.setMatches(matches);
        result.setQuota(quota);
        return result;
    }

    void validateInput(AppOcrSearchBO searchBO) {
        if (searchBO == null) {
            throw new BizException(OcrError.SEARCH_INPUT_EMPTY);
        }
        if (StringUtils.isBlank(searchBO.getText()) && StringUtils.isBlank(searchBO.getImageUrl())) {
            throw new BizException(OcrError.SEARCH_INPUT_EMPTY);
        }
    }

    String resolveRecognizedText(AppOcrSearchBO searchBO) {
        if (StringUtils.isNotBlank(searchBO.getText())) {
            return searchBO.getText().trim();
        }
        IOcrPlatformStrategy platform = ocrFactory.getPlatform();
        String text = platform.recognizeText(searchBO.getImageUrl().trim());
        if (StringUtils.isBlank(text)) {
            throw new BizException(OcrError.RECOGNITION_FAILED);
        }
        return text.trim();
    }

    List<AppOcrMatchItemVO> matchQuestions(String recognizedText) {
        List<Question> candidates = loadCandidateQuestions(recognizedText);
        if (candidates.isEmpty()) {
            return Collections.emptyList();
        }
        double threshold = ocrConfigReader.getSimilarityThreshold();
        int maxResults = ocrConfigReader.getMaxResults();
        List<AppOcrMatchItemVO> scored = new ArrayList<>();
        for (Question question : candidates) {
            double similarity = QuestionSimilaritySupport.score(recognizedText, question.getTitle());
            if (similarity < threshold) {
                continue;
            }
            scored.add(toMatchItem(question, similarity));
        }
        scored.sort(Comparator.comparing(AppOcrMatchItemVO::getSimilarity).reversed());
        if (scored.size() <= maxResults) {
            return scored;
        }
        return scored.subList(0, maxResults);
    }

    List<Question> loadCandidateQuestions(String recognizedText) {
        String keyword = QuestionSimilaritySupport.keywordForLike(recognizedText);
        QueryWrapper<Question> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(Question::getState, STATE_ENABLED)
                .like(StringUtils.isNotBlank(keyword), Question::getTitle, keyword)
                .orderByAsc(Question::getSortOrder);
        List<Question> matched = iQuestionService.list(queryWrapper);
        if (!matched.isEmpty()) {
            return matched;
        }
        QueryWrapper<Question> fallback = new QueryWrapper<>();
        fallback.lambda()
                .eq(Question::getState, STATE_ENABLED)
                .orderByAsc(Question::getSortOrder);
        return iQuestionService.list(fallback);
    }

    private AppOcrMatchItemVO toMatchItem(Question question, double similarity) {
        AppOcrMatchItemVO vo = new AppOcrMatchItemVO();
        vo.setQuestionId(question.getId());
        vo.setQuestionCode(question.getQuestionCode());
        vo.setTitle(question.getTitle());
        vo.setAnswer(question.getRightAnswer());
        vo.setAnalysis(StringUtils.defaultString(question.getAnalysis()));
        vo.setTips(OcrTipsSupport.buildTips(question.getAnalysis(), question.getTagsJson()));
        vo.setSimilarity(roundSimilarity(similarity));
        return vo;
    }

    private double roundSimilarity(double similarity) {
        return Math.round(similarity * 1000D) / 1000D;
    }
}
