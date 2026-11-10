package com.sqx.app.referral.withdraw.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sqx.app.base.user.db.service.IUserService;
import com.sqx.app.base.user.domain.entity.UserEntity;
import com.sqx.app.base.user.support.AdminUserQuerySupport;
import com.sqx.app.referral.withdraw.constants.WithdrawChannelConstants;
import com.sqx.app.referral.withdraw.db.service.IWithdrawalAccountService;
import com.sqx.app.referral.withdraw.domain.bo.AppWithdrawalAccountBindBO;
import com.sqx.app.referral.withdraw.domain.bo.WithdrawalAccountInfoBo;
import com.sqx.app.referral.withdraw.domain.bo.WithdrawalAccountPageBo;
import com.sqx.app.referral.withdraw.domain.bo.WithdrawalAccountSaveBo;
import com.sqx.app.referral.withdraw.domain.bo.WithdrawalAccountUpdateBo;
import com.sqx.app.referral.withdraw.domain.entity.WithdrawalAccount;
import com.sqx.app.referral.withdraw.domain.vo.AppWithdrawalAccountBindResultVO;
import com.sqx.app.referral.withdraw.domain.vo.WithdrawalAccountVo;
import com.sqx.app.referral.withdraw.service.WithdrawalAccountService;
import com.sqx.app.referral.withdraw.support.RealNameSupport;
import com.sqx.common.core.exception.BizException;
import com.sqx.common.mybatis.utils.PageUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 提现账号 服务实现类
 *
 * @author 西安省钱兄网络科技有限公司
 * @since 2025-06-20 14:02:39
 */
@Slf4j
@Service
public class WithdrawalAccountServiceImpl implements WithdrawalAccountService {

    @Resource
    private IWithdrawalAccountService iWithdrawalAccountService;

    @Resource
    private IUserService iUserService;

    @Override
    public void saveInfo(WithdrawalAccountSaveBo withdrawalAccountSaveBo) {
        int accountType = resolveAccountType(withdrawalAccountSaveBo.getType());
        validateBindingFields(accountType,
                withdrawalAccountSaveBo.getAccountName(),
                withdrawalAccountSaveBo.getAccountNumber());

        WithdrawalAccount existing = iWithdrawalAccountService.getOne(new LambdaQueryWrapper<WithdrawalAccount>()
                .eq(WithdrawalAccount::getUserId, withdrawalAccountSaveBo.getUserId())
                .eq(WithdrawalAccount::getType, accountType)
                .last("LIMIT 1"));

        WithdrawalAccount withdrawalAccount = existing != null ? existing : new WithdrawalAccount();
        if (existing == null) {
            withdrawalAccount.setUserId(withdrawalAccountSaveBo.getUserId());
            withdrawalAccount.setType(accountType);
        }

        withdrawalAccount.setAccountName(StringUtils.trimToEmpty(withdrawalAccountSaveBo.getAccountName()));
        withdrawalAccount.setAccountNumber(withdrawalAccountSaveBo.getAccountNumber().trim());
        if (accountType == WithdrawChannelConstants.ACCOUNT_TYPE_ALIPAY) {
            withdrawalAccount.setRealName(withdrawalAccountSaveBo.getAccountName().trim());
        }
        withdrawalAccount.setRealNameVerified(1);

        if (existing != null) {
            iWithdrawalAccountService.updateById(withdrawalAccount);
        } else {
            iWithdrawalAccountService.save(withdrawalAccount);
        }
    }

    private void validateBindingFields(int accountType, String accountName, String accountNumber) {
        if (StringUtils.isBlank(accountNumber)) {
            throw new BizException(accountType == WithdrawChannelConstants.ACCOUNT_TYPE_WECHAT
                    ? "请上传微信收款码" : "请输入支付宝账号");
        }
        if (accountType == WithdrawChannelConstants.ACCOUNT_TYPE_ALIPAY
                && StringUtils.isBlank(accountName)) {
            throw new BizException("请输入收款姓名");
        }
    }

    @Override
    public AppWithdrawalAccountBindResultVO bindRealNameAccount(Long userId, AppWithdrawalAccountBindBO bindBO) {
        RealNameSupport.validateIdCardNo(bindBO.getIdCardNo());
        int accountType = resolveAccountType(bindBO.getType());

        WithdrawalAccount existing = iWithdrawalAccountService.getOne(new LambdaQueryWrapper<WithdrawalAccount>()
                .eq(WithdrawalAccount::getUserId, userId)
                .eq(WithdrawalAccount::getType, accountType)
                .last("LIMIT 1"));

        WithdrawalAccount account;
        if (existing != null) {
            account = existing;
        } else {
            account = new WithdrawalAccount();
            account.setUserId(userId);
            account.setType(accountType);
        }

        account.setRealName(bindBO.getRealName().trim());
        account.setIdCardNo(bindBO.getIdCardNo().trim().toUpperCase());
        account.setAccountName(bindBO.getAccountName().trim());
        account.setAccountNumber(bindBO.getAccountNumber().trim());
        account.setRealNameVerified(1);

        if (existing != null) {
            iWithdrawalAccountService.updateById(account);
        } else {
            iWithdrawalAccountService.save(account);
        }

        return buildBindResult(account);
    }

    private int resolveAccountType(Integer type) {
        if (type == null) {
            return WithdrawChannelConstants.ACCOUNT_TYPE_WECHAT;
        }
        if (type != WithdrawChannelConstants.ACCOUNT_TYPE_WECHAT
                && type != WithdrawChannelConstants.ACCOUNT_TYPE_ALIPAY) {
            throw new BizException("不支持的提现账户类型");
        }
        return type;
    }

