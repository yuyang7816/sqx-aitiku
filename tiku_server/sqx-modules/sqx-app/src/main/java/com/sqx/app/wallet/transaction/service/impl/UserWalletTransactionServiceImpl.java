package com.sqx.app.wallet.transaction.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sqx.app.base.user.db.service.IUserService;
import com.sqx.app.base.user.domain.entity.UserEntity;
import com.sqx.app.base.user.support.AdminUserQuerySupport;
import com.sqx.app.wallet.transaction.constants.UserWalletTransactionConstant;
import com.sqx.app.wallet.transaction.db.mapper.UserWalletTransactionMapper;
import com.sqx.app.wallet.transaction.db.service.IUserWalletTransactionService;
import com.sqx.app.wallet.transaction.domain.bo.UserWalletTransactionInfoBo;
import com.sqx.app.wallet.transaction.domain.bo.UserWalletTransactionPageBo;
import com.sqx.app.wallet.transaction.domain.bo.UserWalletTransactionSaveBo;
import com.sqx.app.wallet.transaction.domain.bo.UserWalletTransactionUpdateBo;
import com.sqx.app.wallet.transaction.domain.entity.UserWalletTransaction;
import com.sqx.app.wallet.transaction.domain.vo.AppUserWalletTransactionDetailVO;
import com.sqx.app.wallet.transaction.domain.vo.AppUserWalletTransactionItemVO;
import com.sqx.app.wallet.transaction.domain.vo.UserWalletTransactionVo;
import com.sqx.app.wallet.transaction.service.UserWalletTransactionService;
import com.sqx.app.wallet.transaction.support.UserWalletTransactionDisplaySupport;
import com.sqx.common.core.exception.BizException;
import com.sqx.common.mybatis.utils.PageUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 用户钱包交易记录表 服务实现类
 *
 * @author 西安省钱兄网络科技有限公司
 * @since 2025-08-13 15:12:03
 */
@Service
public class UserWalletTransactionServiceImpl implements UserWalletTransactionService {

    @Resource
    private IUserWalletTransactionService iUserWalletTransactionService;
    @Resource
    private UserWalletTransactionMapper userWalletTransactionMapper;
    @Resource
    private IUserService iUserService;

    @Override
    public AppUserWalletTransactionDetailVO getDetail(Long userId, Long page, Long limit) {
        UserEntity user = iUserService.getById(userId);
        if (Objects.isNull(user)) {
            throw new BizException("用户不存在");
        }

        AppUserWalletTransactionDetailVO detailVO = new AppUserWalletTransactionDetailVO();
        detailVO.setTotalAmount(normalizeAmount(userWalletTransactionMapper.sumTotalIncome(userId)));
        detailVO.setPendingAmount(normalizeAmount(userWalletTransactionMapper.sumPendingIncome(userId)));
        detailVO.setRecords(pageIncomeRecords(userId, page, limit));
        return detailVO;
    }

    private IPage<AppUserWalletTransactionItemVO> pageIncomeRecords(Long userId, Long page, Long limit) {
        Page<UserWalletTransaction> initPage = PageUtil.initPage(page, limit);

        QueryWrapper<UserWalletTransaction> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(UserWalletTransaction::getUserId, userId)
                .eq(UserWalletTransaction::getDirection, UserWalletTransactionConstant.DIRECTION_INCOME)
                .in(UserWalletTransaction::getStatus, Arrays.asList(
                        UserWalletTransactionConstant.STATUS_PENDING,
                        UserWalletTransactionConstant.STATUS_COMPLETED
                ))
                .orderByDesc(UserWalletTransaction::getCreateTime);

        IPage<UserWalletTransaction> entityPage = iUserWalletTransactionService.page(initPage, queryWrapper);
        List<AppUserWalletTransactionItemVO> voList = entityPage.getRecords().stream()
                .map(this::toItemVO)
                .collect(Collectors.toList());
        return PageUtil.copyPage(entityPage, voList);
    }

    private AppUserWalletTransactionItemVO toItemVO(UserWalletTransaction transaction) {
        AppUserWalletTransactionItemVO vo = new AppUserWalletTransactionItemVO();
        vo.setId(transaction.getId());
        vo.setTitle(UserWalletTransactionDisplaySupport.resolveTitle(transaction));
        vo.setTime(UserWalletTransactionDisplaySupport.formatRecordTime(transaction.getCreateTime()));
        vo.setAmount(UserWalletTransactionDisplaySupport.formatAmount(transaction.getAmount()));
        vo.setStatus(UserWalletTransactionDisplaySupport.resolveStatusLabel(transaction.getStatus()));
        return vo;
    }

