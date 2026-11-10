package com.sqx.app.referral.withdraw.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sqx.app.base.user.db.service.IUserService;
import com.sqx.app.base.user.domain.entity.UserEntity;
import com.sqx.app.referral.constants.ReferralConstant;
import com.sqx.app.referral.withdraw.constants.WithdrawChannelConstants;
import com.sqx.app.referral.withdraw.db.service.IWithdrawRecordService;
import com.sqx.app.referral.withdraw.db.service.IWithdrawalAccountService;
import com.sqx.app.referral.withdraw.domain.bo.AppWithdrawApplyBO;
import com.sqx.app.referral.withdraw.domain.entity.WithdrawRecord;
import com.sqx.app.referral.withdraw.domain.entity.WithdrawalAccount;
import com.sqx.app.referral.withdraw.domain.vo.AppWithdrawApplyResultVO;
import com.sqx.app.wallet.transaction.db.service.IUserWalletTransactionService;
import com.sqx.app.wallet.wallet.db.service.IUserWalletService;
import com.sqx.app.wallet.wallet.domain.entity.UserWallet;
import com.sqx.common.core.exception.BizException;
import com.sqx.system.db.service.ISysConfigService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.session.Configuration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * POST /app/withdrawRecord 提现申请单元测试
 */
@ExtendWith(MockitoExtension.class)
class WithdrawRecordServiceImplApplyTest {

    @Mock
    private IWithdrawRecordService iWithdrawRecordService;
    @Mock
    private ISysConfigService iSysConfigService;
    @Mock
    private IUserService iUserService;
    @Mock
    private IWithdrawalAccountService iWithdrawalAccountService;
    @Mock
    private IUserWalletService iUserWalletService;
    @Mock
    private IUserWalletTransactionService iUserWalletTransactionService;

    @InjectMocks
    private WithdrawRecordServiceImpl withdrawRecordService;

    @BeforeEach
    void initEntityCache() {
        Configuration configuration = new Configuration();
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(configuration, "test");
        TableInfoHelper.initTableInfo(assistant, WithdrawRecord.class);
        TableInfoHelper.initTableInfo(assistant, WithdrawalAccount.class);
        TableInfoHelper.initTableInfo(assistant, UserWallet.class);
    }

    @Test
    void applyWithdraw_whenUserMissing_shouldThrow() {
        when(iUserService.getById(1L)).thenReturn(null);

        AppWithdrawApplyBO applyBO = new AppWithdrawApplyBO();
        applyBO.setAmount(new BigDecimal("100"));

        assertThrows(BizException.class, () -> withdrawRecordService.applyWithdraw(1L, applyBO));
    }

    @Test
    void applyWithdraw_whenAccountMissing_shouldThrow() {
        when(iUserService.getById(100L)).thenReturn(buildUser(100L));
        when(iWithdrawalAccountService.getOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        AppWithdrawApplyBO applyBO = new AppWithdrawApplyBO();
        applyBO.setAmount(new BigDecimal("100"));

        BizException ex = assertThrows(BizException.class, () -> withdrawRecordService.applyWithdraw(100L, applyBO));
        assertEquals("请先绑定微信零钱提现账户", ex.getMessage());
    }

    @Test
    void applyWithdraw_whenBindingIncomplete_shouldThrow() {
        when(iUserService.getById(150L)).thenReturn(buildUser(150L));
        when(iWithdrawalAccountService.getOne(any(LambdaQueryWrapper.class))).thenReturn(buildIncompleteWechatAccount(150L));

        AppWithdrawApplyBO applyBO = new AppWithdrawApplyBO();
        applyBO.setAmount(new BigDecimal("100"));

        BizException ex = assertThrows(BizException.class, () -> withdrawRecordService.applyWithdraw(150L, applyBO));
        assertEquals("请先绑定微信收款码", ex.getMessage());
    }

    @Test
    void applyWithdraw_whenValid_shouldSavePendingRecordAndDecrWallet() {
        when(iUserService.getById(200L)).thenReturn(buildUser(200L));
        when(iWithdrawalAccountService.getOne(any(LambdaQueryWrapper.class))).thenReturn(buildWechatAccount(200L));
        when(iUserWalletService.getOne(any(LambdaQueryWrapper.class))).thenReturn(buildWallet(200L, "500"));
        when(iSysConfigService.getConfigValue(ReferralConstant.withdraw.SYS_WITHDRAW_MIN)).thenReturn("50");
        when(iSysConfigService.getConfigValue(ReferralConstant.withdraw.SYS_WITHDRAW_MAX)).thenReturn("5000");
        when(iSysConfigService.getConfigValue(ReferralConstant.withdraw.SYS_WITHDRAW_RATE)).thenReturn("0");
        when(iWithdrawRecordService.save(any(WithdrawRecord.class))).thenAnswer(invocation -> {
            WithdrawRecord record = invocation.getArgument(0);
            record.setId(9001L);
            record.setCreateTime(java.time.LocalDateTime.of(2026, 8, 12, 10, 0, 0));
            return true;
        });

        AppWithdrawApplyBO applyBO = new AppWithdrawApplyBO();
        applyBO.setAmount(new BigDecimal("100"));

        AppWithdrawApplyResultVO result = withdrawRecordService.applyWithdraw(200L, applyBO);

        assertNotNull(result);
        assertEquals(9001L, result.getRecordId());
        assertEquals(new BigDecimal("100"), result.getAmount());
        assertEquals(WithdrawChannelConstants.STATUS_PENDING, result.getStatus());
        assertEquals("待审核", result.getStatusName());
        assertEquals("微信零钱", result.getAccountTypeName());

        ArgumentCaptor<WithdrawRecord> recordCaptor = ArgumentCaptor.forClass(WithdrawRecord.class);
        verify(iWithdrawRecordService).save(recordCaptor.capture());
        assertEquals(WithdrawChannelConstants.WECHAT, recordCaptor.getValue().getAccountType());
        assertEquals(WithdrawChannelConstants.STATUS_PENDING, recordCaptor.getValue().getStatus());

        verify(iUserWalletService).decr(eq(200L), eq(new BigDecimal("100")));
        verify(iUserWalletTransactionService).save(any());
    }

    private UserEntity buildUser(Long userId) {
        UserEntity user = new UserEntity();
        user.setUserId(userId);
        return user;
    }

    private WithdrawalAccount buildWechatAccount(Long userId) {
        WithdrawalAccount account = new WithdrawalAccount();
        account.setUserId(userId);
        account.setType(WithdrawChannelConstants.ACCOUNT_TYPE_WECHAT);
        account.setAccountNumber("https://cdn.example.com/wechat-qr.png");
        account.setRealNameVerified(1);
        return account;
    }

    private WithdrawalAccount buildIncompleteWechatAccount(Long userId) {
        WithdrawalAccount account = new WithdrawalAccount();
        account.setUserId(userId);
        account.setType(WithdrawChannelConstants.ACCOUNT_TYPE_WECHAT);
        account.setAccountName("微信");
        account.setRealNameVerified(0);
        return account;
    }

    private UserWallet buildWallet(Long userId, String amount) {
        UserWallet wallet = new UserWallet();
        wallet.setUserId(userId);
        wallet.setAmount(new BigDecimal(amount));
        return wallet;
    }
}
