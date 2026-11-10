package com.sqx.app.base.question.paper.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sqx.app.base.enums.AppError;
import com.sqx.app.base.question.paper.domain.bo.*;
import com.sqx.app.base.question.paper.domain.vo.QuestionPaperItemVo;
import com.sqx.app.base.question.paper.domain.vo.QuestionPaperQuestionOptionVo;
import com.sqx.app.base.question.paper.domain.vo.QuestionPaperVo;
import com.sqx.app.base.question.paper.service.QuestionPaperAdminService;
import com.sqx.common.core.exception.BizException;
import com.sqx.common.mybatis.utils.PageUtil;
import com.sqx.system.db.entity.Question;
import com.sqx.system.db.entity.QuestionPaper;
import com.sqx.system.db.entity.QuestionPaperItem;
import com.sqx.system.db.service.IQuestionPaperItemService;
import com.sqx.system.db.service.IQuestionPaperService;
import com.sqx.system.db.service.IQuestionService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 管理端题集维护实现
 */
@Service
public class QuestionPaperAdminServiceImpl implements QuestionPaperAdminService {

    @Resource
    private IQuestionPaperService iQuestionPaperService;

    @Resource
    private IQuestionPaperItemService iQuestionPaperItemService;

    @Resource
    private IQuestionService iQuestionService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveInfo(QuestionPaperSaveBo saveBo) {
        String paperCode = resolvePaperCode(saveBo.getPaperCode(), null, null);

        QuestionPaper paper = buildPaperFromSaveBo(saveBo, paperCode);
        paper.setQuestionCount(countValidItems(saveBo.getItems()));
        iQuestionPaperService.save(paper);

        syncPaperItems(paper.getId(), saveBo.getItems());
    }