    private AppWithdrawalAccountBindResultVO buildBindResult(WithdrawalAccount account) {
        AppWithdrawalAccountBindResultVO vo = new AppWithdrawalAccountBindResultVO();
        vo.setId(account.getId());
        vo.setRealName(account.getRealName());
        vo.setIdCardNoMasked(RealNameSupport.maskIdCardNo(account.getIdCardNo()));
        vo.setAccountName(account.getAccountName());
        vo.setAccountNumber(account.getAccountNumber());
        vo.setType(account.getType());
        vo.setTypeName(resolveTypeName(account.getType()));
        vo.setRealNameVerified(account.getRealNameVerified());
        vo.setUpdateTime(account.getUpdateTime());
        return vo;
    }

    private String resolveTypeName(Integer type) {
        if (Objects.equals(type, WithdrawChannelConstants.ACCOUNT_TYPE_WECHAT)) {
            return "微信零钱";
        }
        if (Objects.equals(type, WithdrawChannelConstants.ACCOUNT_TYPE_ALIPAY)) {
            return "支付宝";
        }
        return "未知渠道";
    }

    private boolean checkExists(Long userId, Integer type) {
        return iWithdrawalAccountService.exists(new LambdaQueryWrapper<WithdrawalAccount>()
                .eq(WithdrawalAccount::getUserId, userId)
                .eq(WithdrawalAccount::getType, type)
        );
    }

    @Override
    public IPage<WithdrawalAccountVo> pageInfo(WithdrawalAccountPageBo withdrawalAccountPageBo) {
        Page<WithdrawalAccount> initPage = PageUtil.initPage(withdrawalAccountPageBo.getPage(), withdrawalAccountPageBo.getLimit());

        List<Long> filterUserIds = AdminUserQuerySupport.resolveFilterUserIds(
                iUserService,
                withdrawalAccountPageBo.getUserId(),
                withdrawalAccountPageBo.getUserName(),
                withdrawalAccountPageBo.getPhone()
        );
        if (filterUserIds != null && filterUserIds.isEmpty()) {
            return PageUtil.copyPage(initPage, Collections.emptyList());
        }

        QueryWrapper<WithdrawalAccount> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .like(StringUtils.isNotBlank(withdrawalAccountPageBo.getAccountName()), WithdrawalAccount::getAccountName, withdrawalAccountPageBo.getAccountName())
                .like(StringUtils.isNotBlank(withdrawalAccountPageBo.getAccountNumber()), WithdrawalAccount::getAccountNumber, withdrawalAccountPageBo.getAccountNumber())
                .eq(Objects.nonNull(withdrawalAccountPageBo.getType()), WithdrawalAccount::getType, withdrawalAccountPageBo.getType())
                .eq(Objects.nonNull(withdrawalAccountPageBo.getUserId()), WithdrawalAccount::getUserId, withdrawalAccountPageBo.getUserId())
                .in(filterUserIds != null, WithdrawalAccount::getUserId, filterUserIds)
                .orderByDesc(WithdrawalAccount::getCreateTime);

        boolean adminQuery = Objects.equals(withdrawalAccountPageBo.getSource(), 1);
        if (!adminQuery) {
            if (withdrawalAccountPageBo.getUserId() == null) {
                return PageUtil.copyPage(initPage, Collections.emptyList());
            }
            queryWrapper.lambda().eq(WithdrawalAccount::getUserId, withdrawalAccountPageBo.getUserId());
        }

        IPage<WithdrawalAccount> iPage = iWithdrawalAccountService.page(initPage, queryWrapper);
        List<WithdrawalAccountVo> voList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(iPage.getRecords())) {
            Map<Long, UserEntity> userMap = AdminUserQuerySupport.loadUserMap(
                    iUserService,
                    iPage.getRecords().stream().map(WithdrawalAccount::getUserId).collect(Collectors.toList())
            );
            for (WithdrawalAccount record : iPage.getRecords()) {
                WithdrawalAccountVo vo = new WithdrawalAccountVo();
                BeanUtils.copyProperties(record, vo);
                AdminUserQuerySupport.fillUserDisplay(userMap.get(record.getUserId()), vo);
                voList.add(vo);
            }
        }

        return PageUtil.copyPage(iPage, voList);
    }

    @Override
    public WithdrawalAccountVo info(WithdrawalAccountInfoBo withdrawalAccountInfoBo) {
        WithdrawalAccount withdrawalAccount = iWithdrawalAccountService.getById(withdrawalAccountInfoBo.getId());
        if (Objects.isNull(withdrawalAccount)) {
            throw new BizException("信息不存在");
        }

        WithdrawalAccountVo vo = new WithdrawalAccountVo();
        BeanUtils.copyProperties(withdrawalAccount, vo);
        AdminUserQuerySupport.fillUserDisplay(iUserService.getById(withdrawalAccount.getUserId()), vo);
        return vo;
    }

    @Override
    public void updateInfo(WithdrawalAccountUpdateBo withdrawalAccountUpdateBo) {
        WithdrawalAccount withdrawalAccount = new WithdrawalAccount();
        BeanUtils.copyProperties(withdrawalAccountUpdateBo, withdrawalAccount);

        iWithdrawalAccountService.updateById(withdrawalAccount);
    }

    @Override
    public void delete(WithdrawalAccountInfoBo withdrawalAccountInfoBo) {
        iWithdrawalAccountService.removeById(withdrawalAccountInfoBo.getId());
    }

    @Override
    public WithdrawalAccount getUserWithdrawalAccount(Long userId, Integer type) {
        return iWithdrawalAccountService.getOne(new LambdaQueryWrapper<WithdrawalAccount>()
                .eq(WithdrawalAccount::getUserId, userId)
                .eq(WithdrawalAccount::getType, type)
        );
    }
}
