package com.sqx.app.referral.commission.service.impl;

import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.sqx.app.base.user.db.service.IUserService;
import com.sqx.app.base.user.domain.entity.UserEntity;
import com.sqx.app.referral.commission.db.service.IReferralCommissionService;
import com.sqx.app.referral.commission.domain.entity.ReferralCommission;
import com.sqx.app.referral.constants.ReferralConstant;
import com.sqx.app.referral.user.db.service.IReferralUserCommissionService;
import com.sqx.app.vip.pay.domain.entity.VipOrder;
import com.sqx.app.wallet.transaction.constants.UserWalletTransactionConstant;
import com.sqx.app.wallet.transaction.db.service.IUserWalletTransactionService;
import com.sqx.app.wallet.transaction.domain.entity.UserWalletTransaction;
import com.sqx.app.wallet.wallet.db.service.IUserWalletService;
import com.sqx.app.wallet.wallet.domain.entity.UserWallet;
import com.sqx.system.db.service.ISysConfigService;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 会员支付成功后推广佣金触发单元测试
 */
@ExtendWith(MockitoExtension.class)
class ReferralCommissionTriggerServiceImplTest {

    @Mock
    private IUserService iUserService;
    @Mock
    private ISysConfigService iSysConfigService;
    @Mock
    private IReferralCommissionService iReferralCommissionService;
    @Mock
    private IReferralUserCommissionService iReferralUserCommissionService;
    @Mock
    private IUserWalletService iUserWalletService;
    @Mock
    private IUserWalletTransactionService iUserWalletTransactionService;

    @InjectMocks
    private ReferralCommissionTriggerServiceImpl referralCommissionTriggerService;

    @BeforeEach
    void initEntityCache() {
        Configuration configuration = new Configuration();
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(configuration, "test");
        TableInfoHelper.initTableInfo(assistant, ReferralCommission.class);
        TableInfoHelper.initTableInfo(assistant, UserWallet.class);
        TableInfoHelper.initTableInfo(assistant, UserWalletTransaction.class);
        TableInfoHelper.initTableInfo(assistant, UserEntity.class);
    }

    @Test
    void triggerOnVipPaySuccess_whenNoSuperior_shouldSkip() {
        VipOrder order = buildOrder(10L, 200L, new BigDecimal("180.00"), "季卡");
        UserEntity buyer = buildBuyer(200L, null);

        when(iReferralCommissionService.count(any())).thenReturn(0L);
        when(iUserService.getById(200L)).thenReturn(buyer);

        referralCommissionTriggerService.triggerOnVipPaySuccess(order);

        verify(iReferralUserCommissionService, never()).incr(any(), any());
        verify(iUserWalletTransactionService, never()).save(any());
    }

    @Test
    void triggerOnVipPaySuccess_whenAlreadySettled_shouldSkip() {
        VipOrder order = buildOrder(11L, 200L, new BigDecimal("180.00"), "季卡");

        when(iReferralCommissionService.count(any())).thenReturn(1L);

        referralCommissionTriggerService.triggerOnVipPaySuccess(order);

        verify(iUserService, never()).getById(any());
        verify(iUserWalletTransactionService, never()).save(any());
    }

    @Test
    void triggerOnVipPaySuccess_whenHasSuperior_shouldCreateCommissionAndWalletTransaction() {
        VipOrder order = buildOrder(12L, 200L, new BigDecimal("180.00"), "季卡");
        UserEntity buyer = buildBuyer(200L, "INV8000");
        UserEntity inviter = buildInviter(100L, "INV8000");

        when(iReferralCommissionService.count(any())).thenReturn(0L);
        when(iUserService.getById(200L)).thenReturn(buyer);
        when(iUserService.getOne(any())).thenReturn(inviter);
        when(iSysConfigService.getConfigValue(ReferralConstant.commission.SYS_COMMISSION_RATE_LEVEL1)).thenReturn("0.10");
        when(iUserWalletService.getOne(any())).thenReturn(buildWallet(100L));

        referralCommissionTriggerService.triggerOnVipPaySuccess(order);

        verify(iReferralUserCommissionService).incr(100L, new BigDecimal("18.00"));
        verify(iUserWalletService).incr(100L, new BigDecimal("18.00"));

        ArgumentCaptor<ReferralCommission> commissionCaptor = ArgumentCaptor.forClass(ReferralCommission.class);
        verify(iReferralCommissionService).save(commissionCaptor.capture());
        ReferralCommission savedCommission = commissionCaptor.getValue();
        assertEquals(100L, savedCommission.getUserId());
        assertEquals(12L, savedCommission.getOrderId());
        assertEquals(new BigDecimal("18.00"), savedCommission.getAmount());
        assertEquals("张华开通季卡会员", savedCommission.getTitle());

        ArgumentCaptor<UserWalletTransaction> transactionCaptor = ArgumentCaptor.forClass(UserWalletTransaction.class);
        verify(iUserWalletTransactionService).save(transactionCaptor.capture());
        UserWalletTransaction savedTransaction = transactionCaptor.getValue();
        assertEquals(100L, savedTransaction.getUserId());
        assertEquals(new BigDecimal("18.00"), savedTransaction.getAmount());
        assertEquals(UserWalletTransactionConstant.DIRECTION_INCOME, savedTransaction.getDirection());
        assertEquals(UserWalletTransactionConstant.STATUS_COMPLETED, savedTransaction.getStatus());
        assertEquals("张华开通季卡会员", savedTransaction.getTitle());
    }

    @Test
    void triggerOnVipPaySuccess_whenInviterDisabled_shouldSkip() {
        VipOrder order = buildOrder(13L, 200L, new BigDecimal("180.00"), "季卡");
        UserEntity buyer = buildBuyer(200L, "INV8000");
        UserEntity inviter = buildInviter(100L, "INV8000");
        inviter.setStatus(2);

        when(iReferralCommissionService.count(any())).thenReturn(0L);
        when(iUserService.getById(200L)).thenReturn(buyer);
        when(iUserService.getOne(any())).thenReturn(inviter);

        referralCommissionTriggerService.triggerOnVipPaySuccess(order);

        verify(iReferralUserCommissionService, never()).incr(any(), any());
        verify(iUserWalletTransactionService, never()).save(any());
    }

    private VipOrder buildOrder(Long orderId, Long userId, BigDecimal amount, String planName) {
        VipOrder order = new VipOrder();
        order.setId(orderId);
        order.setUserId(userId);
        order.setAmount(amount);
        order.setPlanName(planName);
        return order;
    }

    private UserEntity buildBuyer(Long userId, String superiorInvitationCode) {
        UserEntity buyer = new UserEntity();
        buyer.setUserId(userId);
        buyer.setNickname("张华");
        buyer.setSuperiorInvitationCode(superiorInvitationCode);
        return buyer;
    }

    private UserEntity buildInviter(Long userId, String invitationCode) {
        UserEntity inviter = new UserEntity();
        inviter.setUserId(userId);
        inviter.setInvitationCode(invitationCode);
        inviter.setStatus(1);
        return inviter;
    }

    private UserWallet buildWallet(Long userId) {
        UserWallet wallet = new UserWallet();
        wallet.setUserId(userId);
        wallet.setAmount(BigDecimal.ZERO);
        return wallet;
    }
}
