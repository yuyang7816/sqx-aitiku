package com.sqx.app.base.user.service.impl;

import com.sqx.app.base.enums.AppError;
import com.sqx.app.base.user.db.service.IUserService;
import com.sqx.app.base.user.domain.bo.UpdateAppUserProfileBO;
import com.sqx.app.base.user.domain.entity.UserEntity;
import com.sqx.app.base.user.domain.vo.AppUserProfileVO;
import com.sqx.app.base.verifyCode.service.VerifyCodeService;
import com.sqx.common.core.domain.Result;
import com.sqx.common.core.exception.BizException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * PUT/POST /app/user 更新学习计划资料单元测试
 */
@ExtendWith(MockitoExtension.class)
class UserServiceImplUpdateAppProfileTest {

    @Mock
    private IUserService iUserService;
    @Mock
    private VerifyCodeService verifyCodeService;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void updateAppUserProfile_whenUserNotFound_shouldThrow() {
        when(iUserService.getById(999L)).thenReturn(null);

        UpdateAppUserProfileBO bo = buildUpdateBO();

        BizException ex = assertThrows(BizException.class,
                () -> userService.updateAppUserProfile(999L, bo));
        assertEquals(AppError.USER_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void updateAppUserProfile_shouldPersistGradeSubjectsVersionReady() {
        UserEntity existing = buildUser();
        UserEntity updated = buildUser();
        updated.setGrade("初中三年级");
        updated.setGradeShort("初三");
        updated.setTextbookVersion("人教版");
        updated.setSubjects("[\"数学\",\"物理\"]");
        updated.setProfileReady(1);

        when(iUserService.getById(100L)).thenReturn(existing, updated);

        Result<AppUserProfileVO> result = userService.updateAppUserProfile(100L, buildUpdateBO());

        ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
        verify(iUserService).updateById(captor.capture());
        UserEntity saved = captor.getValue();
        assertEquals(100L, saved.getUserId());
        assertEquals("初中三年级", saved.getGrade());
        assertEquals("初三", saved.getGradeShort());
        assertEquals("人教版", saved.getTextbookVersion());
        assertEquals("[\"数学\",\"物理\"]", saved.getSubjects());
        assertEquals(1, saved.getProfileReady());

        AppUserProfileVO vo = result.getData();
        assertEquals("初中三年级", vo.getGrade());
        assertEquals("初三", vo.getGradeShort());
        assertEquals("人教版", vo.getVersion());
        assertEquals(Arrays.asList("数学", "物理"), vo.getSubjects());
        assertTrue(vo.getReady());
    }

    @Test
    void updateAppUserProfile_whenReadyFalse_shouldPersistProfileReadyZero() {
        UserEntity existing = buildUser();
        UserEntity updated = buildUser();
        updated.setGrade("初中一年级");
        updated.setGradeShort("初一");
        updated.setTextbookVersion("北师大版");
        updated.setSubjects("[\"语文\"]");
        updated.setProfileReady(0);

        when(iUserService.getById(100L)).thenReturn(existing, updated);

        UpdateAppUserProfileBO bo = buildUpdateBO();
        bo.setGrade("初中一年级");
        bo.setVersion("北师大版");
        bo.setSubjects(Collections.singletonList("语文"));
        bo.setReady(false);

        Result<AppUserProfileVO> result = userService.updateAppUserProfile(100L, bo);

        ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
        verify(iUserService).updateById(captor.capture());
        assertEquals(0, captor.getValue().getProfileReady());
        assertEquals(false, result.getData().getReady());
    }

    @Test
    void updateAppUserProfile_shouldPersistNicknameAndAvatar() {
        UserEntity existing = buildUser();
        UserEntity updated = buildUser();
        updated.setNickname("小明");
        updated.setAvatar("https://example.com/avatar.png");
        updated.setGrade("初中三年级");
        updated.setGradeShort("初三");
        updated.setTextbookVersion("人教版");
        updated.setSubjects("[\"数学\",\"物理\"]");
        updated.setProfileReady(1);

        when(iUserService.getById(100L)).thenReturn(existing, updated);

        UpdateAppUserProfileBO bo = buildUpdateBO();
        bo.setNickname("小明");
        bo.setAvatar("https://example.com/avatar.png");

        Result<AppUserProfileVO> result = userService.updateAppUserProfile(100L, bo);

        ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
        verify(iUserService).updateById(captor.capture());
        assertEquals("小明", captor.getValue().getNickname());
        assertEquals("https://example.com/avatar.png", captor.getValue().getAvatar());
        assertEquals("小明", result.getData().getNickname());
        assertEquals("https://example.com/avatar.png", result.getData().getAvatar());
    }

    private UpdateAppUserProfileBO buildUpdateBO() {
        UpdateAppUserProfileBO bo = new UpdateAppUserProfileBO();
        bo.setGrade("初中三年级");
        bo.setVersion("人教版");
        bo.setSubjects(Arrays.asList("数学", "物理"));
        bo.setReady(true);
        return bo;
    }

    private UserEntity buildUser() {
        UserEntity user = new UserEntity();
        user.setUserId(100L);
        user.setPhone("13800138000");
        user.setUserName("微信用户");
        user.setIsVip(0);
        return user;
    }
}
