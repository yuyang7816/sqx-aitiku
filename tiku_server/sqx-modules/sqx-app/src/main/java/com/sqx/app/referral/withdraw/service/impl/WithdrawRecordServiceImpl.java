package com.sqx.app.referral.withdraw.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sqx.app.base.user.db.service.IUserService;
import com.sqx.app.base.user.domain.entity.UserEntity;
import com.sqx.app.base.user.support.AdminUserQuerySupport;
import com.sqx.app.referral.constants.ReferralConstant;
import com.sqx.app.referral.withdraw.constants.WithdrawChannelConstants;
import com.sqx.app.referral.withdraw.db.service.IWithdrawRecordService;
import com.sqx.app.referral.withdraw.db.service.IWithdrawalAccountService;
import com.sqx.app.referral.withdraw.domain.bo.*;
import com.sqx.app.referral.withdraw.domain.entity.WithdrawRecord;
import com.sqx.app.referral.withdraw.domain.entity.WithdrawalAccount;
import com.sqx.app.referral.withdraw.domain.vo.AppWithdrawApplyResultVO;
import com.sqx.app.referral.withdraw.domain.vo.AppWithdrawRulesVO;
import com.sqx.app.referral.withdraw.domain.vo.WithdrawRecordVo;
import com.sqx.app.referral.withdraw.service.WithdrawRecordService;
import com.sqx.app.referral.withdraw.support.RealNameSupport;
import com.sqx.app.referral.withdraw.support.WithdrawAmountValidator;
import com.sqx.app.wallet.transaction.db.service.IUserWalletTransactionService;
import com.sqx.app.wallet.transaction.domain.entity.UserWalletTransaction;
import com.sqx.app.wallet.wallet.db.service.IUserWalletService;
import com.sqx.app.wallet.wallet.domain.entity.UserWallet;
import com.sqx.common.core.exception.BizException;
import com.sqx.common.mybatis.utils.PageUtil;
import com.sqx.system.db.service.ISysConfigService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 提现记录 服务实现类
 */
@Service
public class WithdrawRecordServiceImpl implements WithdrawRecordService {

