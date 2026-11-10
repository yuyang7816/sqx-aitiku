package com.sqx.app.referral.members.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.sqx.app.base.user.db.service.IUserService;
import com.sqx.app.base.user.domain.entity.UserEntity;
import com.sqx.app.referral.commission.db.mapper.ReferralCommissionMapper;
import com.sqx.app.referral.members.constants.ReferralMemberConstant;
import com.sqx.app.referral.members.domain.vo.AppReferralMemberVO;
import com.sqx.common.core.exception.BizException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * GET /app/referral/members 推广下级成员列表单元测试
 */
@ExtendWith(MockitoExtension.class)
class ReferralMembersServiceImplTest {

    @Mock
    private IUserService iUserService;

    @Mock
    private ReferralCommissionMapper referralCommissionMapper;

    @InjectMocks
    private ReferralMembersServiceImpl referralMembersService;

    @Test
    void listMembers_whenUserNotFound_shouldThrow() {
        when(iUserService.getById(999L)).thenReturn(null);

        assertThrows(BizException.class, () -> referralMembersService.listMembers(999L));
    }

    @Test
    void listMembers_whenInvitationCodeBlank_shouldReturnEmpty() {
        UserEntity inviter = buildInviter(100L, "  ");
        when(iUserService.getById(100L)).thenReturn(inviter);

        List<AppReferralMemberVO> result = referralMembersService.listMembers(100L);

        assertTrue(result.isEmpty());
    }

    @Test
    void listMembers_whenHasSubordinates_shouldReturnMappedList() {
        UserEntity inviter = buildInviter(200L, "INV200");
        UserEntity paidMember = buildMember("张华", null, 1, LocalDateTime.of(2026, 5, 19, 9, 0));
        UserEntity unpaidMember = buildMember(null, "13900008888", 0, LocalDateTime.of(2026, 5, 18, 9, 0));

        when(iUserService.getById(200L)).thenReturn(inviter);
        when(iUserService.list(any(Wrapper.class))).thenReturn(Arrays.asList(paidMember, unpaidMember));

        List<AppReferralMemberVO> result = referralMembersService.listMembers(200L);

        assertEquals(2, result.size());
        assertEquals("张华", result.get(0).getName());
        assertEquals("2026-05-19", result.get(0).getTime());
        assertEquals(ReferralMemberConstant.STATUS_PAID, result.get(0).getStatus());
        assertEquals("139****8888", result.get(1).getName());
        assertEquals(ReferralMemberConstant.STATUS_UNPAID, result.get(1).getStatus());
        verify(iUserService).list(any(Wrapper.class));
    }

    @Test
    void listMembers_whenNoSubordinates_shouldReturnEmpty() {
        UserEntity inviter = buildInviter(300L, "INV300");
        when(iUserService.getById(300L)).thenReturn(inviter);
        when(iUserService.list(any(Wrapper.class))).thenReturn(Collections.emptyList());

        List<AppReferralMemberVO> result = referralMembersService.listMembers(300L);

        assertTrue(result.isEmpty());
    }

    private UserEntity buildInviter(Long userId, String invitationCode) {
        UserEntity user = new UserEntity();
        user.setUserId(userId);
        user.setInvitationCode(invitationCode);
        return user;
    }

    private UserEntity buildMember(String nickname, String phone, Integer isVip, LocalDateTime createTime) {
        UserEntity member = new UserEntity();
        member.setNickname(nickname);
        member.setPhone(phone);
        member.setIsVip(isVip);
        member.setCreateTime(createTime);
        return member;
    }
}
