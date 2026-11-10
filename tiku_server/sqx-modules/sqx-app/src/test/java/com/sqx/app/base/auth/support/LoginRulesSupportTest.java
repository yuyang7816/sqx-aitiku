package com.sqx.app.base.auth.support;

import com.sqx.app.base.enums.AppError;
import com.sqx.app.base.user.domain.entity.UserEntity;
import com.sqx.common.core.exception.BizException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 文档 23.1 登录规则单元测试：协议必选、profile.ready 门禁
 */
@ExtendWith(MockitoExtension.class)
class LoginRulesSupportTest {

    @InjectMocks
    private LoginRulesSupport loginRulesSupport;

    @Test
    void validateAgreementAccepted_whenTrue_shouldPass() {
        loginRulesSupport.validateAgreementAccepted(Boolean.TRUE);
    }

    @Test
    void validateAgreementAccepted_whenNull_shouldThrow350019() {
        BizException ex = assertThrows(BizException.class,
                () -> loginRulesSupport.validateAgreementAccepted(null));
        assertEquals(AppError.AGREEMENT_NOT_ACCEPTED, ex.getErrorCode());
    }

    @Test
    void validateAgreementAccepted_whenFalse_shouldThrow350019() {
        BizException ex = assertThrows(BizException.class,
                () -> loginRulesSupport.validateAgreementAccepted(Boolean.FALSE));
        assertEquals(AppError.AGREEMENT_NOT_ACCEPTED, ex.getErrorCode());
    }

    @Test
    void isProfileReady_whenProfileReadyIsOne_shouldReturnTrue() {
        UserEntity user = new UserEntity();
        user.setProfileReady(1);
        assertTrue(loginRulesSupport.isProfileReady(user));
    }

    @Test
    void isProfileReady_whenProfileReadyIsZero_shouldReturnFalse() {
        UserEntity user = new UserEntity();
        user.setProfileReady(0);
        assertFalse(loginRulesSupport.isProfileReady(user));
    }

    @Test
    void isProfileReady_whenProfileReadyIsNull_shouldReturnFalse() {
        UserEntity user = new UserEntity();
        assertFalse(loginRulesSupport.isProfileReady(user));
    }

    @Test
    void isProfileReady_whenUserIsNull_shouldReturnFalse() {
        assertFalse(loginRulesSupport.isProfileReady(null));
    }

    @Test
    void buildProfileState_shouldReflectProfileReady() {
        UserEntity readyUser = new UserEntity();
        readyUser.setProfileReady(1);
        assertTrue(loginRulesSupport.buildProfileState(readyUser).getReady());

        UserEntity notReadyUser = new UserEntity();
        notReadyUser.setProfileReady(0);
        assertFalse(loginRulesSupport.buildProfileState(notReadyUser).getReady());
    }
}
