package com.sqx.app.base.question.chapter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sqx.app.base.enums.AppError;
import com.sqx.app.base.question.chapter.domain.bo.*;
import com.sqx.app.base.question.chapter.domain.vo.QuestionSectionVo;
import com.sqx.app.base.question.chapter.service.QuestionSectionAdminService;
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
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * 管理端节维护
 */
@Service
public class QuestionSectionAdminServiceImpl implements QuestionSectionAdminService {

    @Resource
    private IQuestionChapterService iQuestionChapterService;

    @Resource
    private IQuestionSectionService iQuestionSectionService;

    @Override
    public void saveInfo(QuestionSectionSaveBo saveBo) {
        QuestionChapter chapter = resolveChapter(saveBo.getChapterId(), saveBo.getChapterCode());
        String sectionCode = resolveSectionCode(saveBo.getSectionCode(), null, null);

        QuestionSection section = new QuestionSection();
        section.setSectionCode(sectionCode);
        section.setChapterId(chapter.getId());
        section.setTitle(saveBo.getTitle());
        section.setQuestionCount(saveBo.getQuestionCount() != null ? saveBo.getQuestionCount() : 0);
        section.setSortOrder(saveBo.getSortOrder());
        section.setState(saveBo.getState());
        iQuestionSectionService.save(section);
    }

    @Override
    public IPage<QuestionSectionVo> pageInfo(QuestionSectionPageBo pageBo) {
        Page<QuestionSection> initPage = PageUtil.initPage(pageBo.getPage(), pageBo.getLimit());
        Map<Long, QuestionChapter> chapterMap = loadChapterMapForSectionQuery(pageBo);
        if (chapterMap != null && chapterMap.isEmpty()) {
            return PageUtil.copyPage(initPage, Collections.emptyList());
        }

        QueryWrapper<QuestionSection> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .like(StringUtils.isNotBlank(pageBo.getSectionCode()), QuestionSection::getSectionCode, pageBo.getSectionCode())
                .like(StringUtils.isNotBlank(pageBo.getTitle()), QuestionSection::getTitle, pageBo.getTitle())
                .eq(Objects.nonNull(pageBo.getState()), QuestionSection::getState, pageBo.getState())
                .orderByAsc(QuestionSection::getSortOrder)
                .orderByDesc(QuestionSection::getCreateTime);
        if (chapterMap != null) {
            queryWrapper.lambda().in(QuestionSection::getChapterId, chapterMap.keySet());
        }

        IPage<QuestionSection> page = iQuestionSectionService.page(initPage, queryWrapper);
        if (chapterMap == null && CollectionUtils.isNotEmpty(page.getRecords())) {
            chapterMap = loadChaptersByIds(page.getRecords().stream()
                    .map(QuestionSection::getChapterId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet()));
        }

        List<QuestionSectionVo> voList = toSectionVoList(page.getRecords(), chapterMap);
        return PageUtil.copyPage(page, voList);
    }

    @Override
    public QuestionSectionVo info(QuestionSectionInfoBo infoBo) {
        QuestionSection section = iQuestionSectionService.getById(infoBo.getId());
        if (Objects.isNull(section)) {
            throw new BizException(AppError.INFO_NOT_FOUND);
        }
        Map<Long, QuestionChapter> chapterMap = loadChaptersByIds(Collections.singleton(section.getChapterId()));
        return toSectionVo(section, chapterMap.get(section.getChapterId()));
    }

    @Override
    public void updateInfo(QuestionSectionUpdateBo updateBo) {
        QuestionSection existing = iQuestionSectionService.getById(updateBo.getId());
        if (Objects.isNull(existing)) {
            throw new BizException(AppError.INFO_NOT_FOUND);
        }
        QuestionChapter chapter = resolveChapter(updateBo.getChapterId(), updateBo.getChapterCode());
        String sectionCode = resolveSectionCode(updateBo.getSectionCode(), updateBo.getId(), existing.getSectionCode());

        QuestionSection section = new QuestionSection();
        section.setId(updateBo.getId());
        section.setSectionCode(sectionCode);
        section.setChapterId(chapter.getId());
        section.setTitle(updateBo.getTitle());
        section.setQuestionCount(updateBo.getQuestionCount() != null ? updateBo.getQuestionCount() : 0);
        section.setSortOrder(updateBo.getSortOrder());
        section.setState(updateBo.getState());
        iQuestionSectionService.updateById(section);
    }

    @Override
    public void delete(QuestionSectionInfoBo infoBo) {
        QuestionSection section = iQuestionSectionService.getById(infoBo.getId());
        if (Objects.isNull(section)) {
            throw new BizException(AppError.INFO_NOT_FOUND);
        }
        iQuestionSectionService.removeById(infoBo.getId());
    }

    @Override
    public void updateState(QuestionSectionUpdateStateBo updateStateBo) {
        LambdaUpdateWrapper<QuestionSection> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper
                .eq(QuestionSection::getId, updateStateBo.getId())
                .set(QuestionSection::getState, updateStateBo.getState());
        iQuestionSectionService.update(new QuestionSection(), updateWrapper);
    }

