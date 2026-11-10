package com.sqx.app.base.user.service.impl;

import com.sqx.app.base.user.delete.domain.vo.AppUserDeleteAccountVO;
import com.sqx.app.base.user.delete.service.UserAccountDeletionRequestService;
import com.sqx.common.core.domain.Result;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * POST /app/user/delete 控制器委托单元测试（冷静期申请）
 */
@ExtendWith(MockitoExtension.class)
class UserServiceImplDeleteAppAccountTest {

    @Mock
    private UserAccountDeletionRequestService userAccountDeletionRequestService;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void deleteAppAccount_shouldDelegateToRequestService() {
        AppUserDeleteAccountVO vo = new AppUserDeleteAccountVO();
        vo.setPending(true);
        vo.setDeleted(false);
        vo.setCoolingDays(7);
        when(userAccountDeletionRequestService.requestDeletion(100L)).thenReturn(vo);

        Result<AppUserDeleteAccountVO> result = userService.deleteAppAccount(100L);

        verify(userAccountDeletionRequestService).requestDeletion(100L);
        assertTrue(result.getData().getPending());
        assertFalse(result.getData().getDeleted());
    }
}