    private BigDecimal normalizeAmount(BigDecimal amount) {
        return amount == null ? BigDecimal.ZERO : amount;
    }

    @Override
    public void saveInfo(UserWalletTransactionSaveBo userWalletTransactionSaveBo) {
        UserWalletTransaction userWalletTransaction = new UserWalletTransaction();
        BeanUtils.copyProperties(userWalletTransactionSaveBo, userWalletTransaction);

        iUserWalletTransactionService.save(userWalletTransaction);
    }

    @Override
    public IPage<UserWalletTransactionVo> pageInfo(UserWalletTransactionPageBo userWalletTransactionPageBo) {
        Page<UserWalletTransaction> initPage = PageUtil.initPage(userWalletTransactionPageBo.getPage(), userWalletTransactionPageBo.getLimit());

        List<Long> filterUserIds = AdminUserQuerySupport.resolveFilterUserIds(
                iUserService,
                userWalletTransactionPageBo.getUserId(),
                userWalletTransactionPageBo.getUserName(),
                userWalletTransactionPageBo.getPhone()
        );
        if (filterUserIds != null && filterUserIds.isEmpty()) {
            return PageUtil.copyPage(initPage, Collections.emptyList());
        }

        QueryWrapper<UserWalletTransaction> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(Objects.nonNull(userWalletTransactionPageBo.getUserId()), UserWalletTransaction::getUserId, userWalletTransactionPageBo.getUserId())
                .in(filterUserIds != null, UserWalletTransaction::getUserId, filterUserIds)
                .like(StringUtils.isNotBlank(userWalletTransactionPageBo.getTitle()), UserWalletTransaction::getTitle, userWalletTransactionPageBo.getTitle())
                .eq(Objects.nonNull(userWalletTransactionPageBo.getTransactionType()), UserWalletTransaction::getTransactionType, userWalletTransactionPageBo.getTransactionType())
                .eq(Objects.nonNull(userWalletTransactionPageBo.getDirection()), UserWalletTransaction::getDirection, userWalletTransactionPageBo.getDirection())
                .eq(Objects.nonNull(userWalletTransactionPageBo.getStatus()), UserWalletTransaction::getStatus, userWalletTransactionPageBo.getStatus())
                .orderByDesc(UserWalletTransaction::getCreateTime);

        IPage<UserWalletTransaction> iPage = iUserWalletTransactionService.page(initPage, queryWrapper);
        List<UserWalletTransactionVo> voList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(iPage.getRecords())) {
            Map<Long, UserEntity> userMap = AdminUserQuerySupport.loadUserMap(
                    iUserService,
                    iPage.getRecords().stream().map(UserWalletTransaction::getUserId).collect(Collectors.toList())
            );
            for (UserWalletTransaction record : iPage.getRecords()) {
                UserWalletTransactionVo vo = new UserWalletTransactionVo();
                BeanUtils.copyProperties(record, vo);
                AdminUserQuerySupport.fillUserDisplay(userMap.get(record.getUserId()), vo);
                voList.add(vo);
            }
        }

        return PageUtil.copyPage(iPage, voList);
    }

    @Override
    public UserWalletTransactionVo info(UserWalletTransactionInfoBo userWalletTransactionInfoBo) {
        UserWalletTransaction userWalletTransaction = iUserWalletTransactionService.getById(userWalletTransactionInfoBo.getId());
        if (Objects.isNull(userWalletTransaction)) {
            throw new BizException("信息不存在");
        }

        UserWalletTransactionVo vo = new UserWalletTransactionVo();
        BeanUtils.copyProperties(userWalletTransaction, vo);
        AdminUserQuerySupport.fillUserDisplay(iUserService.getById(userWalletTransaction.getUserId()), vo);
        return vo;
    }

    @Override
    public void updateInfo(UserWalletTransactionUpdateBo userWalletTransactionUpdateBo) {
        UserWalletTransaction userWalletTransaction = new UserWalletTransaction();
        BeanUtils.copyProperties(userWalletTransactionUpdateBo, userWalletTransaction);

        iUserWalletTransactionService.updateById(userWalletTransaction);
    }

    @Override
    public void delete(UserWalletTransactionInfoBo userWalletTransactionInfoBo) {
        iUserWalletTransactionService.removeById(userWalletTransactionInfoBo.getId());
    }
}
