package com.sqx.app.referral.members.support;

import com.sqx.app.base.user.domain.entity.UserEntity;
import com.sqx.app.referral.members.constants.ReferralMemberConstant;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReferralMemberDisplaySupportTest {

    @Test
    void resolveDisplayName_whenNicknamePresent_shouldUseNickname() {
        UserEntity member = new UserEntity();
        member.setNickname("张华");
        member.setPhone("13900001111");

        assertEquals("张华", ReferralMemberDisplaySupport.resolveDisplayName(member));
    }

    @Test
    void resolveDisplayName_whenNoNickname_shouldMaskPhone() {
        UserEntity member = new UserEntity();
        member.setPhone("13912345678");

        assertEquals("139****5678", ReferralMemberDisplaySupport.resolveDisplayName(member));
    }

    @Test
    void resolvePayStatus_whenVip_shouldReturnPaid() {
        UserEntity member = new UserEntity();
        member.setIsVip(1);

        assertEquals(ReferralMemberConstant.STATUS_PAID, ReferralMemberDisplaySupport.resolvePayStatus(member));
    }

    @Test
    void resolvePayStatus_whenNotVip_shouldReturnUnpaid() {
        UserEntity member = new UserEntity();
        member.setIsVip(0);

        assertEquals(ReferralMemberConstant.STATUS_UNPAID, ReferralMemberDisplaySupport.resolvePayStatus(member));
    }

    @Test
    void formatRegisterDate_shouldFormatAsYyyyMmDd() {
        LocalDateTime createTime = LocalDateTime.of(2026, 5, 19, 10, 30);

        assertEquals("2026-05-19", ReferralMemberDisplaySupport.formatRegisterDate(createTime));
    }
}
