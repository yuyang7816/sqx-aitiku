package com.sqx.app.base.question.manage.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sqx.app.base.enums.AppError;
import com.sqx.app.base.question.chapter.constants.QuestionChapterConstant;
import com.sqx.app.base.question.list.support.QuestionTagsSupport;
import com.sqx.app.base.question.manage.constants.QuestionPracticeEntry;
import com.sqx.app.base.question.manage.domain.bo.*;
import com.sqx.app.base.question.manage.domain.vo.QuestionBindOptionVo;
import com.sqx.app.base.question.manage.domain.vo.QuestionImportErrorVo;
import com.sqx.app.base.question.manage.domain.vo.QuestionImportResultVo;
import com.sqx.app.base.question.manage.domain.vo.QuestionVo;
import com.sqx.app.base.question.manage.service.QuestionAdminService;
import com.sqx.app.base.question.paper.domain.vo.AppQuestionOptionVO;
import com.sqx.app.base.question.paper.support.QuestionOptionsSupport;
import com.sqx.app.base.setup.support.SetupQueryResolveSupport;
import com.sqx.common.core.exception.BizException;
import com.sqx.common.redis.utils.ConfigUtil;
import com.sqx.common.mybatis.utils.PageUtil;
import com.sqx.system.db.entity.*;
import com.sqx.system.db.service.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * 管理端题目维护实现
 */
@Service
public class QuestionAdminServiceImpl implements QuestionAdminService {

    @Resource
    private IQuestionService iQuestionService;

    @Resource
    private IQuestionChapterService iQuestionChapterService;

    @Resource
    private IQuestionSectionService iQuestionSectionService;

    @Resource
    private IQuestionPaperItemService iQuestionPaperItemService;

    @Resource
    private IQuestionPaperService iQuestionPaperService;

    @Resource
    private IKnowledgePointService iKnowledgePointService;

    @Resource
    private IKnowledgePointQuestionService iKnowledgePointQuestionService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveInfo(QuestionSaveBo saveBo) {
        validateSaveBo(saveBo, null);
        String questionCode = resolveQuestionCode(saveBo.getQuestionCode(), null);
        CatalogContext catalog = resolveCatalogContext(saveBo);

        Question question = buildQuestionEntity(saveBo, questionCode, catalog.chapterId, catalog.sectionId);
        iQuestionService.save(question);

        bindPracticeTarget(question.getId(), saveBo);
        refreshRelatedCounts(question.getId());
    }

    @Override
    public IPage<QuestionVo> pageInfo(QuestionPageBo pageBo) {
        Page<Question> initPage = PageUtil.initPage(pageBo.getPage(), pageBo.getLimit());
        Map<Long, QuestionChapter> chapterMap = loadChapterMapForQuestionQuery(pageBo);
        if (chapterMap != null && chapterMap.isEmpty()) {
            return PageUtil.copyPage(initPage, Collections.emptyList());
        }

        Set<Long> sectionIds = loadSectionIdsForQuestionQuery(pageBo, chapterMap);
        if (sectionIds != null && sectionIds.isEmpty()) {
            return PageUtil.copyPage(initPage, Collections.emptyList());
        }

        Set<Long> practiceFilteredIds = resolveQuestionIdsByPracticeEntry(pageBo.getPracticeEntry(), pageBo.getSubject());
        if (practiceFilteredIds != null && practiceFilteredIds.isEmpty()) {
            return PageUtil.copyPage(initPage, Collections.emptyList());
        }

        QueryWrapper<Question> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .like(StringUtils.isNotBlank(pageBo.getTitle()), Question::getTitle, pageBo.getTitle())
                .eq(StringUtils.isNotBlank(pageBo.getQuestionType()), Question::getQuestionType, pageBo.getQuestionType())
                .eq(StringUtils.isNotBlank(pageBo.getDifficulty()), Question::getDifficulty, pageBo.getDifficulty())
                .like(StringUtils.isNotBlank(pageBo.getTagKeyword()), Question::getTagsJson, pageBo.getTagKeyword())
                .eq(Objects.nonNull(pageBo.getState()), Question::getState, pageBo.getState())
                .orderByAsc(Question::getSortOrder)
                .orderByDesc(Question::getCreateTime);

        if (chapterMap != null) {
            queryWrapper.lambda().in(Question::getChapterId, chapterMap.keySet());
        }
        if (sectionIds != null) {
            queryWrapper.lambda().in(Question::getSectionId, sectionIds);
        }
        if (practiceFilteredIds != null) {
            queryWrapper.lambda().in(Question::getId, practiceFilteredIds);
        }

        IPage<Question> page = iQuestionService.page(initPage, queryWrapper);
        PracticeBindingMaps bindingMaps = loadPracticeBindingMaps(page.getRecords());
        RelationContext relationContext = loadRelationContext(page.getRecords(), chapterMap);
        List<QuestionVo> voList = toQuestionVoList(page.getRecords(), relationContext, bindingMaps);
        return PageUtil.copyPage(page, voList);
    }

