package com.sqx.app.referral.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.sqx.app.base.user.db.service.IUserService;
import com.sqx.app.base.user.domain.entity.UserEntity;
import com.sqx.app.referral.commission.db.service.IReferralCommissionService;
import com.sqx.app.referral.user.domain.vo.AppUserCommissionSummaryVO;
import com.sqx.common.core.exception.BizException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * GET /app/referral/userCommission 推广收益概览单元测试
 */
@ExtendWith(MockitoExtension.class)
class ReferralUserCommissionServiceImplGetSummaryTest {

    @Mock
    private IUserService iUserService;

    @Mock
    private IReferralCommissionService iReferralCommissionService;

    @InjectMocks
    private ReferralUserCommissionServiceImpl referralUserCommissionService;

    @Test
    void getSummary_whenUserNotFound_shouldThrow() {
        when(iUserService.getById(999L)).thenReturn(null);

        assertThrows(BizException.class, () -> referralUserCommissionService.getSummary(999L));
    }

    @Test
    void getSummary_whenNoInviteAndNoCommission_shouldReturnZeros() {
        UserEntity user = buildUser(100L, "INV100", null);
        when(iUserService.getById(100L)).thenReturn(user);
        when(iReferralCommissionService.sumUserCommission(100L)).thenReturn(null);
        when(iReferralCommissionService.sumAmount(eq(100L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(null);

        AppUserCommissionSummaryVO result = referralUserCommissionService.getSummary(100L);

        assertNotNull(result);
        assertEquals(BigDecimal.ZERO, result.getTotalCommission());
        assertEquals(BigDecimal.ZERO, result.getTodayCommission());
        assertEquals(0L, result.getInviteCount());
    }

    @Test
    void getSummary_whenHasInviteAndCommission_shouldReturnAggregatedStats() {
        UserEntity user = buildUser(200L, "INV200", null);
        when(iUserService.getById(200L)).thenReturn(user);
        when(iUserService.count(any(Wrapper.class))).thenReturn(3L);
        when(iReferralCommissionService.sumUserCommission(200L)).thenReturn(new BigDecimal("128.50"));
        when(iReferralCommissionService.sumAmount(eq(200L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(new BigDecimal("12.00"));

        AppUserCommissionSummaryVO result = referralUserCommissionService.getSummary(200L);

        assertEquals(new BigDecimal("128.50"), result.getTotalCommission());
        assertEquals(new BigDecimal("12.00"), result.getTodayCommission());
        assertEquals(3L, result.getInviteCount());
        verify(iUserService).count(any(Wrapper.class));
        verify(iReferralCommissionService).sumUserCommission(200L);
    }

    @Test
    void getSummary_whenInvitationCodeBlank_shouldSkipInviteCountQuery() {
        UserEntity user = buildUser(300L, "  ", null);
        when(iUserService.getById(300L)).thenReturn(user);
        when(iReferralCommissionService.sumUserCommission(300L)).thenReturn(BigDecimal.ZERO);
        when(iReferralCommissionService.sumAmount(eq(300L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(BigDecimal.ZERO);

        AppUserCommissionSummaryVO result = referralUserCommissionService.getSummary(300L);

        assertEquals(0L, result.getInviteCount());
        assertEquals(BigDecimal.ZERO, result.getTotalCommission());
    }

    private UserEntity buildUser(Long userId, String invitationCode, String superiorInvitationCode) {
        UserEntity user = new UserEntity();
        user.setUserId(userId);
        user.setInvitationCode(invitationCode);
        user.setSuperiorInvitationCode(superiorInvitationCode);
        return user;
    }
}