    @Resource
    private IWithdrawRecordService iWithdrawRecordService;
    @Resource
    private ISysConfigService iSysConfigService;
    @Resource
    private IUserService iUserService;
    @Resource
    private IWithdrawalAccountService iWithdrawalAccountService;
    @Resource
    private IUserWalletService iUserWalletService;
    @Resource
    private IUserWalletTransactionService iUserWalletTransactionService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveInfo(WithdrawRecordSaveBo withdrawRecordSaveBo) {
        AppWithdrawApplyBO applyBO = new AppWithdrawApplyBO();
        applyBO.setAmount(withdrawRecordSaveBo.getAmount());
        applyBO.setAccountType(withdrawRecordSaveBo.getAccountType());
        applyWithdraw(withdrawRecordSaveBo.getUserId(), applyBO);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public AppWithdrawApplyResultVO applyWithdraw(Long userId, AppWithdrawApplyBO applyBO) {
        UserEntity userEntity = iUserService.getById(userId);
        if (Objects.isNull(userEntity)) {
            throw new BizException("用户不存在");
        }

        int accountType = resolveAccountType(applyBO.getAccountType());
        ensureWithdrawalAccountExists(userId, accountType);

        BigDecimal amount = applyBO.getAmount();
        BigDecimal walletBalance = loadWalletBalance(userId);
        String minAmount = iSysConfigService.getConfigValue(ReferralConstant.withdraw.SYS_WITHDRAW_MIN);
        String maxAmount = iSysConfigService.getConfigValue(ReferralConstant.withdraw.SYS_WITHDRAW_MAX);
        WithdrawAmountValidator.validate(amount, walletBalance, minAmount, maxAmount);

        String serviceFeeRate = iSysConfigService.getConfigValue(ReferralConstant.withdraw.SYS_WITHDRAW_RATE);
        BigDecimal rate = parseRate(serviceFeeRate);
        BigDecimal serviceFee = amount.multiply(rate);
        BigDecimal actualAmount = amount.subtract(serviceFee);

        WithdrawRecord withdrawRecord = new WithdrawRecord();
        withdrawRecord.setUserId(userId);
        withdrawRecord.setAmount(amount);
        withdrawRecord.setActualAmount(actualAmount);
        withdrawRecord.setServiceFee(serviceFee);
        withdrawRecord.setAccountType(accountType);
        withdrawRecord.setStatus(WithdrawChannelConstants.STATUS_PENDING);
        iWithdrawRecordService.save(withdrawRecord);

        iUserWalletService.decr(userId, amount);
        recordWalletTransaction(userId, amount, withdrawRecord.getId(), 2, 1, "提现申请", "提现申请待审核");

        return buildApplyResult(withdrawRecord);
    }

    @Override
    public AppWithdrawRulesVO getWithdrawRules() {
        String minAmount = iSysConfigService.getConfigValue(ReferralConstant.withdraw.SYS_WITHDRAW_MIN);
        String maxAmount = iSysConfigService.getConfigValue(ReferralConstant.withdraw.SYS_WITHDRAW_MAX);
        String serviceFeeRate = iSysConfigService.getConfigValue(ReferralConstant.withdraw.SYS_WITHDRAW_RATE);

        AppWithdrawRulesVO vo = new AppWithdrawRulesVO();
        vo.setMinAmount(parseConfigAmount(minAmount, WithdrawAmountValidator.DEFAULT_MIN));
        vo.setMaxAmount(parseConfigAmount(maxAmount, WithdrawAmountValidator.DEFAULT_MAX));
        vo.setFeeRate(parseConfigAmount(serviceFeeRate, BigDecimal.ZERO));
        vo.setStepAmount(WithdrawAmountValidator.STEP);
        return vo;
    }

    @Override
    public IPage<WithdrawRecordVo> pageInfo(WithdrawRecordPageBo withdrawRecordPageBo) {
        Page<WithdrawRecord> initPage = PageUtil.initPage(withdrawRecordPageBo.getPage(), withdrawRecordPageBo.getLimit());

        List<Long> filterUserIds = AdminUserQuerySupport.resolveFilterUserIds(
                iUserService,
                withdrawRecordPageBo.getUserId(),
                withdrawRecordPageBo.getUserName(),
                withdrawRecordPageBo.getPhone()
        );
        if (filterUserIds != null && filterUserIds.isEmpty()) {
            return PageUtil.copyPage(initPage, Collections.emptyList());
        }

        QueryWrapper<WithdrawRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(Objects.nonNull(withdrawRecordPageBo.getUserId()), WithdrawRecord::getUserId, withdrawRecordPageBo.getUserId())
                .in(filterUserIds != null, WithdrawRecord::getUserId, filterUserIds)
                .eq(Objects.nonNull(withdrawRecordPageBo.getStatus()), WithdrawRecord::getStatus, withdrawRecordPageBo.getStatus())
                .orderByDesc(WithdrawRecord::getCreateTime);

        IPage<WithdrawRecord> iPage = iWithdrawRecordService.page(initPage, queryWrapper);
        List<WithdrawRecordVo> voList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(iPage.getRecords())) {
            Map<Long, UserEntity> userMap = AdminUserQuerySupport.loadUserMap(
                    iUserService,
                    iPage.getRecords().stream().map(WithdrawRecord::getUserId).collect(Collectors.toList())
            );
            for (WithdrawRecord record : iPage.getRecords()) {
                WithdrawRecordVo vo = new WithdrawRecordVo();
                BeanUtils.copyProperties(record, vo);
                AdminUserQuerySupport.fillUserDisplay(userMap.get(record.getUserId()), vo);

                Integer withdrawalAccountType = mapToWithdrawalAccountType(record.getAccountType());
                if (withdrawalAccountType != null) {
                    WithdrawalAccount withdrawalAccount = iWithdrawalAccountService.getOne(new LambdaQueryWrapper<WithdrawalAccount>()
                            .eq(WithdrawalAccount::getUserId, record.getUserId())
                            .eq(WithdrawalAccount::getType, withdrawalAccountType)
                    );
                    if (Objects.nonNull(withdrawalAccount)) {
                        vo.setWithdrawalAccount(withdrawalAccount);
                    }
                }

                voList.add(vo);
            }
        }

        return PageUtil.copyPage(iPage, voList);
    }