    @Override
    public QuestionVo info(QuestionInfoBo infoBo) {
        Question question = iQuestionService.getById(infoBo.getId());
        if (Objects.isNull(question)) {
            throw new BizException(AppError.INFO_NOT_FOUND);
        }
        PracticeBindingMaps bindingMaps = loadPracticeBindingMaps(Collections.singletonList(question));
        RelationContext relationContext = loadRelationContext(Collections.singletonList(question), null);
        return toQuestionVo(question, relationContext, bindingMaps);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateInfo(QuestionUpdateBo updateBo) {
        Question existing = iQuestionService.getById(updateBo.getId());
        if (Objects.isNull(existing)) {
            throw new BizException(AppError.INFO_NOT_FOUND);
        }
        validateSaveBo(updateBo, updateBo.getId());
        String questionCode = resolveQuestionCode(updateBo.getQuestionCode(), updateBo.getId());
        CatalogContext catalog = resolveCatalogContext(updateBo);

        clearPracticeBindings(updateBo.getId());
        Question question = buildQuestionEntity(updateBo, questionCode, catalog.chapterId, catalog.sectionId);
        question.setId(updateBo.getId());
        iQuestionService.updateById(question);

        bindPracticeTarget(updateBo.getId(), updateBo);
        refreshRelatedCounts(updateBo.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(QuestionInfoBo infoBo) {
        Question question = iQuestionService.getById(infoBo.getId());
        if (Objects.isNull(question)) {
            throw new BizException(AppError.INFO_NOT_FOUND);
        }
        Long chapterId = question.getChapterId();
        Long sectionId = question.getSectionId();
        clearPracticeBindings(infoBo.getId());
        iQuestionService.removeById(infoBo.getId());
        if (chapterId != null) {
            refreshChapterCount(chapterId);
        }
        if (sectionId != null) {
            refreshSectionCount(sectionId);
        }
        refreshAllKnowledgeCounts();
        refreshAllPaperCounts();
    }

    @Override
    public void updateState(QuestionUpdateStateBo updateStateBo) {
        LambdaUpdateWrapper<Question> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper
                .eq(Question::getId, updateStateBo.getId())
                .set(Question::getState, updateStateBo.getState());
        iQuestionService.update(new Question(), updateWrapper);
    }

    @Override
    public QuestionImportResultVo importBatch(QuestionImportBo importBo) {
        QuestionImportResultVo result = new QuestionImportResultVo();
        if (CollectionUtils.isEmpty(importBo.getItems())) {
            return result;
        }
        boolean skipExisting = importBo.getSkipExisting() == null || Boolean.TRUE.equals(importBo.getSkipExisting());
        List<QuestionSaveBo> items = importBo.getItems();
        for (int i = 0; i < items.size(); i++) {
            QuestionSaveBo item = items.get(i);
            int rowNum = i + 1;
            String questionCode = item == null ? null : StringUtils.trim(item.getQuestionCode());
            try {
                if (item == null) {
                    throw new BizException("题目数据为空");
                }
                if (StringUtils.isBlank(item.getPracticeEntry()) && StringUtils.isNotBlank(item.getChapterCode())) {
                    item.setPracticeEntry(QuestionPracticeEntry.CHAPTER);
                }
                resolveImportBindFields(item);
                if (skipExisting && StringUtils.isNotBlank(questionCode) && isQuestionCodeExists(questionCode, null)) {
                    result.setSkipCount(result.getSkipCount() + 1);
                    continue;
                }
                if (skipExisting && StringUtils.isNotBlank(item.getTitle()) && isTitleExists(item.getTitle(), null)) {
                    result.setSkipCount(result.getSkipCount() + 1);
                    continue;
                }
                saveInfo(item);
                result.setSuccessCount(result.getSuccessCount() + 1);
            } catch (Exception ex) {
                result.setFailCount(result.getFailCount() + 1);
                String message = ex instanceof BizException
                        ? ex.getMessage()
                        : (ex.getMessage() == null ? "导入失败" : ex.getMessage());
                result.getErrors().add(new QuestionImportErrorVo(rowNum, questionCode, message));
            }
        }
        return result;
    }

    @Override
    public List<QuestionBindOptionVo> listKnowledgeOptions(QuestionBindOptionQueryBo queryBo) {
        LambdaQueryWrapper<KnowledgePoint> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KnowledgePoint::getState, QuestionChapterConstant.STATE_ENABLED);
        applyGradeSubjectFilter(wrapper, queryBo == null ? null : queryBo.getGrade(),
                queryBo == null ? null : queryBo.getSubject(), KnowledgePoint::getGrade, KnowledgePoint::getSubject);
        if (queryBo != null && StringUtils.isNotBlank(queryBo.getKeyword())) {
            wrapper.like(KnowledgePoint::getTitle, queryBo.getKeyword().trim());
        }
        wrapper.orderByAsc(KnowledgePoint::getSortOrder).orderByAsc(KnowledgePoint::getId);
        return iKnowledgePointService.list(wrapper).stream().map(point -> {
            QuestionBindOptionVo vo = new QuestionBindOptionVo();
            vo.setValue(point.getPointCode());
            vo.setTitle(point.getTitle());
            vo.setMeta(String.format("%s · %s · %d 题",
                    StringUtils.defaultIfBlank(point.getGradeShort(), point.getGrade()),
                    point.getSubject(),
                    point.getQuestionCount() == null ? 0 : point.getQuestionCount()));
            vo.setLabel(String.format("%s（%s）", point.getTitle(), vo.getMeta()));
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public List<QuestionBindOptionVo> listPaperOptions(QuestionBindOptionQueryBo queryBo) {
        LambdaQueryWrapper<QuestionPaper> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(QuestionPaper::getState, QuestionChapterConstant.STATE_ENABLED);
        if (queryBo != null && StringUtils.isNotBlank(queryBo.getPaperType())) {
            wrapper.eq(QuestionPaper::getPaperType, queryBo.getPaperType().trim());
        }
        applyGradeSubjectFilter(wrapper, queryBo == null ? null : queryBo.getGrade(),
                queryBo == null ? null : queryBo.getSubject(), QuestionPaper::getGrade, QuestionPaper::getSubject);
        if (queryBo != null && StringUtils.isNotBlank(queryBo.getKeyword())) {
            wrapper.like(QuestionPaper::getTitle, queryBo.getKeyword().trim());
        }
        wrapper.orderByAsc(QuestionPaper::getSortOrder).orderByDesc(QuestionPaper::getCreateTime);
        return iQuestionPaperService.list(wrapper).stream().map(paper -> {
            QuestionBindOptionVo vo = new QuestionBindOptionVo();
            vo.setValue(paper.getPaperCode());
            vo.setTitle(paper.getTitle());
            vo.setMeta(String.format("%s · %s · %d 题",
                    QuestionPracticeEntry.labelOf(paper.getPaperType()),
                    StringUtils.defaultIfBlank(paper.getGradeShort(), paper.getGrade()),
                    paper.getQuestionCount() == null ? 0 : paper.getQuestionCount()));
            vo.setLabel(String.format("%s（%s）", paper.getTitle(), paper.getSubject()));
            return vo;
        }).collect(Collectors.toList());
    }

    private void resolveImportBindFields(QuestionSaveBo saveBo) {
        if (saveBo == null) {
            return;
        }
        String entry = StringUtils.trimToEmpty(saveBo.getPracticeEntry());
        if (QuestionPracticeEntry.CHAPTER.equals(entry)) {
            if (StringUtils.isBlank(saveBo.getChapterCode()) && StringUtils.isNotBlank(saveBo.getChapterTitle())) {
                QuestionChapter chapter = requireChapterByTitle(
                        saveBo.getGrade(), saveBo.getSubject(), saveBo.getVersion(), saveBo.getChapterTitle());
                saveBo.setChapterCode(chapter.getChapterCode());
            }
            if (StringUtils.isBlank(saveBo.getSectionCode()) && StringUtils.isNotBlank(saveBo.getSectionTitle())) {
                if (StringUtils.isBlank(saveBo.getChapterCode())) {
                    throw new BizException("填写节标题时需同时指定所属章");
                }
                QuestionChapter chapter = requireChapterByCode(saveBo.getChapterCode());
                QuestionSection section = requireSectionByTitle(chapter.getId(), saveBo.getSectionTitle());
                saveBo.setSectionCode(section.getSectionCode());
            }
            return;
        }
        if (QuestionPracticeEntry.KNOWLEDGE.equals(entry)) {
            if (StringUtils.isBlank(saveBo.getPointCode()) && StringUtils.isNotBlank(saveBo.getPointTitle())) {
                KnowledgePoint point = requirePointByTitle(saveBo.getGrade(), saveBo.getSubject(), saveBo.getPointTitle());
                saveBo.setPointCode(point.getPointCode());
            }
            return;
        }
        if (QuestionPracticeEntry.isPaperEntry(entry)) {
            if (StringUtils.isBlank(saveBo.getPaperCode()) && StringUtils.isNotBlank(saveBo.getPaperTitle())) {
                QuestionPaper paper = requirePaperByTitle(
                        saveBo.getGrade(), saveBo.getSubject(), entry, saveBo.getPaperTitle());
                saveBo.setPaperCode(paper.getPaperCode());
            }
        }
    }

    private QuestionChapter requireChapterByTitle(String grade, String subject, String version, String chapterTitle) {
        QueryWrapper<QuestionChapter> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(QuestionChapter::getTitle, StringUtils.trim(chapterTitle))
                .eq(QuestionChapter::getState, QuestionChapterConstant.STATE_ENABLED);
        if (StringUtils.isNotBlank(grade)) {
            queryWrapper.lambda().eq(QuestionChapter::getGrade, SetupQueryResolveSupport.resolveGradeValue(grade));
        }
        if (StringUtils.isNotBlank(subject)) {
            queryWrapper.lambda().eq(QuestionChapter::getSubject, SetupQueryResolveSupport.resolveSubjectValue(subject));
        }
        if (StringUtils.isNotBlank(version)) {
            queryWrapper.lambda().eq(QuestionChapter::getVersion, version.trim());
        }
        List<QuestionChapter> chapters = iQuestionChapterService.list(queryWrapper);
        if (CollectionUtils.isEmpty(chapters)) {
            throw new BizException("未找到章「" + chapterTitle + "」，请先在章节题库中创建");
        }
        if (chapters.size() > 1) {
            throw new BizException("存在多个同名章「" + chapterTitle + "」，请补充年级/学科/版本以唯一定位");
        }
        return chapters.get(0);
    }

    private QuestionSection requireSectionByTitle(Long chapterId, String sectionTitle) {
        QueryWrapper<QuestionSection> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(QuestionSection::getChapterId, chapterId)
                .eq(QuestionSection::getTitle, StringUtils.trim(sectionTitle))
                .eq(QuestionSection::getState, QuestionChapterConstant.STATE_ENABLED);
        QuestionSection section = iQuestionSectionService.getOne(queryWrapper, false);
        if (section == null) {
            throw new BizException("未找到节「" + sectionTitle + "」，请确认所属章下已创建该节");
        }
        return section;
    }

    private KnowledgePoint requirePointByTitle(String grade, String subject, String pointTitle) {
        QueryWrapper<KnowledgePoint> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(KnowledgePoint::getTitle, StringUtils.trim(pointTitle))
                .eq(KnowledgePoint::getState, QuestionChapterConstant.STATE_ENABLED);
        if (StringUtils.isNotBlank(grade)) {
            queryWrapper.lambda().eq(KnowledgePoint::getGrade, SetupQueryResolveSupport.resolveGradeValue(grade));
        }
        if (StringUtils.isNotBlank(subject)) {
            queryWrapper.lambda().eq(KnowledgePoint::getSubject, SetupQueryResolveSupport.resolveSubjectValue(subject));
        }
        List<KnowledgePoint> points = iKnowledgePointService.list(queryWrapper);
        if (CollectionUtils.isEmpty(points)) {
            throw new BizException("未找到知识点「" + pointTitle + "」，请先维护知识点");
        }
        if (points.size() > 1) {
            throw new BizException("存在多个同名知识点「" + pointTitle + "」，请补充年级/学科以唯一定位");
        }
        return points.get(0);
    }

    private QuestionPaper requirePaperByTitle(String grade, String subject, String paperType, String paperTitle) {
        QueryWrapper<QuestionPaper> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(QuestionPaper::getTitle, StringUtils.trim(paperTitle))
                .eq(QuestionPaper::getPaperType, paperType)
                .eq(QuestionPaper::getState, QuestionChapterConstant.STATE_ENABLED);
        if (StringUtils.isNotBlank(grade)) {
            queryWrapper.lambda().eq(QuestionPaper::getGrade, SetupQueryResolveSupport.resolveGradeValue(grade));
        }
        if (StringUtils.isNotBlank(subject)) {
            queryWrapper.lambda().eq(QuestionPaper::getSubject, SetupQueryResolveSupport.resolveSubjectValue(subject));
        }
        List<QuestionPaper> papers = iQuestionPaperService.list(queryWrapper);
        if (CollectionUtils.isEmpty(papers)) {
            throw new BizException("未找到题集「" + paperTitle + "」，请先在题集管理中创建");
        }
        if (papers.size() > 1) {
            throw new BizException("存在多个同名题集「" + paperTitle + "」，请补充年级/学科以唯一定位");
        }
        return papers.get(0);
    }

    private void validateSaveBo(QuestionSaveBo saveBo, Long excludeId) {
        String entry = StringUtils.trimToEmpty(saveBo.getPracticeEntry());
        if (QuestionPracticeEntry.CHAPTER.equals(entry)) {
            if (StringUtils.isBlank(saveBo.getChapterCode())) {
                throw new BizException("章节练习请选择所属章");
            }
        } else if (QuestionPracticeEntry.KNOWLEDGE.equals(entry)) {
            if (StringUtils.isBlank(saveBo.getPointCode())) {
                throw new BizException("知识点练习请选择知识点");
            }
        } else if (QuestionPracticeEntry.SPECIAL.equals(entry) || QuestionPracticeEntry.EXAM.equals(entry)) {
            if (StringUtils.isBlank(saveBo.getPaperCode())) {
                throw new BizException("请选择题集");
            }
            QuestionPaper paper = requirePaperByCode(saveBo.getPaperCode());
            if (!entry.equals(paper.getPaperType())) {
                throw new BizException("题集类型与练习入口不匹配");
            }
        } else {
            throw new BizException("请选择练习入口");
        }
        validateAnalysisAndVideo(saveBo);
        assertTitleUnique(StringUtils.trimToNull(saveBo.getTitle()), excludeId);
    }

    private void validateAnalysisAndVideo(QuestionSaveBo saveBo) {
        String analysis = StringUtils.trimToEmpty(saveBo.getAnalysis());
        if (StringUtils.isBlank(analysis) || analysis.startsWith("请填写")) {
            throw new BizException("请填写题目解析，每题需独立解析，不能使用占位文案");
        }
        if (StringUtils.isBlank(StringUtils.trimToNull(saveBo.getVideoUrl()))) {
            throw new BizException("请为每道题配置独立的讲解视频，不能使用默认视频");
        }
        assertUploadedMediaUrl(saveBo.getVideoUrl(), "讲解视频");
        if (StringUtils.isNotBlank(saveBo.getVideoPosterUrl())) {
            assertUploadedMediaUrl(saveBo.getVideoPosterUrl(), "视频封面");
        }
    }

    /**
     * 入库统一为 /upload/ 相对路径，避免 file_public_base_url 写入错误端口导致管理端/C 端无法播放
     */
    private String normalizeStoredMediaUrl(String rawUrl) {
        String url = StringUtils.trimToNull(rawUrl);
        if (url == null) {
            return null;
        }
        int uploadIdx = url.indexOf("/upload/");
        if (uploadIdx >= 0) {
            return url.substring(uploadIdx);
        }
        return url;
    }

    private void assertUploadedMediaUrl(String rawUrl, String label) {
        String url = StringUtils.trimToNull(rawUrl);
        if (url == null) {
            return;
        }
        String lower = url.toLowerCase(Locale.ROOT);
        if (lower.contains("commondatastorage.googleapis.com")
                || lower.contains("qiniu-web-assets")
                || lower.contains("w3schools.com")
                || lower.contains("sample-videos.com")) {
            throw new BizException(label + "请通过上传功能配置，不能使用外部演示链接");
        }
        if (!lower.startsWith("http://") && !lower.startsWith("https://")) {
            return;
        }
        String cdn = StringUtils.trimToEmpty(ConfigUtil.getConfigValue("file_cdn_domain"));
        String publicBase = StringUtils.trimToEmpty(ConfigUtil.getConfigValue("file_public_base_url"));
        if (StringUtils.isNotBlank(cdn) && lower.startsWith(cdn.toLowerCase(Locale.ROOT))) {
            return;
        }
        if (StringUtils.isNotBlank(publicBase) && lower.startsWith(publicBase.toLowerCase(Locale.ROOT))) {
            return;
        }
        if (lower.contains("localhost") || lower.contains("127.0.0.1")) {
            return;
        }
        throw new BizException(label + "请通过上传功能配置，不能使用未授权的外部链接");
    }

    private String resolveQuestionCode(String rawCode, Long excludeId) {
        String code = StringUtils.trimToNull(rawCode);
        if (code != null) {
            assertQuestionCodeUnique(code, excludeId);
            return code;
        }
        for (int i = 0; i < 8; i++) {
            String candidate = "q-" + System.currentTimeMillis() + "-" + ThreadLocalRandom.current().nextInt(1000, 10000);
            if (!isQuestionCodeExists(candidate, excludeId)) {
                return candidate;
            }
        }
        throw new BizException("系统生成题目编号失败，请稍后重试");
    }

    private CatalogContext resolveCatalogContext(QuestionSaveBo saveBo) {
        String entry = saveBo.getPracticeEntry();
        if (QuestionPracticeEntry.CHAPTER.equals(entry)) {
            QuestionChapter chapter = requireChapterByCode(saveBo.getChapterCode());
            QuestionSection section = resolveSectionByCode(chapter.getId(), saveBo.getSectionCode());
            return new CatalogContext(chapter.getId(), section == null ? null : section.getId());
        }
        if (QuestionPracticeEntry.KNOWLEDGE.equals(entry)) {
            KnowledgePoint point = requirePointByCode(saveBo.getPointCode());
            Long chapterId = findFallbackChapterId(point.getGrade(), point.getSubject(), saveBo.getVersion());
            return new CatalogContext(chapterId, null);
        }
        if (QuestionPracticeEntry.isPaperEntry(entry)) {
            QuestionPaper paper = requirePaperByCode(saveBo.getPaperCode());
            Long chapterId = findFallbackChapterId(paper.getGrade(), paper.getSubject(), saveBo.getVersion());
            return new CatalogContext(chapterId, null);
        }
        throw new BizException("请选择练习入口");
    }

    private Long findFallbackChapterId(String grade, String subject, String version) {
        QueryWrapper<QuestionChapter> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(StringUtils.isNotBlank(grade), QuestionChapter::getGrade, SetupQueryResolveSupport.resolveGradeValue(grade))
                .eq(StringUtils.isNotBlank(subject), QuestionChapter::getSubject, SetupQueryResolveSupport.resolveSubjectValue(subject))
                .eq(StringUtils.isNotBlank(version), QuestionChapter::getVersion, version)
                .eq(QuestionChapter::getState, QuestionChapterConstant.STATE_ENABLED)
                .orderByAsc(QuestionChapter::getSortOrder)
                .last("LIMIT 1");
        QuestionChapter chapter = iQuestionChapterService.getOne(queryWrapper, false);
        if (chapter == null) {
            throw new BizException("请先在「章节题库」中维护该学科/年级下的章节");
        }
        return chapter.getId();
    }

    private void bindPracticeTarget(Long questionId, QuestionSaveBo saveBo) {
        String entry = saveBo.getPracticeEntry();
        int sortOrder = saveBo.getSortOrder() == null ? 0 : saveBo.getSortOrder();
        if (QuestionPracticeEntry.KNOWLEDGE.equals(entry)) {
            KnowledgePoint point = requirePointByCode(saveBo.getPointCode());
            KnowledgePointQuestion link = new KnowledgePointQuestion();
            link.setPointId(point.getId());
            link.setQuestionId(questionId);
            link.setSortOrder(sortOrder);
            iKnowledgePointQuestionService.save(link);
            return;
        }
        if (QuestionPracticeEntry.isPaperEntry(entry)) {
            QuestionPaper paper = requirePaperByCode(saveBo.getPaperCode());
            QuestionPaperItem item = new QuestionPaperItem();
            item.setPaperId(paper.getId());
            item.setQuestionId(questionId);
            item.setSortOrder(sortOrder);
            iQuestionPaperItemService.save(item);
        }
    }

    private void clearPracticeBindings(Long questionId) {
        clearKnowledgeBindings(questionId);
        clearPaperBindings(questionId);
    }

    private void clearKnowledgeBindings(Long questionId) {
        LambdaQueryWrapper<KnowledgePointQuestion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KnowledgePointQuestion::getQuestionId, questionId);
        iKnowledgePointQuestionService.remove(wrapper);
    }

    private void clearPaperBindings(Long questionId) {
        LambdaQueryWrapper<QuestionPaperItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(QuestionPaperItem::getQuestionId, questionId);
        iQuestionPaperItemService.remove(wrapper);
    }

    private void refreshRelatedCounts(Long questionId) {
        Question question = questionId == null ? null : iQuestionService.getById(questionId);
        if (question != null && question.getChapterId() != null) {
            refreshChapterCount(question.getChapterId());
            if (question.getSectionId() != null) {
                refreshSectionCount(question.getSectionId());
            }
        }
        refreshAllKnowledgeCounts();
        refreshAllPaperCounts();
    }

    private void refreshChapterCount(Long chapterId) {
        LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Question::getChapterId, chapterId).eq(Question::getState, QuestionChapterConstant.STATE_ENABLED);
        long count = iQuestionService.count(wrapper);
        LambdaUpdateWrapper<QuestionChapter> update = new LambdaUpdateWrapper<>();
        update.eq(QuestionChapter::getId, chapterId).set(QuestionChapter::getQuestionCount, (int) count);
        iQuestionChapterService.update(new QuestionChapter(), update);
    }

    private void refreshSectionCount(Long sectionId) {
        LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Question::getSectionId, sectionId).eq(Question::getState, QuestionChapterConstant.STATE_ENABLED);
        long count = iQuestionService.count(wrapper);
        LambdaUpdateWrapper<QuestionSection> update = new LambdaUpdateWrapper<>();
        update.eq(QuestionSection::getId, sectionId).set(QuestionSection::getQuestionCount, (int) count);
        iQuestionSectionService.update(new QuestionSection(), update);
    }

    private void refreshAllKnowledgeCounts() {
        List<KnowledgePoint> points = iKnowledgePointService.list();
        for (KnowledgePoint point : points) {
            LambdaQueryWrapper<KnowledgePointQuestion> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(KnowledgePointQuestion::getPointId, point.getId());
            long count = iKnowledgePointQuestionService.count(wrapper);
            LambdaUpdateWrapper<KnowledgePoint> update = new LambdaUpdateWrapper<>();
            update.eq(KnowledgePoint::getId, point.getId()).set(KnowledgePoint::getQuestionCount, (int) count);
            iKnowledgePointService.update(new KnowledgePoint(), update);
        }
    }

    private void refreshAllPaperCounts() {
        List<QuestionPaper> papers = iQuestionPaperService.list();
        for (QuestionPaper paper : papers) {
            LambdaQueryWrapper<QuestionPaperItem> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(QuestionPaperItem::getPaperId, paper.getId());
            long count = iQuestionPaperItemService.count(wrapper);
            LambdaUpdateWrapper<QuestionPaper> update = new LambdaUpdateWrapper<>();
            update.eq(QuestionPaper::getId, paper.getId()).set(QuestionPaper::getQuestionCount, (int) count);
            iQuestionPaperService.update(new QuestionPaper(), update);
        }
    }

    private Set<Long> resolveQuestionIdsByPracticeEntry(String practiceEntry, String subject) {
        if (StringUtils.isBlank(practiceEntry)) {
            return null;
        }
        String entry = practiceEntry.trim();
        if (QuestionPracticeEntry.isPaperEntry(entry)) {
            LambdaQueryWrapper<QuestionPaper> paperWrapper = new LambdaQueryWrapper<>();
            paperWrapper.eq(QuestionPaper::getPaperType, entry);
            if (StringUtils.isNotBlank(subject)) {
                paperWrapper.eq(QuestionPaper::getSubject, SetupQueryResolveSupport.resolveSubjectValue(subject));
            }
            List<QuestionPaper> papers = iQuestionPaperService.list(paperWrapper);
            if (papers.isEmpty()) {
                return Collections.emptySet();
            }
            Set<Long> paperIds = papers.stream().map(QuestionPaper::getId).collect(Collectors.toSet());
            LambdaQueryWrapper<QuestionPaperItem> itemWrapper = new LambdaQueryWrapper<>();
            itemWrapper.in(QuestionPaperItem::getPaperId, paperIds);
            return iQuestionPaperItemService.list(itemWrapper).stream()
                    .map(QuestionPaperItem::getQuestionId)
                    .collect(Collectors.toSet());
        }
        if (QuestionPracticeEntry.KNOWLEDGE.equals(entry)) {
            LambdaQueryWrapper<KnowledgePoint> pointWrapper = new LambdaQueryWrapper<>();
            if (StringUtils.isNotBlank(subject)) {
                pointWrapper.eq(KnowledgePoint::getSubject, SetupQueryResolveSupport.resolveSubjectValue(subject));
            }
            List<KnowledgePoint> points = iKnowledgePointService.list(pointWrapper);
            if (points.isEmpty()) {
                return Collections.emptySet();
            }
            Set<Long> pointIds = points.stream().map(KnowledgePoint::getId).collect(Collectors.toSet());
            LambdaQueryWrapper<KnowledgePointQuestion> linkWrapper = new LambdaQueryWrapper<>();
            linkWrapper.in(KnowledgePointQuestion::getPointId, pointIds);
            Set<Long> knowledgeQuestionIds = iKnowledgePointQuestionService.list(linkWrapper).stream()
                    .map(KnowledgePointQuestion::getQuestionId)
                    .collect(Collectors.toSet());
            Set<Long> paperQuestionIds = loadAllPaperQuestionIds();
            knowledgeQuestionIds.removeAll(paperQuestionIds);
            return knowledgeQuestionIds;
        }
        if (QuestionPracticeEntry.CHAPTER.equals(entry)) {
            Set<Long> paperQuestionIds = loadAllPaperQuestionIds();
            LambdaQueryWrapper<KnowledgePointQuestion> linkWrapper = new LambdaQueryWrapper<>();
            Set<Long> knowledgeQuestionIds = iKnowledgePointQuestionService.list(linkWrapper).stream()
                    .map(KnowledgePointQuestion::getQuestionId)
                    .collect(Collectors.toSet());
            QueryWrapper<Question> queryWrapper = new QueryWrapper<>();
            queryWrapper.select("id");
            List<Question> questions = iQuestionService.list(queryWrapper);
            return questions.stream()
                    .map(Question::getId)
                    .filter(id -> !paperQuestionIds.contains(id) && !knowledgeQuestionIds.contains(id))
                    .collect(Collectors.toSet());
        }
        return null;
    }

    private Set<Long> loadAllPaperQuestionIds() {
        return iQuestionPaperItemService.list().stream()
                .map(QuestionPaperItem::getQuestionId)
                .collect(Collectors.toSet());
    }

    private PracticeBindingMaps loadPracticeBindingMaps(List<Question> questions) {
        if (CollectionUtils.isEmpty(questions)) {
            return PracticeBindingMaps.empty();
        }
        Set<Long> questionIds = questions.stream().map(Question::getId).collect(Collectors.toSet());

        LambdaQueryWrapper<QuestionPaperItem> paperItemWrapper = new LambdaQueryWrapper<>();
        paperItemWrapper.in(QuestionPaperItem::getQuestionId, questionIds);
        List<QuestionPaperItem> paperItems = iQuestionPaperItemService.list(paperItemWrapper);
        Map<Long, QuestionPaperItem> paperItemByQuestion = paperItems.stream()
                .collect(Collectors.toMap(QuestionPaperItem::getQuestionId, item -> item, (a, b) -> a));

        Set<Long> paperIds = paperItems.stream().map(QuestionPaperItem::getPaperId).collect(Collectors.toSet());
        Map<Long, QuestionPaper> paperMap = paperIds.isEmpty() ? Collections.emptyMap()
                : iQuestionPaperService.listByIds(paperIds).stream()
                .collect(Collectors.toMap(QuestionPaper::getId, item -> item, (a, b) -> a));

        LambdaQueryWrapper<KnowledgePointQuestion> knowledgeWrapper = new LambdaQueryWrapper<>();
        knowledgeWrapper.in(KnowledgePointQuestion::getQuestionId, questionIds);
        List<KnowledgePointQuestion> knowledgeLinks = iKnowledgePointQuestionService.list(knowledgeWrapper);
        Map<Long, KnowledgePointQuestion> knowledgeByQuestion = knowledgeLinks.stream()
                .collect(Collectors.toMap(KnowledgePointQuestion::getQuestionId, item -> item, (a, b) -> a));

        Set<Long> pointIds = knowledgeLinks.stream().map(KnowledgePointQuestion::getPointId).collect(Collectors.toSet());
        Map<Long, KnowledgePoint> pointMap = pointIds.isEmpty() ? Collections.emptyMap()
                : iKnowledgePointService.listByIds(pointIds).stream()
                .collect(Collectors.toMap(KnowledgePoint::getId, item -> item, (a, b) -> a));

        return new PracticeBindingMaps(paperItemByQuestion, paperMap, knowledgeByQuestion, pointMap);
    }

    private <T> void applyGradeSubjectFilter(LambdaQueryWrapper<T> wrapper, String grade, String subject,
                                             SFunction<T, String> gradeCol, SFunction<T, String> subjectCol) {
        if (StringUtils.isNotBlank(grade)) {
            String gradeValue = SetupQueryResolveSupport.resolveGradeValue(grade);
            if (StringUtils.isNotBlank(gradeValue)) {
                wrapper.eq(gradeCol, gradeValue);
            }
        }
        if (StringUtils.isNotBlank(subject)) {
            String subjectValue = SetupQueryResolveSupport.resolveSubjectValue(subject);
            if (StringUtils.isNotBlank(subjectValue)) {
                wrapper.eq(subjectCol, subjectValue);
            }
        }
    }

    private boolean isQuestionCodeExists(String questionCode, Long excludeId) {
        if (StringUtils.isBlank(questionCode)) {
            return false;
        }
        QueryWrapper<Question> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(Question::getQuestionCode, questionCode.trim());
        if (excludeId != null) {
            queryWrapper.lambda().ne(Question::getId, excludeId);
        }
        return iQuestionService.count(queryWrapper) > 0;
    }

    private boolean isTitleExists(String title, Long excludeId) {
        if (StringUtils.isBlank(title)) {
            return false;
        }
        QueryWrapper<Question> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(Question::getTitle, title.trim());
        if (excludeId != null) {
            queryWrapper.lambda().ne(Question::getId, excludeId);
        }
        return iQuestionService.count(queryWrapper) > 0;
    }

    private Question buildQuestionEntity(QuestionSaveBo saveBo, String questionCode, Long chapterId, Long sectionId) {
        Question question = new Question();
        question.setQuestionCode(questionCode);
        question.setChapterId(chapterId);
        question.setSectionId(sectionId);
        question.setTitle(saveBo.getTitle());
        question.setQuestionType(saveBo.getQuestionType());
        question.setDifficulty(saveBo.getDifficulty());
        question.setRightAnswer(saveBo.getRightAnswer());
        question.setAnalysis(saveBo.getAnalysis());
        question.setVideoUrl(normalizeStoredMediaUrl(saveBo.getVideoUrl()));
        question.setVideoPosterUrl(normalizeStoredMediaUrl(saveBo.getVideoPosterUrl()));
        question.setVideoDuration(StringUtils.trimToNull(saveBo.getVideoDuration()));
        question.setVideoTeacher(StringUtils.trimToNull(saveBo.getVideoTeacher()));
        question.setVideoSummary(StringUtils.trimToNull(saveBo.getVideoSummary()));
        question.setOptionsJson(serializeOptions(saveBo.getOptions()));
        question.setTagsJson(serializeTags(saveBo.getTags()));
        question.setSortOrder(saveBo.getSortOrder());
        question.setState(saveBo.getState());
        return question;
    }

    private String serializeOptions(List<QuestionOptionBo> options) {
        if (CollectionUtils.isEmpty(options)) {
            return null;
        }
        List<AppQuestionOptionVO> optionVos = options.stream().map(item -> {
            AppQuestionOptionVO vo = new AppQuestionOptionVO();
            vo.setLabel(StringUtils.trim(item.getLabel()));
            vo.setValue(StringUtils.trim(item.getValue()));
            return vo;
        }).collect(Collectors.toList());
        return JSON.toJSONString(optionVos);
    }

    private String serializeTags(List<String> tags) {
        if (CollectionUtils.isEmpty(tags)) {
            return null;
        }
        List<String> normalized = tags.stream()
                .filter(StringUtils::isNotBlank)
                .map(StringUtils::trim)
                .distinct()
                .collect(Collectors.toList());
        return normalized.isEmpty() ? null : JSON.toJSONString(normalized);
    }

    private void assertQuestionCodeUnique(String questionCode, Long excludeId) {
        if (isQuestionCodeExists(questionCode, excludeId)) {
            throw new BizException("题目编号已存在：" + questionCode);
        }
    }

    private void assertTitleUnique(String title, Long excludeId) {
        if (isTitleExists(title, excludeId)) {
            throw new BizException("题干已存在，请勿重复录入相同题目：" + title);
        }
    }

    private QuestionChapter requireChapterByCode(String chapterCode) {
        QueryWrapper<QuestionChapter> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(QuestionChapter::getChapterCode, chapterCode);
        QuestionChapter chapter = iQuestionChapterService.getOne(queryWrapper, false);
        if (Objects.isNull(chapter)) {
            throw new BizException("所属章不存在，请先在章节题库中创建");
        }
        return chapter;
    }

    private QuestionSection resolveSectionByCode(Long chapterId, String sectionCode) {
        if (StringUtils.isBlank(sectionCode)) {
            return null;
        }
        QueryWrapper<QuestionSection> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(QuestionSection::getSectionCode, sectionCode.trim())
                .eq(QuestionSection::getChapterId, chapterId);
        QuestionSection section = iQuestionSectionService.getOne(queryWrapper, false);
        if (Objects.isNull(section)) {
            throw new BizException("所属节不存在或不属于该章");
        }
        return section;
    }

    private KnowledgePoint requirePointByCode(String pointCode) {
        QueryWrapper<KnowledgePoint> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(KnowledgePoint::getPointCode, StringUtils.trim(pointCode));
        KnowledgePoint point = iKnowledgePointService.getOne(queryWrapper, false);
        if (point == null) {
            throw new BizException("知识点不存在，请先维护知识点");
        }
        return point;
    }

    private QuestionPaper requirePaperByCode(String paperCode) {
        QueryWrapper<QuestionPaper> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(QuestionPaper::getPaperCode, StringUtils.trim(paperCode));
        QuestionPaper paper = iQuestionPaperService.getOne(queryWrapper, false);
        if (paper == null) {
            throw new BizException("题集不存在，请先在题集管理中创建");
        }
        return paper;
    }

    private Map<Long, QuestionChapter> loadChapterMapForQuestionQuery(QuestionPageBo pageBo) {
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
                .eq(StringUtils.isNotBlank(pageBo.getGrade()), QuestionChapter::getGrade, SetupQueryResolveSupport.resolveGradeValue(pageBo.getGrade()))
                .eq(StringUtils.isNotBlank(pageBo.getSubject()), QuestionChapter::getSubject, SetupQueryResolveSupport.resolveSubjectValue(pageBo.getSubject()))
                .eq(StringUtils.isNotBlank(pageBo.getVersion()), QuestionChapter::getVersion, pageBo.getVersion());
        List<QuestionChapter> chapters = iQuestionChapterService.list(queryWrapper);
        if (CollectionUtils.isEmpty(chapters)) {
            return Collections.emptyMap();
        }
        return chapters.stream().collect(Collectors.toMap(QuestionChapter::getId, item -> item, (a, b) -> a));
    }

    private Set<Long> loadSectionIdsForQuestionQuery(QuestionPageBo pageBo, Map<Long, QuestionChapter> chapterMap) {
        boolean hasSectionFilter = StringUtils.isNotBlank(pageBo.getSectionCode())
                || StringUtils.isNotBlank(pageBo.getSectionTitle());
        if (!hasSectionFilter) {
            return null;
        }

        QueryWrapper<QuestionSection> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .like(StringUtils.isNotBlank(pageBo.getSectionCode()), QuestionSection::getSectionCode, pageBo.getSectionCode())
                .like(StringUtils.isNotBlank(pageBo.getSectionTitle()), QuestionSection::getTitle, pageBo.getSectionTitle());
        if (chapterMap != null) {
            queryWrapper.lambda().in(QuestionSection::getChapterId, chapterMap.keySet());
        }
        List<QuestionSection> sections = iQuestionSectionService.list(queryWrapper);
        if (CollectionUtils.isEmpty(sections)) {
            return Collections.emptySet();
        }
        return sections.stream().map(QuestionSection::getId).collect(Collectors.toSet());
    }

    private RelationContext loadRelationContext(List<Question> questions, Map<Long, QuestionChapter> presetChapterMap) {
        if (CollectionUtils.isEmpty(questions)) {
            return new RelationContext(Collections.emptyMap(), Collections.emptyMap());
        }

        Map<Long, QuestionChapter> chapterMap = presetChapterMap;
        if (chapterMap == null) {
            Set<Long> chapterIds = questions.stream()
                    .map(Question::getChapterId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            chapterMap = loadChaptersByIds(chapterIds);
        }

        Set<Long> sectionIds = questions.stream()
                .map(Question::getSectionId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, QuestionSection> sectionMap = loadSectionsByIds(sectionIds);
        return new RelationContext(chapterMap, sectionMap);
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

    private Map<Long, QuestionSection> loadSectionsByIds(Set<Long> sectionIds) {
        if (CollectionUtils.isEmpty(sectionIds)) {
            return Collections.emptyMap();
        }
        List<QuestionSection> sections = iQuestionSectionService.listByIds(sectionIds);
        if (CollectionUtils.isEmpty(sections)) {
            return Collections.emptyMap();
        }
        return sections.stream().collect(Collectors.toMap(QuestionSection::getId, item -> item, (a, b) -> a));
    }

    private List<QuestionVo> toQuestionVoList(List<Question> questions, RelationContext relationContext,
                                              PracticeBindingMaps bindingMaps) {
        if (CollectionUtils.isEmpty(questions)) {
            return Collections.emptyList();
        }
        List<QuestionVo> voList = new ArrayList<>(questions.size());
        for (Question question : questions) {
            voList.add(toQuestionVo(question, relationContext, bindingMaps));
        }
        return voList;
    }

    private QuestionVo toQuestionVo(Question question, RelationContext relationContext, PracticeBindingMaps bindingMaps) {
        QuestionVo vo = new QuestionVo();
        BeanUtils.copyProperties(question, vo);
        vo.setOptions(QuestionOptionsSupport.parseOptions(question.getOptionsJson()));
        vo.setTags(QuestionTagsSupport.parseTags(question.getTagsJson()));

        QuestionChapter chapter = relationContext.chapterMap.get(question.getChapterId());
        if (chapter != null) {
            vo.setChapterCode(chapter.getChapterCode());
            vo.setChapterTitle(chapter.getTitle());
            vo.setGrade(chapter.getGrade());
            vo.setSubject(chapter.getSubject());
            vo.setVersion(chapter.getVersion());
        }

        if (question.getSectionId() != null) {
            QuestionSection section = relationContext.sectionMap.get(question.getSectionId());
            if (section != null) {
                vo.setSectionCode(section.getSectionCode());
                vo.setSectionTitle(section.getTitle());
            }
        }

        applyPracticeBinding(vo, question.getId(), bindingMaps);
        return vo;
    }

    private void applyPracticeBinding(QuestionVo vo, Long questionId, PracticeBindingMaps bindingMaps) {
        QuestionPaperItem paperItem = bindingMaps.paperItemByQuestion.get(questionId);
        if (paperItem != null) {
            QuestionPaper paper = bindingMaps.paperMap.get(paperItem.getPaperId());
            if (paper != null) {
                vo.setPracticeEntry(paper.getPaperType());
                vo.setPracticeEntryLabel(QuestionPracticeEntry.labelOf(paper.getPaperType()));
                vo.setPaperCode(paper.getPaperCode());
                vo.setPaperTitle(paper.getTitle());
                vo.setBindTarget(paper.getTitle());
                return;
            }
        }

        KnowledgePointQuestion knowledgeLink = bindingMaps.knowledgeByQuestion.get(questionId);
        if (knowledgeLink != null) {
            KnowledgePoint point = bindingMaps.pointMap.get(knowledgeLink.getPointId());
            if (point != null) {
                vo.setPracticeEntry(QuestionPracticeEntry.KNOWLEDGE);
                vo.setPracticeEntryLabel(QuestionPracticeEntry.labelOf(QuestionPracticeEntry.KNOWLEDGE));
                vo.setPointCode(point.getPointCode());
                vo.setPointTitle(point.getTitle());
                vo.setBindTarget(point.getTitle());
                return;
            }
        }

        vo.setPracticeEntry(QuestionPracticeEntry.CHAPTER);
        vo.setPracticeEntryLabel(QuestionPracticeEntry.labelOf(QuestionPracticeEntry.CHAPTER));
        if (StringUtils.isNotBlank(vo.getSectionTitle())) {
            vo.setBindTarget(vo.getChapterTitle() + " / " + vo.getSectionTitle());
        } else {
            vo.setBindTarget(vo.getChapterTitle());
        }
    }

    private static final class CatalogContext {
        private final Long chapterId;
        private final Long sectionId;

        private CatalogContext(Long chapterId, Long sectionId) {
            this.chapterId = chapterId;
            this.sectionId = sectionId;
        }
    }

    private static final class RelationContext {
        private final Map<Long, QuestionChapter> chapterMap;
        private final Map<Long, QuestionSection> sectionMap;

        private RelationContext(Map<Long, QuestionChapter> chapterMap, Map<Long, QuestionSection> sectionMap) {
            this.chapterMap = chapterMap != null ? chapterMap : Collections.emptyMap();
            this.sectionMap = sectionMap != null ? sectionMap : Collections.emptyMap();
        }
    }

    private static final class PracticeBindingMaps {
        private final Map<Long, QuestionPaperItem> paperItemByQuestion;
        private final Map<Long, QuestionPaper> paperMap;
        private final Map<Long, KnowledgePointQuestion> knowledgeByQuestion;
        private final Map<Long, KnowledgePoint> pointMap;

        private PracticeBindingMaps(Map<Long, QuestionPaperItem> paperItemByQuestion,
                                    Map<Long, QuestionPaper> paperMap,
                                    Map<Long, KnowledgePointQuestion> knowledgeByQuestion,
                                    Map<Long, KnowledgePoint> pointMap) {
            this.paperItemByQuestion = paperItemByQuestion;
            this.paperMap = paperMap;
            this.knowledgeByQuestion = knowledgeByQuestion;
            this.pointMap = pointMap;
        }

        private static PracticeBindingMaps empty() {
            return new PracticeBindingMaps(Collections.emptyMap(), Collections.emptyMap(),
                    Collections.emptyMap(), Collections.emptyMap());
        }
    }
}