    @Override
    public IPage<QuestionPaperVo> pageInfo(QuestionPaperPageBo pageBo) {
        Page<QuestionPaper> initPage = PageUtil.initPage(pageBo.getPage(), pageBo.getLimit());
        QueryWrapper<QuestionPaper> queryWrapper = buildPaperQuery(pageBo);
        queryWrapper.lambda().orderByAsc(QuestionPaper::getSortOrder).orderByDesc(QuestionPaper::getCreateTime);

        IPage<QuestionPaper> page = iQuestionPaperService.page(initPage, queryWrapper);
        List<QuestionPaperVo> voList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(page.getRecords())) {
            for (QuestionPaper record : page.getRecords()) {
                QuestionPaperVo vo = new QuestionPaperVo();
                BeanUtils.copyProperties(record, vo);
                voList.add(vo);
            }
        }
        return PageUtil.copyPage(page, voList);
    }

    @Override
    public QuestionPaperVo info(QuestionPaperInfoBo infoBo) {
        QuestionPaper paper = iQuestionPaperService.getById(infoBo.getId());
        if (Objects.isNull(paper)) {
            throw new BizException(AppError.INFO_NOT_FOUND);
        }
        QuestionPaperVo vo = new QuestionPaperVo();
        BeanUtils.copyProperties(paper, vo);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateInfo(QuestionPaperUpdateBo updateBo) {
        QuestionPaper existing = iQuestionPaperService.getById(updateBo.getId());
        if (Objects.isNull(existing)) {
            throw new BizException(AppError.INFO_NOT_FOUND);
        }
        String paperCode = resolvePaperCode(updateBo.getPaperCode(), updateBo.getId(), existing.getPaperCode());

        QuestionPaper paper = buildPaperFromSaveBo(updateBo, paperCode);
        paper.setId(updateBo.getId());
        if (updateBo.getItems() != null) {
            paper.setQuestionCount(countValidItems(updateBo.getItems()));
            iQuestionPaperService.updateById(paper);
            removePaperItems(updateBo.getId());
            syncPaperItems(updateBo.getId(), updateBo.getItems());
            return;
        }
        paper.setQuestionCount(existing.getQuestionCount());
        iQuestionPaperService.updateById(paper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(QuestionPaperInfoBo infoBo) {
        QuestionPaper paper = iQuestionPaperService.getById(infoBo.getId());
        if (Objects.isNull(paper)) {
            throw new BizException(AppError.INFO_NOT_FOUND);
        }
        removePaperItems(infoBo.getId());
        iQuestionPaperService.removeById(infoBo.getId());
    }

    @Override
    public void updateState(QuestionPaperUpdateStateBo updateStateBo) {
        LambdaUpdateWrapper<QuestionPaper> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper
                .eq(QuestionPaper::getId, updateStateBo.getId())
                .set(QuestionPaper::getState, updateStateBo.getState());
        iQuestionPaperService.update(new QuestionPaper(), updateWrapper);
    }

    @Override
    public IPage<QuestionPaperQuestionOptionVo> searchQuestions(QuestionPaperQuestionSearchBo searchBo) {
        Page<Question> initPage = PageUtil.initPage(searchBo.getPage(), searchBo.getLimit());
        QueryWrapper<Question> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .like(StringUtils.isNotBlank(searchBo.getQuestionCode()), Question::getQuestionCode, searchBo.getQuestionCode())
                .like(StringUtils.isNotBlank(searchBo.getTitle()), Question::getTitle, searchBo.getTitle())
                .orderByAsc(Question::getSortOrder)
                .orderByDesc(Question::getCreateTime);

        IPage<Question> page = iQuestionService.page(initPage, queryWrapper);
        List<QuestionPaperQuestionOptionVo> voList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(page.getRecords())) {
            for (Question record : page.getRecords()) {
                QuestionPaperQuestionOptionVo vo = new QuestionPaperQuestionOptionVo();
                vo.setQuestionId(record.getId());
                vo.setQuestionCode(record.getQuestionCode());
                vo.setTitle(record.getTitle());
                vo.setQuestionType(record.getQuestionType());
                vo.setDifficulty(record.getDifficulty());
                voList.add(vo);
            }
        }
        return PageUtil.copyPage(page, voList);
    }

    @Override
    public IPage<QuestionPaperItemVo> pageItems(QuestionPaperItemPageBo pageBo) {
        requirePaper(pageBo.getPaperId());

        Set<Long> filteredQuestionIds = resolveFilteredQuestionIds(pageBo.getQuestionCode(), pageBo.getTitle());
        if (filteredQuestionIds != null && filteredQuestionIds.isEmpty()) {
            Page<QuestionPaperItem> emptyPage = PageUtil.initPage(pageBo.getPage(), pageBo.getLimit());
            return PageUtil.copyPage(emptyPage, Collections.emptyList());
        }

        Page<QuestionPaperItem> initPage = PageUtil.initPage(pageBo.getPage(), pageBo.getLimit());
        LambdaQueryWrapper<QuestionPaperItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(QuestionPaperItem::getPaperId, pageBo.getPaperId());
        if (filteredQuestionIds != null) {
            wrapper.in(QuestionPaperItem::getQuestionId, filteredQuestionIds);
        }
        wrapper.orderByAsc(QuestionPaperItem::getSortOrder).orderByAsc(QuestionPaperItem::getId);

        IPage<QuestionPaperItem> page = iQuestionPaperItemService.page(initPage, wrapper);
        return PageUtil.copyPage(page, toPaperItemVoList(page.getRecords()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void bindQuestions(QuestionPaperBindBo bindBo) {
        QuestionPaper paper = requirePaper(bindBo.getPaperId());
        List<Long> questionIds = resolveBindQuestionIds(bindBo);
        if (questionIds.isEmpty()) {
            throw new BizException("请选择要绑定的题目");
        }

        Set<Long> boundIds = loadBoundQuestionIds(paper.getId());
        Map<Long, Question> questionMap = loadQuestionMapByIds(new HashSet<>(questionIds));

        int nextSort = resolveNextSortOrder(paper.getId());
        List<QuestionPaperItem> entities = new ArrayList<>();
        for (Long questionId : questionIds) {
            if (boundIds.contains(questionId)) {
                continue;
            }
            Question question = questionMap.get(questionId);
            if (question == null) {
                throw new BizException("题目不存在：" + questionId);
            }
            QuestionPaperItem item = new QuestionPaperItem();
            item.setPaperId(paper.getId());
            item.setQuestionId(question.getId());
            item.setSortOrder(nextSort++);
            entities.add(item);
            boundIds.add(questionId);
        }
        if (entities.isEmpty()) {
            return;
        }
        iQuestionPaperItemService.saveBatch(entities);
        refreshQuestionCount(paper.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unbindQuestion(QuestionPaperUnbindBo unbindBo) {
        requirePaper(unbindBo.getPaperId());
        Long questionId = resolveUnbindQuestionId(unbindBo);

        LambdaQueryWrapper<QuestionPaperItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(QuestionPaperItem::getPaperId, unbindBo.getPaperId())
                .eq(QuestionPaperItem::getQuestionId, questionId);
        if (!iQuestionPaperItemService.remove(wrapper)) {
            throw new BizException("题集中未绑定该题目");
        }
        refreshQuestionCount(unbindBo.getPaperId());
    }

    private List<Long> resolveBindQuestionIds(QuestionPaperBindBo bindBo) {
        LinkedHashSet<Long> ids = new LinkedHashSet<>();
        if (CollectionUtils.isNotEmpty(bindBo.getQuestionIds())) {
            bindBo.getQuestionIds().stream()
                    .filter(Objects::nonNull)
                    .forEach(ids::add);
        }
        if (CollectionUtils.isNotEmpty(bindBo.getQuestionCodes())) {
            Map<String, Question> questionMap = loadQuestionMapByCodes(bindBo.getQuestionCodes().stream()
                    .filter(StringUtils::isNotBlank)
                    .map(StringUtils::trim)
                    .collect(Collectors.toSet()));
            for (String code : bindBo.getQuestionCodes()) {
                if (StringUtils.isBlank(code)) {
                    continue;
                }
                Question question = questionMap.get(StringUtils.trim(code));
                if (question == null) {
                    throw new BizException("题目不存在：" + StringUtils.trim(code));
                }
                ids.add(question.getId());
            }
        }
        return new ArrayList<>(ids);
    }

    private Long resolveUnbindQuestionId(QuestionPaperUnbindBo unbindBo) {
        if (unbindBo.getQuestionId() != null) {
            Question question = iQuestionService.getById(unbindBo.getQuestionId());
            if (question == null) {
                throw new BizException("题目不存在");
            }
            return question.getId();
        }
        if (StringUtils.isNotBlank(unbindBo.getQuestionCode())) {
            Map<String, Question> questionMap = loadQuestionMapByCodes(
                    Collections.singleton(StringUtils.trim(unbindBo.getQuestionCode())));
            Question question = questionMap.get(StringUtils.trim(unbindBo.getQuestionCode()));
            if (question == null) {
                throw new BizException("题目不存在");
            }
            return question.getId();
        }
        throw new BizException("请指定要移除的题目");
    }

    private QuestionPaper requirePaper(Long paperId) {
        QuestionPaper paper = iQuestionPaperService.getById(paperId);
        if (paper == null) {
            throw new BizException(AppError.INFO_NOT_FOUND);
        }
        return paper;
    }

    private Set<Long> resolveFilteredQuestionIds(String questionCode, String title) {
        if (StringUtils.isBlank(questionCode) && StringUtils.isBlank(title)) {
            return null;
        }
        LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.isNotBlank(questionCode), Question::getQuestionCode, questionCode)
                .like(StringUtils.isNotBlank(title), Question::getTitle, title);
        List<Question> questions = iQuestionService.list(wrapper);
        if (CollectionUtils.isEmpty(questions)) {
            return Collections.emptySet();
        }
        return questions.stream().map(Question::getId).collect(Collectors.toSet());
    }

    private List<QuestionPaperItemVo> toPaperItemVoList(List<QuestionPaperItem> items) {
        if (CollectionUtils.isEmpty(items)) {
            return Collections.emptyList();
        }
        List<Long> questionIds = items.stream()
                .map(QuestionPaperItem::getQuestionId)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, Question> questionMap = iQuestionService.listByIds(questionIds).stream()
                .collect(Collectors.toMap(Question::getId, Function.identity(), (a, b) -> a));

        List<QuestionPaperItemVo> voList = new ArrayList<>();
        for (QuestionPaperItem item : items) {
            QuestionPaperItemVo vo = new QuestionPaperItemVo();
            vo.setId(item.getId());
            vo.setSortOrder(item.getSortOrder());
            Question question = questionMap.get(item.getQuestionId());
            if (question != null) {
                vo.setQuestionId(question.getId());
                vo.setQuestionCode(question.getQuestionCode());
                vo.setQuestionTitle(question.getTitle());
                vo.setQuestionType(question.getQuestionType());
            }
            voList.add(vo);
        }
        return voList;
    }

    private Set<Long> loadBoundQuestionIds(Long paperId) {
        LambdaQueryWrapper<QuestionPaperItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(QuestionPaperItem::getPaperId, paperId);
        List<QuestionPaperItem> items = iQuestionPaperItemService.list(wrapper);
        if (CollectionUtils.isEmpty(items)) {
            return new HashSet<>();
        }
        return items.stream()
                .map(QuestionPaperItem::getQuestionId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private int resolveNextSortOrder(Long paperId) {
        LambdaQueryWrapper<QuestionPaperItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(QuestionPaperItem::getPaperId, paperId)
                .orderByDesc(QuestionPaperItem::getSortOrder)
                .last("LIMIT 1");
        QuestionPaperItem last = iQuestionPaperItemService.getOne(wrapper, false);
        if (last == null || last.getSortOrder() == null) {
            return 1;
        }
        return last.getSortOrder() + 1;
    }

    private void refreshQuestionCount(Long paperId) {
        LambdaQueryWrapper<QuestionPaperItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(QuestionPaperItem::getPaperId, paperId);
        long count = iQuestionPaperItemService.count(wrapper);

        LambdaUpdateWrapper<QuestionPaper> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(QuestionPaper::getId, paperId).set(QuestionPaper::getQuestionCount, count);
        iQuestionPaperService.update(new QuestionPaper(), updateWrapper);
    }

    private QuestionPaper buildPaperFromSaveBo(QuestionPaperSaveBo saveBo, String paperCode) {
        QuestionPaper paper = new QuestionPaper();
        paper.setPaperCode(paperCode);
        paper.setTitle(saveBo.getTitle());
        paper.setPaperType(saveBo.getPaperType());
        paper.setDescription(saveBo.getDescription());
        paper.setGrade(saveBo.getGrade());
        paper.setGradeShort(saveBo.getGradeShort());
        paper.setSubject(saveBo.getSubject());
        paper.setSortOrder(saveBo.getSortOrder());
        paper.setState(saveBo.getState());
        return paper;
    }

    private String resolvePaperCode(String rawCode, Long excludeId, String fallback) {
        String code = StringUtils.trimToNull(rawCode);
        if (code != null) {
            assertPaperCodeUnique(code, excludeId);
            return code;
        }
        if (StringUtils.isNotBlank(fallback)) {
            return fallback;
        }
        for (int i = 0; i < 8; i++) {
            String candidate = "paper-" + System.currentTimeMillis() + "-" + ThreadLocalRandom.current().nextInt(1000, 10000);
            if (!isPaperCodeExists(candidate, excludeId)) {
                return candidate;
            }
        }
        throw new BizException("系统生成题集编号失败，请稍后重试");
    }

    private boolean isPaperCodeExists(String paperCode, Long excludeId) {
        QueryWrapper<QuestionPaper> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(QuestionPaper::getPaperCode, paperCode);
        if (excludeId != null) {
            queryWrapper.lambda().ne(QuestionPaper::getId, excludeId);
        }
        return iQuestionPaperService.count(queryWrapper) > 0;
    }

    private void assertPaperCodeUnique(String paperCode, Long excludeId) {
        if (isPaperCodeExists(paperCode, excludeId)) {
            throw new BizException("题集编号已存在：" + paperCode);
        }
    }

    private int countValidItems(List<QuestionPaperItemSaveBo> items) {
        if (CollectionUtils.isEmpty(items)) {
            return 0;
        }
        return (int) items.stream()
                .filter(item -> item != null && StringUtils.isNotBlank(item.getQuestionCode()))
                .count();
    }

    private void syncPaperItems(Long paperId, List<QuestionPaperItemSaveBo> items) {
        if (paperId == null || CollectionUtils.isEmpty(items)) {
            return;
        }

        List<QuestionPaperItemSaveBo> validItems = items.stream()
                .filter(item -> item != null && StringUtils.isNotBlank(item.getQuestionCode()))
                .collect(Collectors.toList());
        if (validItems.isEmpty()) {
            return;
        }

        Set<String> questionCodes = validItems.stream()
                .map(item -> StringUtils.trim(item.getQuestionCode()))
                .collect(Collectors.toSet());
        Map<String, Question> questionMap = loadQuestionMapByCodes(questionCodes);

        List<QuestionPaperItem> entities = new ArrayList<>();
        for (QuestionPaperItemSaveBo itemBo : validItems) {
            String code = StringUtils.trim(itemBo.getQuestionCode());
            Question question = questionMap.get(code);
            if (question == null) {
                throw new BizException("题目不存在：" + code);
            }
            QuestionPaperItem item = new QuestionPaperItem();
            item.setPaperId(paperId);
            item.setQuestionId(question.getId());
            item.setSortOrder(itemBo.getSortOrder());
            entities.add(item);
        }
        iQuestionPaperItemService.saveBatch(entities);
    }

    private List<QuestionPaperItemVo> loadPaperItemVos(Long paperId) {
        LambdaQueryWrapper<QuestionPaperItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(QuestionPaperItem::getPaperId, paperId)
                .orderByAsc(QuestionPaperItem::getSortOrder)
                .orderByAsc(QuestionPaperItem::getId);
        List<QuestionPaperItem> items = iQuestionPaperItemService.list(wrapper);
        if (CollectionUtils.isEmpty(items)) {
            return Collections.emptyList();
        }

        List<Long> questionIds = items.stream()
                .map(QuestionPaperItem::getQuestionId)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, Question> questionMap = iQuestionService.listByIds(questionIds).stream()
                .collect(Collectors.toMap(Question::getId, Function.identity(), (a, b) -> a));

        List<QuestionPaperItemVo> voList = new ArrayList<>();
        for (QuestionPaperItem item : items) {
            Question question = questionMap.get(item.getQuestionId());
            QuestionPaperItemVo vo = new QuestionPaperItemVo();
            vo.setId(item.getId());
            vo.setSortOrder(item.getSortOrder());
            if (question != null) {
                vo.setQuestionId(question.getId());
                vo.setQuestionCode(question.getQuestionCode());
                vo.setQuestionTitle(question.getTitle());
                vo.setQuestionType(question.getQuestionType());
            }
            voList.add(vo);
        }
        return voList;
    }

    private Map<String, Question> loadQuestionMapByCodes(Set<String> questionCodes) {
        if (CollectionUtils.isEmpty(questionCodes)) {
            return Collections.emptyMap();
        }
        LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Question::getQuestionCode, questionCodes);
        return iQuestionService.list(wrapper).stream()
                .collect(Collectors.toMap(Question::getQuestionCode, Function.identity(), (a, b) -> a));
    }

    private Map<Long, Question> loadQuestionMapByIds(Set<Long> questionIds) {
        if (CollectionUtils.isEmpty(questionIds)) {
            return Collections.emptyMap();
        }
        return iQuestionService.listByIds(questionIds).stream()
                .collect(Collectors.toMap(Question::getId, Function.identity(), (a, b) -> a));
    }

    private void removePaperItems(Long paperId) {
        LambdaQueryWrapper<QuestionPaperItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(QuestionPaperItem::getPaperId, paperId);
        iQuestionPaperItemService.remove(wrapper);
    }

    private QueryWrapper<QuestionPaper> buildPaperQuery(QuestionPaperPageBo pageBo) {
        QueryWrapper<QuestionPaper> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .like(StringUtils.isNotBlank(pageBo.getPaperCode()), QuestionPaper::getPaperCode, pageBo.getPaperCode())
                .like(StringUtils.isNotBlank(pageBo.getTitle()), QuestionPaper::getTitle, pageBo.getTitle())
                .eq(StringUtils.isNotBlank(pageBo.getPaperType()), QuestionPaper::getPaperType, pageBo.getPaperType())
                .eq(StringUtils.isNotBlank(pageBo.getGrade()), QuestionPaper::getGrade, pageBo.getGrade())
                .eq(StringUtils.isNotBlank(pageBo.getSubject()), QuestionPaper::getSubject, pageBo.getSubject())
                .eq(Objects.nonNull(pageBo.getState()), QuestionPaper::getState, pageBo.getState());
        return queryWrapper;
    }
}
