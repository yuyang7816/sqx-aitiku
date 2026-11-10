package com.sqx.app.base.notebook.support;

import com.sqx.app.base.notebook.domain.vo.AppNotebookItemVO;
import com.sqx.app.base.question.chapter.constants.QuestionChapterConstant;
import com.sqx.app.base.question.list.support.QuestionTagsSupport;
import com.sqx.app.base.question.paper.support.QuestionOptionsSupport;
import com.sqx.app.base.setup.support.SetupQueryResolveSupport;
import com.sqx.system.db.entity.Question;
import com.sqx.system.db.entity.QuestionChapter;
import com.sqx.system.db.entity.UserQuestionWrong;
import com.sqx.system.db.service.IQuestionChapterService;
import com.sqx.system.db.service.IQuestionService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 错题本列表项组装（用户端与管理端共用）
 */
@Component
public class NotebookItemAssembler {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String DEFAULT_AI_SUMMARY = "暂无 AI 讲解";
    private static final String DEFAULT_TAG = "#错题回顾";
    private static final String DEFAULT_SUBJECT = "章节练习";
    private static final String WRONG_REDO_SUBJECT = "错题重做";
    private static final String GENERIC_SUBJECT = "通用题目";

    @Resource
    private IQuestionService iQuestionService;

    @Resource
    private IQuestionChapterService iQuestionChapterService;

    public Map<Long, Question> loadQuestionMap(List<UserQuestionWrong> wrongList, boolean enabledOnly) {
        List<Long> questionIds = wrongList.stream()
                .map(UserQuestionWrong::getQuestionId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (questionIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return iQuestionService.listByIds(questionIds).stream()
                .filter(question -> question != null && question.getId() != null)
                .filter(question -> !enabledOnly || isEnabledQuestion(question))
                .collect(Collectors.toMap(Question::getId, Function.identity(), (a, b) -> a, LinkedHashMap::new));
    }

    public Map<Long, String> loadChapterSubjectMap(Collection<Question> questions) {
        if (questions == null || questions.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Long> chapterIds = questions.stream()
                .map(Question::getChapterId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (chapterIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return iQuestionChapterService.listByIds(chapterIds).stream()
                .filter(chapter -> chapter != null && chapter.getId() != null)
                .filter(chapter -> StringUtils.isNotBlank(chapter.getSubject()))
                .collect(Collectors.toMap(
                        QuestionChapter::getId,
                        chapter -> normalizeSubjectLabel(StringUtils.trim(chapter.getSubject())),
                        (a, b) -> a,
                        LinkedHashMap::new
                ));
    }

    public AppNotebookItemVO toItemVO(UserQuestionWrong wrong, Question question) {
        return toItemVO(wrong, question, Collections.emptyMap());
    }

    public AppNotebookItemVO toItemVO(UserQuestionWrong wrong, Question question, Map<Long, String> chapterSubjectMap) {
        AppNotebookItemVO vo = new AppNotebookItemVO();
        vo.setNotebookId(wrong.getId());
        vo.setSubject(resolveSubject(wrong, question, chapterSubjectMap));
        vo.setWrongCount(Math.max(1, wrong.getWrongCount() == null ? 1 : wrong.getWrongCount()));
        vo.setMastered(Integer.valueOf(UserQuestionWrong.MASTERED_YES).equals(wrong.getMastered()));

        String userAnswer = StringUtils.defaultString(wrong.getUserAnswer());
        vo.setMyAnswer(userAnswer);
        vo.setWrong(userAnswer);

        if (question != null) {
            vo.setId(StringUtils.defaultIfBlank(question.getQuestionCode(),
                    question.getId() == null ? String.valueOf(wrong.getQuestionId()) : String.valueOf(question.getId())));
            vo.setQuestionId(question.getId());
            vo.setQuestionCode(question.getQuestionCode());

            String title = StringUtils.defaultIfBlank(question.getTitle(), "题目已删除或不可用");
            vo.setTitle(title);
            vo.setQuestion(title);

            String rightAnswer = StringUtils.defaultString(question.getRightAnswer());
            vo.setRightAnswer(rightAnswer);
            vo.setRight(rightAnswer);

            vo.setAnalysis(StringUtils.defaultIfBlank(question.getAnalysis(), "暂无解析"));
            vo.setAiSummary(DEFAULT_AI_SUMMARY);
            vo.setOptions(QuestionOptionsSupport.parseOptions(question.getOptionsJson()));
            vo.setTags(resolveTags(question.getTagsJson()));
        } else {
            String fallbackId = wrong.getQuestionId() == null ? String.valueOf(wrong.getId()) : String.valueOf(wrong.getQuestionId());
            vo.setId(fallbackId);
            vo.setQuestionId(wrong.getQuestionId());
            vo.setQuestionCode(fallbackId);
            vo.setTitle("题目已删除或不可用");
            vo.setQuestion(vo.getTitle());
            vo.setRightAnswer("");
            vo.setRight("");
            vo.setAnalysis("暂无解析");
            vo.setAiSummary(DEFAULT_AI_SUMMARY);
            vo.setOptions(Collections.emptyList());
            vo.setTags(Collections.singletonList(DEFAULT_TAG));
        }

        LocalDateTime practiceAt = wrong.getLastPracticeAt() != null ? wrong.getLastPracticeAt() : wrong.getCreateTime();
        if (practiceAt != null) {
            vo.setDate(practiceAt.format(DATE_FORMATTER));
            vo.setLastPracticeAt(practiceAt.format(DATETIME_FORMATTER));
        }
        return vo;
    }

    String resolveSubject(UserQuestionWrong wrong, Question question, Map<Long, String> chapterSubjectMap) {
        String stored = StringUtils.trimToEmpty(wrong.getSubject());
        if (StringUtils.isNotBlank(stored) && !isGenericSubject(stored)) {
            return normalizeSubjectLabel(stored);
        }
        if (question != null && question.getChapterId() != null && chapterSubjectMap != null) {
            String chapterSubject = chapterSubjectMap.get(question.getChapterId());
            if (StringUtils.isNotBlank(chapterSubject)) {
                return normalizeSubjectLabel(chapterSubject);
            }
        }
        return normalizeSubjectLabel(StringUtils.defaultIfBlank(stored, DEFAULT_SUBJECT));
    }

    public boolean isGenericSubject(String subject) {
        String value = StringUtils.trim(subject);
        if (StringUtils.isBlank(value)) {
            return true;
        }
        if (DEFAULT_SUBJECT.equals(value)) {
            return true;
        }
        if (WRONG_REDO_SUBJECT.equals(value)) {
            return true;
        }
        if (GENERIC_SUBJECT.equals(value)) {
            return true;
        }
        return value.startsWith(WRONG_REDO_SUBJECT);
    }

    public String normalizeSubjectLabel(String raw) {
        String token = StringUtils.trimToNull(raw);
        if (token == null) {
            return DEFAULT_SUBJECT;
        }
        String resolved = SetupQueryResolveSupport.resolveSubjectValue(token);
        if (StringUtils.isNotBlank(resolved)) {
            return resolved;
        }
        return token;
    }

    public boolean isEnabledQuestion(Question question) {
        return question != null
                && Integer.valueOf(QuestionChapterConstant.STATE_ENABLED).equals(question.getState());
    }

    private List<String> resolveTags(String tagsJson) {
        List<String> tags = QuestionTagsSupport.parseTags(tagsJson);
        if (tags.isEmpty()) {
            return Collections.singletonList(DEFAULT_TAG);
        }
        return tags;
    }
}
