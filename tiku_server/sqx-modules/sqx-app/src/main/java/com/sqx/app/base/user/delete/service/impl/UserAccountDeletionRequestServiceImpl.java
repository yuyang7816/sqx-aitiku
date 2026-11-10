package com.sqx.app.base.user.delete.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sqx.app.base.enums.AppError;
import com.sqx.app.base.user.db.service.IUserService;
import com.sqx.app.base.user.delete.constants.AccountDeletionConstant;
import com.sqx.app.base.user.delete.db.service.IUserAccountDeletionRequestService;
import com.sqx.app.base.user.delete.domain.bo.AdminUserDeleteRequestActionBo;
import com.sqx.app.base.user.delete.domain.bo.AdminUserDeleteRequestPageBo;
import com.sqx.app.base.user.delete.domain.entity.UserAccountDeletionRequest;
import com.sqx.app.base.user.delete.domain.enums.DeletionRequestStatus;
import com.sqx.app.base.user.delete.domain.vo.AdminUserDeleteRequestVo;
import com.sqx.app.base.user.delete.domain.vo.AppUserDeleteAccountVO;
import com.sqx.app.base.user.delete.service.UserAccountDeletionRequestService;
import com.sqx.app.base.user.delete.service.UserAccountDeletionService;
import com.sqx.app.base.user.delete.support.UserDeletedSupport;
import com.sqx.app.base.user.domain.entity.UserEntity;
import com.sqx.app.base.user.support.AdminUserQuerySupport;
import com.sqx.common.core.exception.BizException;
import com.sqx.common.mybatis.utils.PageUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class UserAccountDeletionRequestServiceImpl implements UserAccountDeletionRequestService {

    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Resource
    private IUserService iUserService;
    @Resource
    private IUserAccountDeletionRequestService iUserAccountDeletionRequestService;
    @Resource
    private UserAccountDeletionService userAccountDeletionService;

    @Override
    public AppUserDeleteAccountVO getDeleteStatus(Long userId) {
        ensureActiveUser(userId);
        UserAccountDeletionRequest pending = findPendingRequest(userId);
        if (pending == null) {
            return buildIdleStatus();
        }
        return toAppVo(pending);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public AppUserDeleteAccountVO requestDeletion(Long userId) {
        ensureActiveUser(userId);
        UserAccountDeletionRequest existing = findPendingRequest(userId);
        if (existing != null) {
            return toAppVo(existing);
        }

        LocalDateTime now = LocalDateTime.now();
        UserAccountDeletionRequest request = new UserAccountDeletionRequest();
        request.setUserId(userId);
        request.setStatus(DeletionRequestStatus.PENDING.getCode());
        request.setRequestTime(now);
        request.setEffectiveTime(now.plusDays(AccountDeletionConstant.COOLING_DAYS));
        iUserAccountDeletionRequestService.save(request);
        return toAppVo(request);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public AppUserDeleteAccountVO cancelDeletion(Long userId) {
        ensureActiveUser(userId);
        UserAccountDeletionRequest pending = findPendingRequest(userId);
        if (pending == null) {
            throw new BizException(AppError.USER_DELETE_REQUEST_NOT_FOUND);
        }

        LocalDateTime now = LocalDateTime.now();
        pending.setStatus(DeletionRequestStatus.CANCELLED.getCode());
        pending.setCancelTime(now);
        iUserAccountDeletionRequestService.updateById(pending);
        return buildIdleStatus();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public int executeDueDeletions() {
        LocalDateTime now = LocalDateTime.now();
        List<UserAccountDeletionRequest> dueList = iUserAccountDeletionRequestService.list(
                new LambdaQueryWrapper<UserAccountDeletionRequest>()
                        .eq(UserAccountDeletionRequest::getStatus, DeletionRequestStatus.PENDING.getCode())
                        .le(UserAccountDeletionRequest::getEffectiveTime, now)
                        .orderByAsc(UserAccountDeletionRequest::getEffectiveTime)
        );
        if (CollectionUtils.isEmpty(dueList)) {
            return 0;
        }

        int executed = 0;
        for (UserAccountDeletionRequest request : dueList) {
            if (completeRequest(request.getId(), "冷静期结束自动执行")) {
                executed += 1;
            }
        }
        return executed;
    }

    @Override
    public IPage<AdminUserDeleteRequestVo> adminPage(AdminUserDeleteRequestPageBo pageBo) {
        Page<UserAccountDeletionRequest> initPage = PageUtil.initPage(pageBo.getPage(), pageBo.getLimit());
        List<Long> filterUserIds = AdminUserQuerySupport.resolveFilterUserIds(
                iUserService, pageBo.getUserId(), pageBo.getUserName(), pageBo.getPhone());
        if (filterUserIds != null && filterUserIds.isEmpty()) {
            return initPage.convert(item -> null);
        }

        QueryWrapper<UserAccountDeletionRequest> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(pageBo.getUserId() != null, UserAccountDeletionRequest::getUserId, pageBo.getUserId())
                .in(filterUserIds != null, UserAccountDeletionRequest::getUserId, filterUserIds)
                .eq(StringUtils.isNotBlank(pageBo.getStatus()), UserAccountDeletionRequest::getStatus, pageBo.getStatus())
                .orderByDesc(UserAccountDeletionRequest::getRequestTime);

        IPage<UserAccountDeletionRequest> page = iUserAccountDeletionRequestService.page(initPage, queryWrapper);
        List<UserAccountDeletionRequest> records = page.getRecords();
        if (CollectionUtils.isEmpty(records)) {
            return page.convert(item -> null);
        }

        List<Long> userIds = new ArrayList<>();
        for (UserAccountDeletionRequest record : records) {
            if (record.getUserId() != null) {
                userIds.add(record.getUserId());
            }
        }
        Map<Long, UserEntity> userMap = AdminUserQuerySupport.loadUserMap(iUserService, userIds);

        return page.convert(record -> toAdminVo(record, userMap.get(record.getUserId())));
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void adminCancel(AdminUserDeleteRequestActionBo actionBo) {
        UserAccountDeletionRequest request = requirePendingRequest(actionBo.getId());
        request.setStatus(DeletionRequestStatus.ADMIN_CANCELLED.getCode());
        request.setCancelTime(LocalDateTime.now());
        request.setAdminRemark(StringUtils.trimToNull(actionBo.getAdminRemark()));
        iUserAccountDeletionRequestService.updateById(request);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void adminExecuteNow(AdminUserDeleteRequestActionBo actionBo) {
        completeRequest(actionBo.getId(), StringUtils.defaultIfBlank(actionBo.getAdminRemark(), "管理员立即执行注销"));
    }

    private boolean completeRequest(Long requestId, String adminRemark) {
        UserAccountDeletionRequest request = iUserAccountDeletionRequestService.getById(requestId);
        if (request == null) {
            return false;
        }
        if (!DeletionRequestStatus.PENDING.getCode().equals(request.getStatus())) {
            return false;
        }

        userAccountDeletionService.deleteAccount(request.getUserId());

        request.setStatus(DeletionRequestStatus.COMPLETED.getCode());
        request.setCompleteTime(LocalDateTime.now());
        request.setAdminRemark(StringUtils.trimToNull(adminRemark));
        iUserAccountDeletionRequestService.updateById(request);
        return true;
    }

    private UserAccountDeletionRequest requirePendingRequest(Long requestId) {
        UserAccountDeletionRequest request = iUserAccountDeletionRequestService.getById(requestId);
        if (request == null) {
            throw new BizException(AppError.USER_DELETE_REQUEST_NOT_FOUND);
        }
        if (!DeletionRequestStatus.PENDING.getCode().equals(request.getStatus())) {
            throw new BizException(AppError.USER_DELETE_REQUEST_NOT_PENDING);
        }
        return request;
    }

    private UserAccountDeletionRequest findPendingRequest(Long userId) {
        return iUserAccountDeletionRequestService.getOne(
                new LambdaQueryWrapper<UserAccountDeletionRequest>()
                        .eq(UserAccountDeletionRequest::getUserId, userId)
                        .eq(UserAccountDeletionRequest::getStatus, DeletionRequestStatus.PENDING.getCode())
                        .orderByDesc(UserAccountDeletionRequest::getRequestTime)
                        .last("LIMIT 1"),
                false
        );
    }

    private void ensureActiveUser(Long userId) {
        UserEntity userEntity = iUserService.getById(userId);
        if (userEntity == null) {
            throw new BizException(AppError.USER_NOT_FOUND);
        }
        if (UserDeletedSupport.isDeleted(userEntity)) {
            throw new BizException(AppError.USER_ACCOUNT_DELETED);
        }
    }

    private AppUserDeleteAccountVO buildIdleStatus() {
        AppUserDeleteAccountVO vo = new AppUserDeleteAccountVO();
        vo.setDeleted(false);
        vo.setPending(false);
        vo.setCoolingDays(AccountDeletionConstant.COOLING_DAYS);
        vo.setRemainingDays(0);
        return vo;
    }

    private AppUserDeleteAccountVO toAppVo(UserAccountDeletionRequest request) {
        AppUserDeleteAccountVO vo = new AppUserDeleteAccountVO();
        vo.setDeleted(false);
        vo.setPending(true);
        vo.setCoolingDays(AccountDeletionConstant.COOLING_DAYS);
        vo.setEffectiveAt(formatDateTime(request.getEffectiveTime()));
        vo.setRemainingDays(calcRemainingDays(request.getEffectiveTime()));
        return vo;
    }

    private AdminUserDeleteRequestVo toAdminVo(UserAccountDeletionRequest request, UserEntity user) {
        AdminUserDeleteRequestVo vo = new AdminUserDeleteRequestVo();
        vo.setId(request.getId());
        vo.setUserId(request.getUserId());
        vo.setStatus(request.getStatus());
        DeletionRequestStatus status = DeletionRequestStatus.fromCode(request.getStatus());
        vo.setStatusLabel(status != null ? status.getLabel() : request.getStatus());
        vo.setRequestTime(request.getRequestTime());
        vo.setEffectiveTime(request.getEffectiveTime());
        vo.setCancelTime(request.getCancelTime());
        vo.setCompleteTime(request.getCompleteTime());
        vo.setAdminRemark(request.getAdminRemark());
        if (DeletionRequestStatus.PENDING.getCode().equals(request.getStatus())) {
            vo.setRemainingDays(calcRemainingDays(request.getEffectiveTime()));
        }
        if (user != null) {
            vo.setUserName(user.getUserName());
            vo.setNickname(user.getNickname());
            vo.setPhone(user.getPhone());
        }
        return vo;
    }

    private int calcRemainingDays(LocalDateTime effectiveTime) {
        if (effectiveTime == null) {
            return 0;
        }
        long days = ChronoUnit.DAYS.between(LocalDateTime.now().toLocalDate(), effectiveTime.toLocalDate());
        return (int) Math.max(days, 0);
    }

    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime == null ? null : dateTime.format(DATETIME_FORMATTER);
    }
}
