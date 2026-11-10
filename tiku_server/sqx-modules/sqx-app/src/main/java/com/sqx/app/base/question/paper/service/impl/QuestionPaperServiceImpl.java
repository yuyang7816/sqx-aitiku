package com.sqx.app.base.question.paper.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sqx.app.base.enums.AppError;
import com.sqx.app.base.question.chapter.constants.QuestionChapterConstant;
import com.sqx.app.base.question.paper.constants.QuestionPaperConstant;
import com.sqx.app.base.question.paper.domain.bo.AppQuestionPaperListQueryBO;
import com.sqx.app.base.question.paper.domain.vo.AppQuestionPaperItemVO;
import com.sqx.app.base.question.paper.domain.vo.AppQuestionPaperQuestionVO;
import com.sqx.app.base.question.paper.service.QuestionPaperService;
import com.sqx.app.base.question.paper.support.QuestionOptionsSupport;
import com.sqx.common.core.exception.BizException;
import com.sqx.system.db.entity.Question;
import com.sqx.system.db.entity.QuestionPaper;
import com.sqx.system.db.entity.QuestionPaperItem;
import com.sqx.system.db.service.IQuestionPaperItemService;
import com.sqx.system.db.service.IQuestionPaperService;
import com.sqx.system.db.service.IQuestionService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 用户端专项/真题题集服务实现
 */
@Service
public class QuestionPaperServiceImpl implements QuestionPaperService {

    @Resource
    private IQuestionPaperService iQuestionPaperService;

    @Resource
    private IQuestionPaperItemService iQuestionPaperItemService;

    @Resource
    private IQuestionService iQuestionService;

    @Override
    public List<AppQuestionPaperItemVO> listPapers(AppQuestionPaperListQueryBO query) {
        String paperType = StringUtils.trim(query.getType());
        LambdaQueryWrapper<QuestionPaper> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(QuestionPaper::getPaperType, paperType)
                .eq(QuestionPaper::getState, QuestionPaperConstant.STATE_ENABLED)
                .orderByAsc(QuestionPaper::getSortOrder)
                .orderByAsc(QuestionPaper::getId);
        return iQuestionPaperService.list(wrapper).stream()
                .map(this::toPaperItemVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<AppQuestionPaperQuestionVO> listPaperQuestions(String paperId) {
        QuestionPaper paper = resolvePaper(paperId);
        if (paper == null) {
            throw new BizException(AppError.INFO_NOT_FOUND);
        }

        List<QuestionPaperItem> items = listPaperItems(paper.getId());
        if (items.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, Question> questionMap = loadQuestionMap(items);
        return items.stream()
                .map(item -> toPaperQuestionVO(item, questionMap.get(item.getQuestionId())))
                .filter(vo -> vo != null)
                .collect(Collectors.toList());
    }

    QuestionPaper resolvePaper(String paperId) {
        if (StringUtils.isBlank(paperId)) {
            return null;
        }
        LambdaQueryWrapper<QuestionPaper> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(QuestionPaper::getPaperCode, StringUtils.trim(paperId))
                .eq(QuestionPaper::getState, QuestionPaperConstant.STATE_ENABLED)
                .last("LIMIT 1");
        return iQuestionPaperService.getOne(wrapper);
    }

    List<QuestionPaperItem> listPaperItems(Long paperId) {
        if (paperId == null) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<QuestionPaperItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(QuestionPaperItem::getPaperId, paperId)
                .orderByAsc(QuestionPaperItem::getSortOrder)
                .orderByAsc(QuestionPaperItem::getId);
        return iQuestionPaperItemService.list(wrapper);
    }

    Map<Long, Question> loadQuestionMap(List<QuestionPaperItem> items) {
        List<Long> questionIds = items.stream()
                .map(QuestionPaperItem::getQuestionId)
                .distinct()
                .collect(Collectors.toList());
        if (questionIds.isEmpty()) {
            return Collections.emptyMap();
        }
        LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Question::getId, questionIds)
                .eq(Question::getState, QuestionChapterConstant.STATE_ENABLED);
        return iQuestionService.list(wrapper).stream()
                .collect(Collectors.toMap(Question::getId, Function.identity(), (a, b) -> a));
    }

    AppQuestionPaperItemVO toPaperItemVO(QuestionPaper paper) {
        AppQuestionPaperItemVO vo = new AppQuestionPaperItemVO();
        vo.setPaperId(paper.getPaperCode());
        vo.setTitle(paper.getTitle());
        vo.setType(paper.getPaperType());
        vo.setDescription(paper.getDescription());
        vo.setGrade(paper.getGrade());
        vo.setGradeShort(paper.getGradeShort());
        vo.setSubject(paper.getSubject());
        vo.setQuestionCount(paper.getQuestionCount());
        return vo;
    }

    AppQuestionPaperQuestionVO toPaperQuestionVO(QuestionPaperItem item, Question question) {
        if (question == null) {
            return null;
        }
        AppQuestionPaperQuestionVO vo = new AppQuestionPaperQuestionVO();
        vo.setNo(formatNo(item.getSortOrder()));
        vo.setQuestionId(question.getId());
        vo.setQuestionCode(question.getQuestionCode());
        vo.setType(StringUtils.defaultIfBlank(question.getQuestionType(), "单选题"));
        vo.setDifficulty(StringUtils.defaultIfBlank(question.getDifficulty(), "中等"));
        vo.setTitle(question.getTitle());
        vo.setRightAnswer(question.getRightAnswer());
        vo.setAnalysis(StringUtils.defaultString(question.getAnalysis()));
        vo.setOptions(QuestionOptionsSupport.parseOptions(question.getOptionsJson()));
        return vo;
    }

    String formatNo(Integer sortOrder) {
        int value = sortOrder == null || sortOrder <= 0 ? 0 : sortOrder;
        return String.format("%02d", value);
    }
}
