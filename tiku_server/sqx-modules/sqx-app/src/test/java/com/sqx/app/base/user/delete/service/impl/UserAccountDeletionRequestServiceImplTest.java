package com.sqx.app.base.user.delete.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.sqx.app.base.enums.AppError;
import com.sqx.app.base.user.db.service.IUserService;
import com.sqx.app.base.user.delete.constants.AccountDeletionConstant;
import com.sqx.app.base.user.delete.db.service.IUserAccountDeletionRequestService;
import com.sqx.app.base.user.delete.domain.entity.UserAccountDeletionRequest;
import com.sqx.app.base.user.delete.domain.enums.DeletionRequestStatus;
import com.sqx.app.base.user.delete.domain.vo.AppUserDeleteAccountVO;
import com.sqx.app.base.user.delete.service.UserAccountDeletionService;
import com.sqx.app.base.user.domain.entity.UserEntity;
import com.sqx.app.base.user.enums.UserStatus;
import com.sqx.common.core.exception.BizException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserAccountDeletionRequestServiceImplTest {

    private static final Long USER_ID = 100L;

    @Mock
    private IUserService iUserService;
    @Mock
    private IUserAccountDeletionRequestService iUserAccountDeletionRequestService;
    @Mock
    private UserAccountDeletionService userAccountDeletionService;

    @InjectMocks
    private UserAccountDeletionRequestServiceImpl service;

    @Test
    void requestDeletion_shouldCreatePendingRequestWithoutDeletingData() {
        when(iUserService.getById(USER_ID)).thenReturn(buildActiveUser());
        when(iUserAccountDeletionRequestService.getOne(any(), any(Boolean.class))).thenReturn(null);
        when(iUserAccountDeletionRequestService.save(any(UserAccountDeletionRequest.class))).thenAnswer(invocation -> {
            UserAccountDeletionRequest request = invocation.getArgument(0);
            request.setId(1L);
            return true;
        });

        AppUserDeleteAccountVO vo = service.requestDeletion(USER_ID);

        assertTrue(vo.getPending());
        assertFalse(vo.getDeleted());
        assertEquals(AccountDeletionConstant.COOLING_DAYS, vo.getCoolingDays());
        verify(userAccountDeletionService, never()).deleteAccount(USER_ID);

        ArgumentCaptor<UserAccountDeletionRequest> captor = ArgumentCaptor.forClass(UserAccountDeletionRequest.class);
        verify(iUserAccountDeletionRequestService).save(captor.capture());
        assertEquals(DeletionRequestStatus.PENDING.getCode(), captor.getValue().getStatus());
    }

    @Test
    void cancelDeletion_whenNoPending_shouldThrow() {
        when(iUserService.getById(USER_ID)).thenReturn(buildActiveUser());
        when(iUserAccountDeletionRequestService.getOne(any(), any(Boolean.class))).thenReturn(null);

        BizException ex = assertThrows(BizException.class, () -> service.cancelDeletion(USER_ID));
        assertEquals(AppError.USER_DELETE_REQUEST_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void executeDueDeletions_shouldDeleteAccountWhenDue() {
        UserAccountDeletionRequest request = new UserAccountDeletionRequest();
        request.setId(9L);
        request.setUserId(USER_ID);
        request.setStatus(DeletionRequestStatus.PENDING.getCode());
        request.setEffectiveTime(LocalDateTime.now().minusHours(1));

        when(iUserAccountDeletionRequestService.list(any(Wrapper.class))).thenReturn(java.util.Collections.singletonList(request));
        when(iUserAccountDeletionRequestService.getById(9L)).thenReturn(request);

        int executed = service.executeDueDeletions();

        assertEquals(1, executed);
        verify(userAccountDeletionService).deleteAccount(USER_ID);
        assertEquals(DeletionRequestStatus.COMPLETED.getCode(), request.getStatus());
    }

    private UserEntity buildActiveUser() {
        UserEntity user = new UserEntity();
        user.setUserId(USER_ID);
        user.setPhone("13800138000");
        user.setStatus(UserStatus.ENABLE.getCode());
        return user;
    }
}
