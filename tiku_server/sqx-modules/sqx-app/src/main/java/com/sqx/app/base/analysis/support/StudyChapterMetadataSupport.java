package com.sqx.app.base.analysis.support;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sqx.system.db.entity.QuestionChapter;
import com.sqx.system.db.entity.UserPracticeRecord;
import com.sqx.system.db.service.IQuestionChapterService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 练习记录展示字段：优先从 tb_question_chapter 解析学科/章标题，避免快照乱码
 */
@Component
public class StudyChapterMetadataSupport {

    private static final String DEFAULT_SUBJECT = "综合练习";
    private static final String DEFAULT_CHAPTER_TITLE = "未命名章节";

    @Resource
    private IQuestionChapterService iQuestionChapterService;

    public Map<String, QuestionChapter> loadChapterMap(Collection<UserPracticeRecord> records) {
        if (CollectionUtils.isEmpty(records)) {
            return Collections.emptyMap();
        }
        List<String> chapterCodes = records.stream()
                .map(UserPracticeRecord::getChapterCode)
                .filter(StringUtils::isNotBlank)
                .map(StringUtils::trim)
                .distinct()
                .collect(Collectors.toList());
        if (chapterCodes.isEmpty()) {
            return Collections.emptyMap();
        }

        LambdaQueryWrapper<QuestionChapter> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(QuestionChapter::getChapterCode, chapterCodes);
        List<QuestionChapter> chapters = iQuestionChapterService.list(wrapper);
        if (CollectionUtils.isEmpty(chapters)) {
            return Collections.emptyMap();
        }

        Map<String, QuestionChapter> chapterMap = new HashMap<>();
        for (QuestionChapter chapter : chapters) {
            if (chapter != null && StringUtils.isNotBlank(chapter.getChapterCode())) {
                chapterMap.putIfAbsent(StringUtils.trim(chapter.getChapterCode()), chapter);
            }
        }
        return chapterMap;
    }

    public String resolveSubject(UserPracticeRecord record, Map<String, QuestionChapter> chapterMap) {
        QuestionChapter chapter = findChapter(record, chapterMap);
        if (chapter != null && StringUtils.isNotBlank(chapter.getSubject())) {
            return StringUtils.trim(chapter.getSubject());
        }
        return normalizeSubject(record == null ? null : record.getSubject());
    }

    public String resolveChapterTitle(UserPracticeRecord record, Map<String, QuestionChapter> chapterMap) {
        QuestionChapter chapter = findChapter(record, chapterMap);
        if (chapter != null && StringUtils.isNotBlank(chapter.getTitle())) {
            return StringUtils.trim(chapter.getTitle());
        }
        return normalizeChapterTitle(record == null ? null : record.getChapterTitle());
    }

    public String normalizeChapterCode(UserPracticeRecord record) {
        if (record != null && StringUtils.isNotBlank(record.getChapterCode())) {
            return StringUtils.trim(record.getChapterCode());
        }
        if (record != null && StringUtils.isNotBlank(record.getChapterTitle())) {
            return "chapter_" + StringUtils.trim(record.getChapterTitle());
        }
        return "chapter_unknown";
    }

    private QuestionChapter findChapter(UserPracticeRecord record, Map<String, QuestionChapter> chapterMap) {
        if (record == null || chapterMap == null || chapterMap.isEmpty()) {
            return null;
        }
        String chapterCode = StringUtils.trimToNull(record.getChapterCode());
        if (chapterCode == null) {
            return null;
        }
        return chapterMap.get(chapterCode);
    }

    private String normalizeSubject(String subject) {
        if (StringUtils.isBlank(subject) || isGarbledText(subject)) {
            return DEFAULT_SUBJECT;
        }
        return StringUtils.trim(subject);
    }

    private String normalizeChapterTitle(String chapterTitle) {
        if (StringUtils.isBlank(chapterTitle) || isGarbledText(chapterTitle)) {
            return DEFAULT_CHAPTER_TITLE;
        }
        return StringUtils.trim(chapterTitle);
    }

    /** 乱码快照常见形态：纯问号或不含任何中文/字母 */
    static boolean isGarbledText(String text) {
        String normalized = StringUtils.trimToEmpty(text);
        if (normalized.isEmpty()) {
            return true;
        }
        if (normalized.chars().allMatch(ch -> ch == '?')) {
            return true;
        }
        return !normalized.matches(".*[\\u4e00-\\u9fffA-Za-z0-9].*");
    }

    static boolean hasReadableText(String text) {
        return !isGarbledText(text);
    }
}
