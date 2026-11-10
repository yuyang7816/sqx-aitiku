package com.sqx.app.vip.pay.service.impl;

import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.sqx.app.base.user.db.service.IUserService;
import com.sqx.app.base.user.domain.entity.UserEntity;
import com.sqx.app.referral.commission.service.ReferralCommissionTriggerService;
import com.sqx.app.vip.pay.db.service.IVipOrderService;
import com.sqx.app.vip.pay.domain.entity.VipOrder;
import com.sqx.app.vip.pay.enums.PayError;
import com.sqx.app.vip.rules.support.MemberRulesSupport;
import com.sqx.common.core.exception.BizException;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 微信支付回调开通 VIP 单元测试
 */
@ExtendWith(MockitoExtension.class)
class VipPayNotifyServiceImplTest {

    @Mock
    private IVipOrderService iVipOrderService;
    @Mock
    private IUserService iUserService;
    @Mock
    private ReferralCommissionTriggerService referralCommissionTriggerService;

    @Spy
    private MemberRulesSupport memberRulesSupport = new MemberRulesSupport();

    @InjectMocks
    private VipPayNotifyServiceImpl vipPayNotifyService;

    @BeforeEach
    void initEntityCache() {
        Configuration configuration = new Configuration();
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(configuration, "test");
        TableInfoHelper.initTableInfo(assistant, VipOrder.class);
        TableInfoHelper.initTableInfo(assistant, UserEntity.class);
    }

    @Test
    void onWechatPaySuccess_whenOrderNotFound_shouldThrow() {
        when(iVipOrderService.getOne(any())).thenReturn(null);

        BizException ex = assertThrows(BizException.class,
                () -> vipPayNotifyService.onWechatPaySuccess("ORDER001", "WX123"));
        assertEquals(PayError.ORDER_NOT_FOUND.getCode(), ex.getErrorCode().getCode());
    }

    @Test
    void onWechatPaySuccess_whenAlreadyPaid_shouldSkip() {
        VipOrder order = buildOrder(VipOrder.STATUS_PAID);
        when(iVipOrderService.getOne(any())).thenReturn(order);

        vipPayNotifyService.onWechatPaySuccess("ORDER001", "WX123");

        verify(iVipOrderService, never()).updateById(any());
        verify(iUserService, never()).update(any());
    }

    @Test
    void onWechatPaySuccess_whenPending_shouldActivateVip() {
        VipOrder order = buildOrder(VipOrder.STATUS_PENDING);
        when(iVipOrderService.getOne(any())).thenReturn(order);
        when(iVipOrderService.updateById(any())).thenReturn(true);

        UserEntity user = new UserEntity();
        user.setUserId(100L);
        user.setIsVip(0);
        when(iUserService.getById(100L)).thenReturn(user);
        when(iUserService.update(any())).thenReturn(true);

        vipPayNotifyService.onWechatPaySuccess("ORDER001", "WX123");

        verify(iVipOrderService).updateById(any());
        verify(iUserService).update(any());
        verify(referralCommissionTriggerService).triggerOnVipPaySuccess(any());
    }

    @Test
    void activateVip_shouldExtendFromExistingExpire() {
        LocalDateTime now = LocalDateTime.of(2026, 8, 13, 10, 0);
        UserEntity user = new UserEntity();
        user.setUserId(100L);
        user.setVipExpireAt(now.plusDays(10));
        when(iUserService.getById(100L)).thenReturn(user);
        when(iUserService.update(any())).thenReturn(true);

        vipPayNotifyService.activateVip(100L, 31, now);

        ArgumentCaptor<com.baomidou.mybatisplus.core.conditions.Wrapper> captor =
                ArgumentCaptor.forClass(com.baomidou.mybatisplus.core.conditions.Wrapper.class);
        verify(iUserService).update(captor.capture());
    }

    @Test
    void activateVip_whenNewUser_shouldResetQuotaAndSetExpireFromNow() {
        LocalDateTime now = LocalDateTime.of(2026, 8, 13, 10, 0);
        UserEntity user = new UserEntity();
        user.setUserId(100L);
        user.setIsVip(0);
        user.setAiUsedToday(4);
        user.setOcrUsedToday(3);
        when(iUserService.getById(100L)).thenReturn(user);
        when(iUserService.update(any())).thenReturn(true);

        vipPayNotifyService.activateVip(100L, 30, now);

        LocalDateTime expectedExpire = memberRulesSupport.calculateVipExpireAt(user, 30, now);
        assertEquals(now.plusDays(30), expectedExpire);
        verify(iUserService).update(any());
    }

    @Test
    void activateVip_shouldUseMemberRulesQuotaReset() {
        LocalDate today = LocalDate.now();
        UserEntity user = new UserEntity();
        user.setUserId(100L);
        when(iUserService.getById(100L)).thenReturn(user);
        when(iUserService.update(any())).thenReturn(true);

        vipPayNotifyService.activateVip(100L, 31, LocalDateTime.now());

        assertEquals(0, memberRulesSupport.buildActivationQuotaReset(today).getAiUsedToday());
        assertEquals(0, memberRulesSupport.buildActivationQuotaReset(today).getOcrUsedToday());
        verify(iUserService).update(any());
    }

    private VipOrder buildOrder(int payStatus) {
        VipOrder order = new VipOrder();
        order.setId(1L);
        order.setOrderNo("ORDER001");
        order.setUserId(100L);
        order.setPlanDays(31);
        order.setPayStatus(payStatus);
        return order;
    }
}