    @Override
    public WithdrawRecordVo info(WithdrawRecordInfoBo withdrawRecordInfoBo) {
        WithdrawRecord withdrawRecord = iWithdrawRecordService.getById(withdrawRecordInfoBo.getId());
        if (Objects.isNull(withdrawRecord)) {
            throw new BizException("信息不存在");
        }

        WithdrawRecordVo vo = new WithdrawRecordVo();
        BeanUtils.copyProperties(withdrawRecord, vo);
        AdminUserQuerySupport.fillUserDisplay(iUserService.getById(withdrawRecord.getUserId()), vo);

        Integer withdrawalAccountType = mapToWithdrawalAccountType(withdrawRecord.getAccountType());
        if (withdrawalAccountType != null) {
            WithdrawalAccount withdrawalAccount = iWithdrawalAccountService.getOne(new LambdaQueryWrapper<WithdrawalAccount>()
                    .eq(WithdrawalAccount::getUserId, withdrawRecord.getUserId())
                    .eq(WithdrawalAccount::getType, withdrawalAccountType)
            );
            if (Objects.nonNull(withdrawalAccount)) {
                vo.setWithdrawalAccount(withdrawalAccount);
            }
        }

        return vo;
    }

    @Override
    public void updateInfo(WithdrawRecordUpdateBo withdrawRecordUpdateBo) {
        WithdrawRecord withdrawRecord = new WithdrawRecord();
        BeanUtils.copyProperties(withdrawRecordUpdateBo, withdrawRecord);
        iWithdrawRecordService.updateById(withdrawRecord);
    }

