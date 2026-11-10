package com.sqx.app.base.user.badge.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sqx.app.base.enums.AppError;
import com.sqx.app.base.user.badge.constants.UserBadgeRuleType;
import com.sqx.app.base.user.badge.db.service.IUserBadgeDefinitionService;
import com.sqx.app.base.user.badge.domain.bo.UserBadgeDefinitionInfoBo;
import com.sqx.app.base.user.badge.domain.bo.UserBadgeDefinitionPageBo;
import com.sqx.app.base.user.badge.domain.bo.UserBadgeDefinitionSaveBo;
import com.sqx.app.base.user.badge.domain.bo.UserBadgeDefinitionUpdateBo;
import com.sqx.app.base.user.badge.domain.entity.UserBadgeDefinition;
import com.sqx.app.base.user.badge.domain.vo.UserBadgeDefinitionVo;
import com.sqx.app.base.user.badge.service.UserBadgeDefinitionService;
import com.sqx.common.core.exception.BizException;
import com.sqx.common.mybatis.utils.PageUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
public class UserBadgeDefinitionServiceImpl implements UserBadgeDefinitionService {

    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final Set<String> ALLOWED_RULE_TYPES = new HashSet<>(Arrays.asList(
            UserBadgeRuleType.SUBMIT_COUNT,
            UserBadgeRuleType.STUDY_DAYS,
            UserBadgeRuleType.QUESTION_COUNT,
            UserBadgeRuleType.PERFECT_SCORE
    ));

    @Resource
    private IUserBadgeDefinitionService iUserBadgeDefinitionService;

    @Override
    public void saveInfo(UserBadgeDefinitionSaveBo saveBo) {
        String badgeCode = StringUtils.trimToEmpty(saveBo.getBadgeCode());
        validateRuleType(saveBo.getRuleType());
        assertBadgeCodeUnique(badgeCode, null);

        UserBadgeDefinition entity = new UserBadgeDefinition();
        entity.setBadgeCode(badgeCode);
        entity.setName(StringUtils.trimToEmpty(saveBo.getName()));
        entity.setConditionText(StringUtils.trimToEmpty(saveBo.getConditionText()));
        entity.setRuleType(StringUtils.trim(saveBo.getRuleType()));
        entity.setTargetValue(Math.max(1, saveBo.getTargetValue()));
        entity.setSortOrder(saveBo.getSortOrder() != null ? saveBo.getSortOrder() : 0);
        entity.setState(saveBo.getState() != null ? saveBo.getState() : UserBadgeDefinition.STATE_ENABLED);
        iUserBadgeDefinitionService.save(entity);
    }

    @Override
    public IPage<UserBadgeDefinitionVo> pageInfo(UserBadgeDefinitionPageBo pageBo) {
        Page<UserBadgeDefinition> initPage = PageUtil.initPage(pageBo.getPage(), pageBo.getLimit());
        QueryWrapper<UserBadgeDefinition> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .like(StringUtils.isNotBlank(pageBo.getName()), UserBadgeDefinition::getName, pageBo.getName())
                .eq(Objects.nonNull(pageBo.getState()), UserBadgeDefinition::getState, pageBo.getState())
                .orderByAsc(UserBadgeDefinition::getSortOrder)
                .orderByDesc(UserBadgeDefinition::getCreateTime);

        IPage<UserBadgeDefinition> iPage = iUserBadgeDefinitionService.page(initPage, queryWrapper);
        List<UserBadgeDefinitionVo> voList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(iPage.getRecords())) {
            for (UserBadgeDefinition record : iPage.getRecords()) {
                voList.add(toVo(record));
            }
        }
        return PageUtil.copyPage(iPage, voList);
    }

    @Override
    public UserBadgeDefinitionVo info(UserBadgeDefinitionInfoBo infoBo) {
        UserBadgeDefinition entity = requireEntity(infoBo.getId());
        return toVo(entity);
    }

    @Override
    public void updateInfo(UserBadgeDefinitionUpdateBo updateBo) {
        UserBadgeDefinition existing = requireEntity(updateBo.getId());
        UserBadgeDefinition entity = new UserBadgeDefinition();
        entity.setId(updateBo.getId());
        if (StringUtils.isNotBlank(updateBo.getName())) {
            entity.setName(updateBo.getName().trim());
        }
        if (StringUtils.isNotBlank(updateBo.getConditionText())) {
            entity.setConditionText(updateBo.getConditionText().trim());
        }
        if (StringUtils.isNotBlank(updateBo.getRuleType())) {
            validateRuleType(updateBo.getRuleType());
            entity.setRuleType(updateBo.getRuleType().trim());
        }
        if (updateBo.getTargetValue() != null) {
            entity.setTargetValue(Math.max(1, updateBo.getTargetValue()));
        }
        if (updateBo.getSortOrder() != null) {
            entity.setSortOrder(updateBo.getSortOrder());
        }
        if (updateBo.getState() != null) {
            entity.setState(updateBo.getState());
        }
        iUserBadgeDefinitionService.updateById(entity);
        if (StringUtils.isBlank(existing.getBadgeCode())) {
            return;
        }
    }

    @Override
    public void delete(UserBadgeDefinitionInfoBo infoBo) {
        requireEntity(infoBo.getId());
        iUserBadgeDefinitionService.removeById(infoBo.getId());
    }

    private UserBadgeDefinition requireEntity(Long id) {
        UserBadgeDefinition entity = iUserBadgeDefinitionService.getById(id);
        if (entity == null) {
            throw new BizException(AppError.INFO_NOT_FOUND);
        }
        return entity;
    }

    private void assertBadgeCodeUnique(String badgeCode, Long excludeId) {
        QueryWrapper<UserBadgeDefinition> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(UserBadgeDefinition::getBadgeCode, badgeCode);
        if (excludeId != null) {
            queryWrapper.lambda().ne(UserBadgeDefinition::getId, excludeId);
        }
        if (iUserBadgeDefinitionService.count(queryWrapper) > 0) {
            throw new BizException("勋章编码已存在");
        }
    }

    private void validateRuleType(String ruleType) {
        if (StringUtils.isBlank(ruleType) || !ALLOWED_RULE_TYPES.contains(ruleType.trim())) {
            throw new BizException("规则类型不正确");
        }
    }

    private UserBadgeDefinitionVo toVo(UserBadgeDefinition record) {
        UserBadgeDefinitionVo vo = new UserBadgeDefinitionVo();
        vo.setId(record.getId());
        vo.setBadgeCode(record.getBadgeCode());
        vo.setName(record.getName());
        vo.setConditionText(record.getConditionText());
        vo.setRuleType(record.getRuleType());
        vo.setTargetValue(record.getTargetValue());
        vo.setSortOrder(record.getSortOrder());
        vo.setState(record.getState());
        if (record.getCreateTime() != null) {
            vo.setCreateTime(record.getCreateTime().format(DATETIME_FORMATTER));
        }
        if (record.getUpdateTime() != null) {
            vo.setUpdateTime(record.getUpdateTime().format(DATETIME_FORMATTER));
        }
        return vo;
    }
}
