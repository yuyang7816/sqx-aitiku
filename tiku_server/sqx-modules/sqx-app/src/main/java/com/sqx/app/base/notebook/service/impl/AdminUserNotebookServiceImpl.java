package com.sqx.app.base.notebook.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sqx.app.base.notebook.db.mapper.AdminUserNotebookMapper;
import com.sqx.app.base.notebook.domain.bo.AdminUserNotebookPageBo;
import com.sqx.app.base.notebook.domain.vo.AdminUserNotebookItemVo;
import com.sqx.app.base.notebook.domain.vo.AdminUserNotebookSummaryVo;
import com.sqx.app.base.notebook.domain.vo.AppNotebookItemVO;
import com.sqx.app.base.notebook.service.AdminUserNotebookService;
import com.sqx.app.base.notebook.support.NotebookItemAssembler;
import com.sqx.app.base.user.domain.entity.UserEntity;
import com.sqx.app.base.user.db.service.IUserService;
import com.sqx.common.mybatis.utils.PageUtil;
import com.sqx.system.db.entity.Question;
import com.sqx.system.db.entity.UserQuestionWrong;
import com.sqx.system.db.service.IUserQuestionWrongService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 管理端查看用户错题，便于老师针对性辅导
 */
@Service
public class AdminUserNotebookServiceImpl implements AdminUserNotebookService {

    @Resource
    private IUserQuestionWrongService iUserQuestionWrongService;

    @Resource
    private IUserService iUserService;

    @Resource
    private NotebookItemAssembler notebookItemAssembler;

    @Resource
    private AdminUserNotebookMapper adminUserNotebookMapper;

    @Override
    public IPage<AdminUserNotebookSummaryVo> pageUserSummary(AdminUserNotebookPageBo pageBo) {
        preparePageBo(pageBo);
        Page<AdminUserNotebookSummaryVo> page = PageUtil.initPage(pageBo.getPage(), pageBo.getLimit());
        return adminUserNotebookMapper.pageUserSummary(page, pageBo);
    }

    @Override
    public IPage<AdminUserNotebookItemVo> pageNotebook(AdminUserNotebookPageBo pageBo) {
        preparePageBo(pageBo);
        Page<UserQuestionWrong> initPage = PageUtil.initPage(pageBo.getPage(), pageBo.getLimit());
        List<Long> userIds = resolveUserIds(pageBo);
        if (userIds != null && userIds.isEmpty()) {
            return PageUtil.copyPage(initPage, Collections.emptyList());
        }

        LambdaQueryWrapper<UserQuestionWrong> wrapper = new LambdaQueryWrapper<>();
        if (pageBo.getUserId() != null) {
            wrapper.eq(UserQuestionWrong::getUserId, pageBo.getUserId());
        } else if (userIds != null) {
            wrapper.in(UserQuestionWrong::getUserId, userIds);
        }
        if (StringUtils.isNotBlank(pageBo.getSubject())) {
            wrapper.eq(UserQuestionWrong::getSubject, StringUtils.trim(pageBo.getSubject()));
        }
        if (pageBo.getMastered() != null) {
            int masteredFlag = Boolean.TRUE.equals(pageBo.getMastered())
                    ? UserQuestionWrong.MASTERED_YES
                    : UserQuestionWrong.MASTERED_NO;
            wrapper.eq(UserQuestionWrong::getMastered, masteredFlag);
        }
        wrapper.orderByDesc(UserQuestionWrong::getLastPracticeAt)
                .orderByDesc(UserQuestionWrong::getUpdateTime);

        IPage<UserQuestionWrong> wrongPage = iUserQuestionWrongService.page(initPage, wrapper);
        List<UserQuestionWrong> wrongList = wrongPage.getRecords();
        if (CollectionUtils.isEmpty(wrongList)) {
            return PageUtil.copyPage(wrongPage, Collections.emptyList());
        }

        Map<Long, UserEntity> userMap = loadUserMap(wrongList);
        Map<Long, Question> questionMap = notebookItemAssembler.loadQuestionMap(wrongList, false);
        Map<Long, String> chapterSubjectMap = notebookItemAssembler.loadChapterSubjectMap(questionMap.values());

        List<AdminUserNotebookItemVo> voList = new ArrayList<>(wrongList.size());
        for (UserQuestionWrong wrong : wrongList) {
            Question question = questionMap.get(wrong.getQuestionId());
            AppNotebookItemVO base = notebookItemAssembler.toItemVO(wrong, question, chapterSubjectMap);
            AdminUserNotebookItemVo vo = new AdminUserNotebookItemVo();
            copyNotebookFields(base, vo);
            UserEntity user = userMap.get(wrong.getUserId());
            if (user != null) {
                vo.setUserId(user.getUserId());
                vo.setUserName(user.getUserName());
                vo.setNickname(user.getNickname());
                vo.setPhone(user.getPhone());
                vo.setGradeShort(user.getGradeShort());
            } else {
                vo.setUserId(wrong.getUserId());
            }
            voList.add(vo);
        }
        return PageUtil.copyPage(wrongPage, voList);
    }

