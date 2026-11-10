package com.sqx.app.base.practice.support;

import com.alibaba.fastjson.JSON;
import com.sqx.app.base.practice.domain.bo.AppPracticeDraftAnswerBO;
import com.sqx.app.base.practice.domain.bo.AppPracticeDraftQueryBO;
import com.sqx.app.base.practice.domain.bo.AppPracticeDraftSaveBO;
import com.sqx.app.base.question.list.constants.QuestionListMode;
import com.sqx.common.core.exception.BizException;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 练习答题暂存：范围键与答案 JSON 解析
 */
public final class PracticeDraftSupport {

    private PracticeDraftSupport() {
    }

    /**
     * 根据章/题集/单题/来源模式生成唯一 scope_key
     */
    public static String resolveScopeKey(String chapterId, String paperId, String questionId, String sourceMode) {
        String chapterCode = StringUtils.trimToEmpty(chapterId);
        String paperCode = StringUtils.trimToEmpty(paperId);
        String questionCode = StringUtils.trimToEmpty(questionId);
        String mode = normalizeSourceMode(sourceMode);

        if (StringUtils.isNotBlank(paperCode)) {
            return "paper:" + paperCode + ":" + mode;
        }
        if (QuestionListMode.SINGLE.equals(mode) && StringUtils.isNotBlank(questionCode)) {
            return "single:" + questionCode;
        }
        if (StringUtils.isNotBlank(chapterCode)) {
            return "chapter:" + chapterCode + ":" + mode;
        }
        throw new BizException("请提供 chapterId、paperId 或 questionId");
    }

    public static String resolveScopeKey(AppPracticeDraftSaveBO save) {
        return resolveScopeKey(save.getChapterId(), save.getPaperId(), save.getQuestionId(), save.getSourceMode());
    }

    public static String resolveScopeKey(AppPracticeDraftQueryBO query) {
        return resolveScopeKey(query.getChapterId(), query.getPaperId(), query.getQuestionId(), query.getSourceMode());
    }

    public static String normalizeSourceMode(String sourceMode) {
        String value = StringUtils.trimToEmpty(sourceMode);
        if (QuestionListMode.VALID_MODES.contains(value)) {
            return value;
        }
        return QuestionListMode.ALL;
    }

    public static Map<String, AppPracticeDraftAnswerBO> dedupAnswers(List<AppPracticeDraftAnswerBO> answers) {
        Map<String, AppPracticeDraftAnswerBO> dedup = new LinkedHashMap<>();
        if (answers == null) {
            return dedup;
        }
        for (AppPracticeDraftAnswerBO item : answers) {
            if (item == null || StringUtils.isBlank(item.getQuestionCode())) {
                continue;
            }
            dedup.put(StringUtils.trim(item.getQuestionCode()), item);
        }
        return dedup;
    }

    public static List<AppPracticeDraftAnswerBO> filterStoredAnswers(Map<String, AppPracticeDraftAnswerBO> dedup) {
        if (dedup == null || dedup.isEmpty()) {
            return Collections.emptyList();
        }
        return dedup.values().stream()
                .filter(item -> StringUtils.isNotBlank(StringUtils.trimToEmpty(item.getUserAnswer())))
                .collect(java.util.stream.Collectors.toList());
    }

    public static String serializeAnswers(List<AppPracticeDraftAnswerBO> answers) {
        List<AppPracticeDraftAnswerBO> safeList = answers == null ? Collections.emptyList() : answers;
        return JSON.toJSONString(safeList);
    }

    public static List<AppPracticeDraftAnswerBO> parseAnswers(String answersJson) {
        if (StringUtils.isBlank(answersJson)) {
            return Collections.emptyList();
        }
        try {
            List<AppPracticeDraftAnswerBO> answers = JSON.parseArray(answersJson, AppPracticeDraftAnswerBO.class);
            return answers == null ? Collections.emptyList() : answers;
        } catch (Exception ignored) {
            return Collections.emptyList();
        }
    }
}
