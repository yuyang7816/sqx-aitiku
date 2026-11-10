package com.sqx.app.vip.rules.support;

import com.sqx.app.base.user.domain.entity.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 文档 23.3 会员规则单元测试
 */
class MemberRulesSupportTest {

    private MemberRulesSupport memberRulesSupport;

    @BeforeEach
    void setUp() {
        memberRulesSupport = new MemberRulesSupport();
    }

    @Test
    void isVip_whenFlagOne_shouldReturnTrue() {
        UserEntity user = buildUser(1, 0, LocalDate.now(), 0, LocalDate.now());
        assertTrue(memberRulesSupport.isVip(user));
    }

    @Test
    void isVip_whenNotVip_shouldReturnFalse() {
        UserEntity user = buildUser(0, 3, LocalDate.now(), 3, LocalDate.now());
        assertFalse(memberRulesSupport.isVip(user));
    }

    @Test
    void isQuotaUnlimited_whenVip_shouldReturnTrue() {
        UserEntity user = buildUser(1, 5, LocalDate.now(), 5, LocalDate.now());
        assertTrue(memberRulesSupport.isQuotaUnlimited(user));
    }

    @Test
    void isQuotaUnlimited_whenFreeUser_shouldReturnFalse() {
        UserEntity user = buildUser(0, 0, LocalDate.now(), 0, LocalDate.now());
        assertFalse(memberRulesSupport.isQuotaUnlimited(user));
    }

    @Test
    void resolveMemberLevel_whenVip_shouldReturnVipLabel() {
        UserEntity user = buildUser(1, 0, LocalDate.now(), 0, LocalDate.now());
        assertEquals(MemberRulesSupport.VIP_LEVEL_LABEL, memberRulesSupport.resolveMemberLevel(user));
    }

    @Test
    void resolveMemberLevel_whenFree_shouldReturnNormalLabel() {
        UserEntity user = buildUser(0, 0, LocalDate.now(), 0, LocalDate.now());
        assertEquals("普通会员", memberRulesSupport.resolveMemberLevel(user));
    }

    @Test
    void resolveAiUsedToday_whenQuotaDateNotToday_shouldReturnZero() {
        UserEntity user = buildUser(0, 5, LocalDate.now().minusDays(1), 0, LocalDate.now());
        assertEquals(0, memberRulesSupport.resolveAiUsedToday(user, LocalDate.now()));
    }

    @Test
    void resolveOcrUsedToday_whenQuotaDateNull_shouldReturnZero() {
        UserEntity user = buildUser(0, 0, LocalDate.now(), 3, null);
        assertEquals(0, memberRulesSupport.resolveOcrUsedToday(user, LocalDate.now()));
    }

    @Test
    void resolveAiUsedToday_whenSameDay_shouldReturnStoredValue() {
        LocalDate today = LocalDate.now();
        UserEntity user = buildUser(0, 3, today, 0, today);
        assertEquals(3, memberRulesSupport.resolveAiUsedToday(user, today));
    }

    @Test
    void canConsumeAiQuota_whenFreeUnderLimit_shouldReturnTrue() {
        UserEntity user = buildUser(0, 4, LocalDate.now(), 0, LocalDate.now());
        assertTrue(memberRulesSupport.canConsumeAiQuota(user, 5, LocalDate.now()));
    }

    @Test
    void canConsumeAiQuota_whenFreeExhausted_shouldReturnFalse() {
        UserEntity user = buildUser(0, 5, LocalDate.now(), 0, LocalDate.now());
        assertFalse(memberRulesSupport.canConsumeAiQuota(user, 5, LocalDate.now()));
    }

    @Test
    void canConsumeOcrQuota_whenVip_shouldReturnTrueEvenIfUsedHigh() {
        UserEntity user = buildUser(1, 0, LocalDate.now(), 99, LocalDate.now());
        assertTrue(memberRulesSupport.canConsumeOcrQuota(user, 5, LocalDate.now()));
    }

    @Test
    void calculateVipExpireAt_whenNoExistingExpire_shouldAddFromNow() {
        LocalDateTime now = LocalDateTime.of(2026, 8, 13, 10, 0);
        UserEntity user = new UserEntity();
        user.setIsVip(0);

        LocalDateTime expireAt = memberRulesSupport.calculateVipExpireAt(user, 31, now);

        assertEquals(now.plusDays(31), expireAt);
    }

    @Test
    void calculateVipExpireAt_whenExistingExpireInFuture_shouldStackFromExpire() {
        LocalDateTime now = LocalDateTime.of(2026, 8, 13, 10, 0);
        LocalDateTime existing = now.plusDays(10);
        UserEntity user = new UserEntity();
        user.setIsVip(1);
        user.setVipExpireAt(existing);

        LocalDateTime expireAt = memberRulesSupport.calculateVipExpireAt(user, 31, now);

        assertEquals(existing.plusDays(31), expireAt);
    }

    @Test
    void calculateVipExpireAt_whenExistingExpirePast_shouldAddFromNow() {
        LocalDateTime now = LocalDateTime.of(2026, 8, 13, 10, 0);
        UserEntity user = new UserEntity();
        user.setVipExpireAt(now.minusDays(1));

        LocalDateTime expireAt = memberRulesSupport.calculateVipExpireAt(user, 30, now);

        assertEquals(now.plusDays(30), expireAt);
    }

    @Test
    void buildActivationQuotaReset_shouldZeroUsedAndSetToday() {
        LocalDate today = LocalDate.of(2026, 8, 13);
        ActivationQuotaReset reset = memberRulesSupport.buildActivationQuotaReset(today);

        assertEquals(0, reset.getAiUsedToday());
        assertEquals(0, reset.getOcrUsedToday());
        assertEquals(today, reset.getAiQuotaDate());
        assertEquals(today, reset.getOcrQuotaDate());
    }

    private UserEntity buildUser(int isVip, int aiUsed, LocalDate aiDate, int ocrUsed, LocalDate ocrDate) {
        UserEntity user = new UserEntity();
        user.setIsVip(isVip);
        user.setAiUsedToday(aiUsed);
        user.setAiQuotaDate(aiDate);
        user.setOcrUsedToday(ocrUsed);
        user.setOcrQuotaDate(ocrDate);
        return user;
    }
}