    @Override
    public void delete(WithdrawRecordInfoBo withdrawRecordInfoBo) {
        iWithdrawRecordService.removeById(withdrawRecordInfoBo.getId());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void confirm(WithdrawRecordConfirmBo withdrawRecordConfirmBo) {
        WithdrawRecord withdrawRecord = iWithdrawRecordService.getById(withdrawRecordConfirmBo.getId());
        if (Objects.isNull(withdrawRecord)) {
            throw new BizException("提现信息不存在");
        }
        if (!Objects.equals(withdrawRecord.getStatus(), WithdrawChannelConstants.STATUS_PENDING)) {
            throw new BizException("该提现申请已处理");
        }

        if (Objects.equals(withdrawRecordConfirmBo.getStatus(), WithdrawChannelConstants.STATUS_SUCCESS)) {
            withdrawRecord.setStatus(WithdrawChannelConstants.STATUS_SUCCESS);
            withdrawRecord.setWithdrawTime(LocalDateTime.now());
            iWithdrawRecordService.updateById(withdrawRecord);
            updateWalletTransactionStatus(withdrawRecord.getId(), 2);
            return;
        }

        if (Objects.equals(withdrawRecordConfirmBo.getStatus(), WithdrawChannelConstants.STATUS_REJECTED)) {
            withdrawRecord.setStatus(WithdrawChannelConstants.STATUS_REJECTED);
            withdrawRecord.setReason(withdrawRecordConfirmBo.getReason());
            iWithdrawRecordService.updateById(withdrawRecord);

            iUserWalletService.incr(withdrawRecord.getUserId(), withdrawRecord.getAmount());
            recordWalletTransaction(withdrawRecord.getUserId(), withdrawRecord.getAmount(), withdrawRecord.getId(),
                    1, 2, "提现拒绝退回", "提现审核拒绝，余额已退回");
            updateWalletTransactionStatus(withdrawRecord.getId(), 3);
            return;
        }

        throw new BizException("非法操作");
    }

    private int resolveAccountType(Integer accountType) {
        int resolved = accountType == null ? WithdrawChannelConstants.WECHAT : accountType;
        if (resolved != WithdrawChannelConstants.WECHAT && resolved != WithdrawChannelConstants.ALIPAY) {
            throw new BizException("不支持的提现渠道");
        }
        return resolved;
    }

    private void ensureWithdrawalAccountExists(Long userId, int accountType) {
        WithdrawalAccount account = iWithdrawalAccountService.getOne(new LambdaQueryWrapper<WithdrawalAccount>()
                .eq(WithdrawalAccount::getUserId, userId)
                .eq(WithdrawalAccount::getType, mapToWithdrawalAccountType(accountType))
                .last("LIMIT 1"));
        if (Objects.isNull(account)) {
            String channelName = accountType == WithdrawChannelConstants.WECHAT ? "微信" : "支付宝";
            throw new BizException("请先绑定" + channelName + "提现账户");
        }
        int withdrawalAccountType = mapToWithdrawalAccountType(accountType);
        if (!RealNameSupport.isWithdrawBindingReady(account, withdrawalAccountType)) {
            String channelName = accountType == WithdrawChannelConstants.WECHAT ? "微信收款码" : "支付宝账户";
            throw new BizException("请先绑定" + channelName);
        }
    }

    private Integer mapToWithdrawalAccountType(Integer recordAccountType) {
        if (recordAccountType == null) {
            return null;
        }
        return recordAccountType == WithdrawChannelConstants.WECHAT
                ? WithdrawChannelConstants.ACCOUNT_TYPE_WECHAT
                : WithdrawChannelConstants.ACCOUNT_TYPE_ALIPAY;
    }

    private BigDecimal parseConfigAmount(String configValue, BigDecimal defaultValue) {
        if (configValue == null || configValue.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            return new BigDecimal(configValue.trim());
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    private BigDecimal loadWalletBalance(Long userId) {
        UserWallet wallet = iUserWalletService.getOne(new LambdaQueryWrapper<UserWallet>()
                .eq(UserWallet::getUserId, userId)
                .last("LIMIT 1"));
        if (wallet == null || wallet.getAmount() == null) {
            return BigDecimal.ZERO;
        }
        return wallet.getAmount();
    }

    private BigDecimal parseRate(String serviceFeeRate) {
        if (serviceFeeRate == null || serviceFeeRate.trim().isEmpty()) {
            return BigDecimal.ZERO;
        }
        try {
            return new BigDecimal(serviceFeeRate.trim());
        } catch (NumberFormatException ex) {
            return BigDecimal.ZERO;
        }
    }

    private void recordWalletTransaction(Long userId, BigDecimal amount, Long bizId,
                                         int direction, int status, String title, String description) {
        UserWalletTransaction transaction = new UserWalletTransaction();
        transaction.setUserId(userId);
        transaction.setAmount(amount);
        transaction.setTransactionType(5);
        transaction.setDirection(direction);
        transaction.setBizType(3);
        transaction.setBizId(bizId);
        transaction.setStatus(status);
        transaction.setTitle(title);
        transaction.setDescription(description);
        iUserWalletTransactionService.save(transaction);
    }

    private void updateWalletTransactionStatus(Long withdrawRecordId, int status) {
        UserWalletTransaction pending = iUserWalletTransactionService.getOne(new LambdaQueryWrapper<UserWalletTransaction>()
                .eq(UserWalletTransaction::getBizId, withdrawRecordId)
                .eq(UserWalletTransaction::getDirection, 2)
                .eq(UserWalletTransaction::getTitle, "提现申请")
                .orderByDesc(UserWalletTransaction::getCreateTime)
                .last("LIMIT 1"));
        if (pending != null) {
            pending.setStatus(status);
            iUserWalletTransactionService.updateById(pending);
        }
    }

    private AppWithdrawApplyResultVO buildApplyResult(WithdrawRecord record) {
        AppWithdrawApplyResultVO vo = new AppWithdrawApplyResultVO();
        vo.setRecordId(record.getId());
        vo.setAmount(record.getAmount());
        vo.setActualAmount(record.getActualAmount());
        vo.setServiceFee(record.getServiceFee());
        vo.setAccountType(record.getAccountType());
        vo.setAccountTypeName(resolveAccountTypeName(record.getAccountType()));
        vo.setStatus(record.getStatus());
        vo.setStatusName("待审核");
        vo.setCreateTime(record.getCreateTime());
        return vo;
    }

    private String resolveAccountTypeName(Integer accountType) {
        if (Objects.equals(accountType, WithdrawChannelConstants.WECHAT)) {
            return "微信零钱";
        }
        if (Objects.equals(accountType, WithdrawChannelConstants.ALIPAY)) {
            return "支付宝";
        }
        return "未知渠道";
    }
}
