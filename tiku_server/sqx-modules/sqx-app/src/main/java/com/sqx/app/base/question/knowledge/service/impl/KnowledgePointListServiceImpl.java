package com.sqx.app.base.question.knowledge.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sqx.app.base.question.chapter.constants.QuestionChapterConstant;
import com.sqx.app.base.question.knowledge.domain.bo.AppQuestionKnowledgeListQueryBO;
import com.sqx.app.base.question.knowledge.domain.vo.AppQuestionKnowledgePointVO;
import com.sqx.app.base.question.knowledge.service.KnowledgePointListService;
import com.sqx.app.base.setup.support.SetupQueryResolveSupport;
import com.sqx.system.db.entity.KnowledgePoint;
import com.sqx.system.db.service.IKnowledgePointService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户端知识点列表服务实现
 */
@Service
public class KnowledgePointListServiceImpl implements KnowledgePointListService {

    @Resource
    private IKnowledgePointService iKnowledgePointService;

    @Override
    public List<AppQuestionKnowledgePointVO> listKnowledgePoints(AppQuestionKnowledgeListQueryBO query) {
        LambdaQueryWrapper<KnowledgePoint> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KnowledgePoint::getState, QuestionChapterConstant.STATE_ENABLED);
        if (query != null && StringUtils.isNotBlank(query.getGrade())) {
            String gradeValue = SetupQueryResolveSupport.resolveGradeValue(query.getGrade());
            if (StringUtils.isNotBlank(gradeValue)) {
                wrapper.eq(KnowledgePoint::getGrade, gradeValue);
            }
        }
        if (query != null && StringUtils.isNotBlank(query.getSubject())) {
            String subjectValue = SetupQueryResolveSupport.resolveSubjectValue(query.getSubject());
            if (StringUtils.isNotBlank(subjectValue)) {
                wrapper.eq(KnowledgePoint::getSubject, subjectValue);
            }
        }
        wrapper.orderByAsc(KnowledgePoint::getSortOrder)
                .orderByAsc(KnowledgePoint::getId);

        List<KnowledgePoint> points = iKnowledgePointService.list(wrapper);
        if (points == null || points.isEmpty()) {
            return Collections.emptyList();
        }
        return points.stream().map(this::toVo).collect(Collectors.toList());
    }

    private AppQuestionKnowledgePointVO toVo(KnowledgePoint point) {
        AppQuestionKnowledgePointVO vo = new AppQuestionKnowledgePointVO();
        vo.setCode(point.getPointCode());
        vo.setTitle(point.getTitle());
        int count = point.getQuestionCount() == null ? 0 : point.getQuestionCount();
        vo.setQuestionCount(count);
        vo.setMeta(String.format("共 %d 题", count));
        vo.setSubject(point.getSubject());
        vo.setGrade(point.getGrade());
        vo.setGradeShort(point.getGradeShort());
        return vo;
    }
}
