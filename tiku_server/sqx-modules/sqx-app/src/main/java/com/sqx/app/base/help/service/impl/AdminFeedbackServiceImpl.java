package com.sqx.app.base.help.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sqx.app.base.enums.AppError;
import com.sqx.app.base.help.constants.FeedbackConstant;
import com.sqx.app.base.help.db.service.IUserFeedbackService;
import com.sqx.app.base.help.domain.bo.FeedbackInfoBo;
import com.sqx.app.base.help.domain.bo.FeedbackPageBo;
import com.sqx.app.base.help.domain.bo.FeedbackUpdateBo;
import com.sqx.app.base.help.domain.entity.UserFeedback;
import com.sqx.app.base.help.domain.vo.FeedbackVo;
import com.sqx.app.base.help.service.AdminFeedbackService;
import com.sqx.app.base.user.db.service.IUserService;
import com.sqx.app.base.user.domain.entity.UserEntity;
import com.sqx.app.base.user.support.AdminUserQuerySupport;
import com.sqx.common.core.exception.BizException;
import com.sqx.common.mybatis.utils.PageUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class AdminFeedbackServiceImpl implements AdminFeedbackService {

    private static final Set<String> ALLOWED_STATUS = new HashSet<String>() {{
        add(FeedbackConstant.STATUS_PENDING);
        add(FeedbackConstant.STATUS_PROCESSING);
        add(FeedbackConstant.STATUS_REPLIED);
    }};

    @Resource
    private IUserFeedbackService iUserFeedbackService;

    @Resource
    private IUserService iUserService;

    @Override
    public IPage<FeedbackVo> pageInfo(FeedbackPageBo pageBo) {
        Page<UserFeedback> initPage = PageUtil.initPage(pageBo.getPage(), pageBo.getLimit());

        List<Long> filterUserIds = AdminUserQuerySupport.resolveFilterUserIds(
                iUserService, pageBo.getUserId(), pageBo.getUserName(), pageBo.getPhone());
        if (filterUserIds != null && filterUserIds.isEmpty()) {
            return emptyPage(initPage);
        }

        QueryWrapper<UserFeedback> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .like(StringUtils.isNotBlank(pageBo.getTicketNo()), UserFeedback::getTicketNo, pageBo.getTicketNo())
                .eq(StringUtils.isNotBlank(pageBo.getCategory()), UserFeedback::getCategory, pageBo.getCategory())
                .eq(StringUtils.isNotBlank(pageBo.getStatus()), UserFeedback::getStatus, pageBo.getStatus())
                .eq(pageBo.getUserId() != null, UserFeedback::getUserId, pageBo.getUserId())
                .in(filterUserIds != null, UserFeedback::getUserId, filterUserIds)
                .like(StringUtils.isNotBlank(pageBo.getContactPhone()), UserFeedback::getContactPhone, pageBo.getContactPhone())
                .orderByDesc(UserFeedback::getCreateTime)
                .orderByDesc(UserFeedback::getId);

        IPage<UserFeedback> entityPage = iUserFeedbackService.page(initPage, queryWrapper);
        List<UserFeedback> records = entityPage.getRecords();
        if (CollectionUtils.isEmpty(records)) {
            return PageUtil.copyPage(entityPage, Collections.emptyList());
        }

        Map<Long, UserEntity> userMap = AdminUserQuerySupport.loadUserMap(
                iUserService,
                records.stream().map(UserFeedback::getUserId).filter(Objects::nonNull).collect(Collectors.toList()));

        List<FeedbackVo> voList = new ArrayList<>(records.size());
        for (UserFeedback record : records) {
            FeedbackVo vo = toVo(record);
            AdminUserQuerySupport.fillUserDisplay(userMap.get(record.getUserId()), vo);
            voList.add(vo);
        }
        return PageUtil.copyPage(entityPage, voList);
    }

    @Override
    public FeedbackVo info(FeedbackInfoBo infoBo) {
        UserFeedback entity = requireFeedback(infoBo.getId());
        FeedbackVo vo = toVo(entity);
        if (entity.getUserId() != null) {
            UserEntity user = iUserService.getById(entity.getUserId());
            AdminUserQuerySupport.fillUserDisplay(user, vo);
        }
        return vo;
    }

    @Override
    public void updateInfo(FeedbackUpdateBo updateBo) {
        UserFeedback existing = requireFeedback(updateBo.getId());

        String status = StringUtils.trimToNull(updateBo.getStatus());
        if (status != null && !ALLOWED_STATUS.contains(status)) {
            throw new BizException("状态仅支持 pending、processing、replied");
        }

        UserFeedback entity = new UserFeedback();
        entity.setId(existing.getId());
        if (status != null) {
            entity.setStatus(status);
        }
        if (updateBo.getReply() != null) {
            entity.setReply(StringUtils.trimToEmpty(updateBo.getReply()));
            if (FeedbackConstant.STATUS_REPLIED.equals(status)
                    || (status == null && StringUtils.isNotBlank(entity.getReply()))) {
                entity.setStatus(FeedbackConstant.STATUS_REPLIED);
            }
        }
        iUserFeedbackService.updateById(entity);
    }

    private UserFeedback requireFeedback(Long id) {
        UserFeedback entity = iUserFeedbackService.getById(id);
        if (entity == null) {
            throw new BizException(AppError.INFO_NOT_FOUND);
        }
        return entity;
    }

    private FeedbackVo toVo(UserFeedback entity) {
        FeedbackVo vo = new FeedbackVo();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    private IPage<FeedbackVo> emptyPage(Page<UserFeedback> initPage) {
        IPage<FeedbackVo> page = new Page<>();
        page.setCurrent(initPage.getCurrent());
        page.setSize(initPage.getSize());
        page.setTotal(0);
        page.setPages(0);
        page.setRecords(Collections.emptyList());
        return page;
    }
}
