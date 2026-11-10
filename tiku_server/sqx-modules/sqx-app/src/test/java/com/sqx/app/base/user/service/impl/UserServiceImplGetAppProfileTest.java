package com.sqx.app.base.user.service.impl;

import com.sqx.app.base.enums.AppError;
import com.sqx.app.base.user.db.service.IUserService;
import com.sqx.app.base.user.domain.entity.UserEntity;
import com.sqx.app.base.user.domain.vo.AppUserProfileVO;
import com.sqx.app.base.verifyCode.service.VerifyCodeService;
import com.sqx.app.vip.expire.service.VipExpireService;
import com.sqx.common.core.domain.Result;
import com.sqx.common.core.exception.BizException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

/**
 * GET /app/user 用户资料单元测试
 */
@ExtendWith(MockitoExtension.class)
class UserServiceImplGetAppProfileTest {

    @Mock
    private IUserService iUserService;
    @Mock
    private VerifyCodeService verifyCodeService;
    @Mock
    private VipExpireService vipExpireService;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void getAppUserProfile_whenUserNotFound_shouldThrow() {
        when(vipExpireService.refreshIfExpired(999L)).thenReturn(null);

        BizException ex = assertThrows(BizException.class, () -> userService.getAppUserProfile(999L));
        assertEquals(AppError.USER_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void getAppUserProfile_whenProfileComplete_shouldReturnAllFields() {
        UserEntity user = buildUser();
        user.setNickname("李同学");
        user.setGrade("初中三年级");
        user.setGradeShort("初三");
        user.setTextbookVersion("人教版（默认）");
        user.setSubjects("[\"数学\",\"物理\"]");
        user.setProfileReady(1);
        user.setIsVip(1);
        when(vipExpireService.refreshIfExpired(100L)).thenReturn(user);

        Result<AppUserProfileVO> result = userService.getAppUserProfile(100L);

        AppUserProfileVO vo = result.getData();
        assertEquals(100L, vo.getUserId());
        assertEquals("13800138000", vo.getPhone());
        assertEquals("李同学", vo.getNickname());
        assertEquals("初中三年级", vo.getGrade());
        assertEquals("初三", vo.getGradeShort());
        assertEquals("人教版", vo.getVersion());
        assertEquals(Arrays.asList("数学", "物理"), vo.getSubjects());
        assertTrue(vo.getReady());
        assertTrue(vo.getVip());
    }

    @Test
    void getAppUserProfile_whenNewUser_shouldReturnReadyFalseAndEmptySubjects() {
        UserEntity user = buildUser();
        user.setUserName("微信用户");
        user.setProfileReady(0);
        user.setIsVip(0);
        when(vipExpireService.refreshIfExpired(100L)).thenReturn(user);

        Result<AppUserProfileVO> result = userService.getAppUserProfile(100L);

        AppUserProfileVO vo = result.getData();
        assertEquals("微信用户", vo.getNickname());
        assertFalse(vo.getReady());
        assertFalse(vo.getVip());
        assertEquals(Collections.emptyList(), vo.getSubjects());
        assertNull(vo.getGrade());
        assertNull(vo.getVersion());
    }

    @Test
    void toAppUserProfileVO_shouldDeriveGradeShortWhenMissing() {
        UserEntity user = buildUser();
        user.setGrade("初中二年级");
        user.setProfileReady(0);

        AppUserProfileVO vo = userService.toAppUserProfileVO(user);

        assertEquals("初二", vo.getGradeShort());
    }


    @Test
    void getAppUserProfile_whenVip_shouldReturnExpireAt() {
        UserEntity user = buildUser();
        user.setIsVip(1);
        user.setVipExpireAt(LocalDateTime.of(2026, 9, 15, 23, 59, 59));
        when(vipExpireService.refreshIfExpired(100L)).thenReturn(user);

        Result<AppUserProfileVO> result = userService.getAppUserProfile(100L);

        AppUserProfileVO vo = result.getData();
        assertTrue(vo.getVip());
        assertEquals("2026-09-15", vo.getVipExpireAt());
    }

    private UserEntity buildUser() {
        UserEntity user = new UserEntity();
        user.setUserId(100L);
        user.setPhone("13800138000");
        user.setAvatar("https://example.com/avatar.png");
        return user;
    }
}