    private void preparePageBo(AdminUserNotebookPageBo pageBo) {
        if (pageBo.getMastered() != null) {
            pageBo.setMasteredFlag(Boolean.TRUE.equals(pageBo.getMastered())
                    ? UserQuestionWrong.MASTERED_YES
                    : UserQuestionWrong.MASTERED_NO);
        }
    }

    private List<Long> resolveUserIds(AdminUserNotebookPageBo pageBo) {
        if (pageBo.getUserId() != null) {
            return null;
        }
        String phone = StringUtils.trimToEmpty(pageBo.getPhone());
        String userName = StringUtils.trimToEmpty(pageBo.getUserName());
        if (StringUtils.isBlank(phone) && StringUtils.isBlank(userName)) {
            return null;
        }
        LambdaQueryWrapper<UserEntity> userWrapper = new LambdaQueryWrapper<>();
        userWrapper.like(StringUtils.isNotBlank(userName), UserEntity::getUserName, userName)
                .like(StringUtils.isNotBlank(phone), UserEntity::getPhone, phone);
        List<UserEntity> users = iUserService.list(userWrapper);
        if (CollectionUtils.isEmpty(users)) {
            return Collections.emptyList();
        }
        return users.stream().map(UserEntity::getUserId).filter(Objects::nonNull).collect(Collectors.toList());
    }

    private Map<Long, UserEntity> loadUserMap(List<UserQuestionWrong> wrongList) {
        List<Long> userIds = wrongList.stream()
                .map(UserQuestionWrong::getUserId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return iUserService.listByIds(userIds).stream()
                .collect(Collectors.toMap(UserEntity::getUserId, Function.identity(), (a, b) -> a, LinkedHashMap::new));
    }

    private void copyNotebookFields(AppNotebookItemVO source, AdminUserNotebookItemVo target) {
        target.setId(source.getId());
        target.setNotebookId(source.getNotebookId());
        target.setQuestionId(source.getQuestionId());
        target.setQuestionCode(source.getQuestionCode());
        target.setSubject(source.getSubject());
        target.setDate(source.getDate());
        target.setTitle(source.getTitle());
        target.setQuestion(source.getQuestion());
        target.setMyAnswer(source.getMyAnswer());
        target.setWrong(source.getWrong());
        target.setRightAnswer(source.getRightAnswer());
        target.setRight(source.getRight());
        target.setAnalysis(source.getAnalysis());
        target.setAiSummary(source.getAiSummary());
        target.setOptions(source.getOptions());
        target.setTags(source.getTags());
        target.setWrongCount(source.getWrongCount());
        target.setMastered(source.getMastered());
        target.setLastPracticeAt(source.getLastPracticeAt());
    }
}
