package com.sqx.app.referral.invitecode.support;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.sqx.app.base.user.db.service.IUserService;
import com.sqx.app.base.user.domain.entity.UserEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 唯一邀请码懒生成单元测试
 */
@ExtendWith(MockitoExtension.class)
class InviteCodeSupportTest {

    @Mock
    private IUserService iUserService;

    @InjectMocks
    private InviteCodeSupport inviteCodeSupport;

    @Test
    void ensureUniqueInviteCode_whenAlreadyHasCode_shouldReturnWithoutUpdate() {
        UserEntity user = buildUser(1L, "INV8000", "13800138000", null);

        String result = inviteCodeSupport.ensureUniqueInviteCode(user);

        assertEquals("INV8000", result);
        verify(iUserService, never()).updateById(any(UserEntity.class));
    }

    @Test
    void ensureUniqueInviteCode_whenBlank_shouldGenerateAndPersist() {
        UserEntity user = buildUser(2L, null, "13900139000", null);
        when(iUserService.exists(any(Wrapper.class))).thenReturn(false);

        String result = inviteCodeSupport.ensureUniqueInviteCode(user);

        assertEquals(6, result.length());
        verify(iUserService).updateById(any(UserEntity.class));
    }

    @Test
    void ensureUniqueInviteCode_whenCollision_shouldRetryUntilUnique() {
        UserEntity user = buildUser(3L, "  ", "13700137000", null);
        when(iUserService.exists(any(Wrapper.class))).thenReturn(true, true, false);

        String result = inviteCodeSupport.ensureUniqueInviteCode(user);

        assertEquals(6, result.length());
        verify(iUserService).updateById(any(UserEntity.class));
    }

    @Test
    void ensureUniqueInviteCode_whenAllRetriesCollide_shouldThrow() {
        UserEntity user = buildUser(4L, null, "13600136000", null);
        when(iUserService.exists(any(Wrapper.class))).thenReturn(true);

        assertThrows(com.sqx.common.core.exception.BizException.class,
                () -> inviteCodeSupport.ensureUniqueInviteCode(user));
    }

    @Test
    void buildInviteSeed_whenNoPhone_shouldUseOpenIdSeed() {
        UserEntity user = buildUser(5L, null, null, "openid-abc");

        String seed = inviteCodeSupport.buildInviteSeed(user);

        assertEquals(11, seed.length());
        assertTrueStartsWithDigit(seed);
    }

    private void assertTrueStartsWithDigit(String seed) {
        org.junit.jupiter.api.Assertions.assertTrue(Character.isDigit(seed.charAt(0)));
    }

    private UserEntity buildUser(Long userId, String invitationCode, String phone, String maOpenId) {
        UserEntity user = new UserEntity();
        user.setUserId(userId);
        user.setInvitationCode(invitationCode);
        user.setPhone(phone);
        user.setMaOpenId(maOpenId);
        return user;
    }
}