    /**
     * 按章筛选条件加载章映射；无章级筛选时返回 null 表示不过滤章。
     */
    private Map<Long, QuestionChapter> loadChapterMapForSectionQuery(QuestionSectionPageBo pageBo) {
        boolean hasChapterFilter = StringUtils.isNotBlank(pageBo.getChapterCode())
                || StringUtils.isNotBlank(pageBo.getChapterTitle())
                || StringUtils.isNotBlank(pageBo.getGrade())
                || StringUtils.isNotBlank(pageBo.getSubject())
                || StringUtils.isNotBlank(pageBo.getVersion());
        if (!hasChapterFilter) {
            return null;
        }

        QueryWrapper<QuestionChapter> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .like(StringUtils.isNotBlank(pageBo.getChapterCode()), QuestionChapter::getChapterCode, pageBo.getChapterCode())
                .like(StringUtils.isNotBlank(pageBo.getChapterTitle()), QuestionChapter::getTitle, pageBo.getChapterTitle())
                .eq(StringUtils.isNotBlank(pageBo.getGrade()), QuestionChapter::getGrade, pageBo.getGrade())
                .eq(StringUtils.isNotBlank(pageBo.getSubject()), QuestionChapter::getSubject, pageBo.getSubject())
                .eq(StringUtils.isNotBlank(pageBo.getVersion()), QuestionChapter::getVersion, pageBo.getVersion());
        List<QuestionChapter> chapters = iQuestionChapterService.list(queryWrapper);
        if (CollectionUtils.isEmpty(chapters)) {
            return Collections.emptyMap();
        }
        return chapters.stream().collect(Collectors.toMap(QuestionChapter::getId, item -> item, (a, b) -> a));
    }

    private Map<Long, QuestionChapter> loadChaptersByIds(Set<Long> chapterIds) {
        if (CollectionUtils.isEmpty(chapterIds)) {
            return Collections.emptyMap();
        }
        List<QuestionChapter> chapters = iQuestionChapterService.listByIds(chapterIds);
        if (CollectionUtils.isEmpty(chapters)) {
            return Collections.emptyMap();
        }
        return chapters.stream().collect(Collectors.toMap(QuestionChapter::getId, item -> item, (a, b) -> a));
    }

    private QuestionChapter resolveChapter(Long chapterId, String chapterCode) {
        if (chapterId != null) {
            QuestionChapter chapter = iQuestionChapterService.getById(chapterId);
            if (chapter == null) {
                throw new BizException("所属章不存在");
            }
            return chapter;
        }
        if (StringUtils.isNotBlank(chapterCode)) {
            return requireChapterByCode(chapterCode);
        }
        throw new BizException("请选择所属章");
    }

    private QuestionChapter requireChapterByCode(String chapterCode) {
        QueryWrapper<QuestionChapter> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(QuestionChapter::getChapterCode, chapterCode);
        QuestionChapter chapter = iQuestionChapterService.getOne(queryWrapper, false);
        if (Objects.isNull(chapter)) {
            throw new BizException("所属章不存在");
        }
        return chapter;
    }

    private String resolveSectionCode(String rawCode, Long excludeId, String fallback) {
        String code = StringUtils.trimToNull(rawCode);
        if (code != null) {
            assertSectionCodeUnique(code, excludeId);
            return code;
        }
        if (StringUtils.isNotBlank(fallback)) {
            return fallback;
        }
        for (int i = 0; i < 8; i++) {
            String candidate = "sec-" + System.currentTimeMillis() + "-" + ThreadLocalRandom.current().nextInt(1000, 10000);
            if (!isSectionCodeExists(candidate, excludeId)) {
                return candidate;
            }
        }
        throw new BizException("系统生成节编号失败，请稍后重试");
    }

    private boolean isSectionCodeExists(String sectionCode, Long excludeId) {
        QueryWrapper<QuestionSection> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(QuestionSection::getSectionCode, sectionCode);
        if (excludeId != null) {
            queryWrapper.lambda().ne(QuestionSection::getId, excludeId);
        }
        return iQuestionSectionService.count(queryWrapper) > 0;
    }

    private void assertSectionCodeUnique(String sectionCode, Long excludeId) {
        if (isSectionCodeExists(sectionCode, excludeId)) {
            throw new BizException("节编号已存在：" + sectionCode);
        }
    }

    private List<QuestionSectionVo> toSectionVoList(List<QuestionSection> sections, Map<Long, QuestionChapter> chapterMap) {
        if (CollectionUtils.isEmpty(sections)) {
            return Collections.emptyList();
        }
        List<QuestionSectionVo> voList = new ArrayList<>(sections.size());
        for (QuestionSection section : sections) {
            QuestionChapter chapter = chapterMap != null ? chapterMap.get(section.getChapterId()) : null;
            voList.add(toSectionVo(section, chapter));
        }
        return voList;
    }

    private QuestionSectionVo toSectionVo(QuestionSection section, QuestionChapter chapter) {
        QuestionSectionVo vo = new QuestionSectionVo();
        BeanUtils.copyProperties(section, vo);
        if (chapter != null) {
            vo.setChapterCode(chapter.getChapterCode());
            vo.setChapterTitle(chapter.getTitle());
        }
        return vo;
    }
}
