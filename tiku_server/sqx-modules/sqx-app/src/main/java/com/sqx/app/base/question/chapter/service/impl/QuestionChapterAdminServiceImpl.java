package com.sqx.app.base.question.chapter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sqx.app.base.enums.AppError;
import com.sqx.app.base.question.chapter.domain.bo.*;
import com.sqx.app.base.question.chapter.domain.vo.QuestionChapterVo;
import com.sqx.app.base.question.chapter.service.QuestionChapterAdminService;
import com.sqx.common.core.exception.BizException;
import com.sqx.common.mybatis.utils.PageUtil;
import com.sqx.system.db.entity.QuestionChapter;
import com.sqx.system.db.entity.QuestionSection;
import com.sqx.system.db.service.IQuestionChapterService;
import com.sqx.system.db.service.IQuestionSectionService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 管理端章维护
 */
@Service
public class QuestionChapterAdminServiceImpl implements QuestionChapterAdminService {

    @Resource
    private IQuestionChapterService iQuestionChapterService;

    @Resource
    private IQuestionSectionService iQuestionSectionService;

    @Override
    public void saveInfo(QuestionChapterSaveBo saveBo) {
        String chapterCode = resolveChapterCode(saveBo.getChapterCode(), null, null);

        QuestionChapter chapter = new QuestionChapter();
        BeanUtils.copyProperties(saveBo, chapter);
        chapter.setChapterCode(chapterCode);
        if (chapter.getQuestionCount() == null) {
            chapter.setQuestionCount(0);
        }
        iQuestionChapterService.save(chapter);
    }

    @Override
    public IPage<QuestionChapterVo> pageInfo(QuestionChapterPageBo pageBo) {
        Page<QuestionChapter> initPage = PageUtil.initPage(pageBo.getPage(), pageBo.getLimit());
        QueryWrapper<QuestionChapter> queryWrapper = buildChapterQuery(pageBo);
        queryWrapper.lambda().orderByAsc(QuestionChapter::getSortOrder).orderByDesc(QuestionChapter::getCreateTime);

        IPage<QuestionChapter> page = iQuestionChapterService.page(initPage, queryWrapper);
        List<QuestionChapterVo> voList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(page.getRecords())) {
            for (QuestionChapter record : page.getRecords()) {
                QuestionChapterVo vo = new QuestionChapterVo();
                BeanUtils.copyProperties(record, vo);
                voList.add(vo);
            }
        }
        return PageUtil.copyPage(page, voList);
    }

    @Override
    public QuestionChapterVo info(QuestionChapterInfoBo infoBo) {
        QuestionChapter chapter = iQuestionChapterService.getById(infoBo.getId());
        if (Objects.isNull(chapter)) {
            throw new BizException(AppError.INFO_NOT_FOUND);
        }
        QuestionChapterVo vo = new QuestionChapterVo();
        BeanUtils.copyProperties(chapter, vo);
        return vo;
    }

    @Override
    public void updateInfo(QuestionChapterUpdateBo updateBo) {
        QuestionChapter existing = iQuestionChapterService.getById(updateBo.getId());
        if (Objects.isNull(existing)) {
            throw new BizException(AppError.INFO_NOT_FOUND);
        }
        String chapterCode = resolveChapterCode(updateBo.getChapterCode(), updateBo.getId(), existing.getChapterCode());

        QuestionChapter chapter = new QuestionChapter();
        BeanUtils.copyProperties(updateBo, chapter);
        chapter.setChapterCode(chapterCode);
        if (chapter.getQuestionCount() == null) {
            chapter.setQuestionCount(0);
        }
        iQuestionChapterService.updateById(chapter);
    }

    @Override
    public void delete(QuestionChapterInfoBo infoBo) {
        QuestionChapter chapter = iQuestionChapterService.getById(infoBo.getId());
        if (Objects.isNull(chapter)) {
            throw new BizException(AppError.INFO_NOT_FOUND);
        }
        QueryWrapper<QuestionSection> sectionQuery = new QueryWrapper<>();
        sectionQuery.lambda().eq(QuestionSection::getChapterId, infoBo.getId());
        long sectionCount = iQuestionSectionService.count(sectionQuery);
        if (sectionCount > 0) {
            throw new BizException("该章下仍有节，请先删除或迁移节后再删除章");
        }
        iQuestionChapterService.removeById(infoBo.getId());
    }

    @Override
    public void updateState(QuestionChapterUpdateStateBo updateStateBo) {
        LambdaUpdateWrapper<QuestionChapter> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper
                .eq(QuestionChapter::getId, updateStateBo.getId())
                .set(QuestionChapter::getState, updateStateBo.getState());
        iQuestionChapterService.update(new QuestionChapter(), updateWrapper);
    }

    private QueryWrapper<QuestionChapter> buildChapterQuery(QuestionChapterPageBo pageBo) {
        QueryWrapper<QuestionChapter> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .like(StringUtils.isNotBlank(pageBo.getChapterCode()), QuestionChapter::getChapterCode, pageBo.getChapterCode())
                .like(StringUtils.isNotBlank(pageBo.getTitle()), QuestionChapter::getTitle, pageBo.getTitle())
                .eq(StringUtils.isNotBlank(pageBo.getGrade()), QuestionChapter::getGrade, pageBo.getGrade())
                .eq(StringUtils.isNotBlank(pageBo.getSubject()), QuestionChapter::getSubject, pageBo.getSubject())
                .eq(StringUtils.isNotBlank(pageBo.getVersion()), QuestionChapter::getVersion, pageBo.getVersion())
                .eq(Objects.nonNull(pageBo.getState()), QuestionChapter::getState, pageBo.getState());
        return queryWrapper;
    }

    private String resolveChapterCode(String rawCode, Long excludeId, String fallback) {
        String code = StringUtils.trimToNull(rawCode);
        if (code != null) {
            assertChapterCodeUnique(code, excludeId);
            return code;
        }
        if (StringUtils.isNotBlank(fallback)) {
            return fallback;
        }
        for (int i = 0; i < 8; i++) {
            String candidate = "ch-" + System.currentTimeMillis() + "-" + ThreadLocalRandom.current().nextInt(1000, 10000);
            if (!isChapterCodeExists(candidate, excludeId)) {
                return candidate;
            }
        }
        throw new BizException("系统生成章编号失败，请稍后重试");
    }

    private boolean isChapterCodeExists(String chapterCode, Long excludeId) {
        QueryWrapper<QuestionChapter> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(QuestionChapter::getChapterCode, chapterCode);
        if (excludeId != null) {
            queryWrapper.lambda().ne(QuestionChapter::getId, excludeId);
        }
        return iQuestionChapterService.count(queryWrapper) > 0;
    }

    private void assertChapterCodeUnique(String chapterCode, Long excludeId) {
        if (isChapterCodeExists(chapterCode, excludeId)) {
            throw new BizException("章编号已存在：" + chapterCode);
        }
    }
}
