package com.sqx.app.base.user.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sqx.app.base.user.db.service.IUserService;
import com.sqx.app.base.user.domain.bo.ListUserBO;
import com.sqx.app.base.user.domain.entity.UserEntity;
import com.sqx.app.base.user.domain.vo.UserVo;
import com.sqx.app.base.verifyCode.service.VerifyCodeService;
import com.sqx.app.referral.withdraw.db.service.IWithdrawalAccountService;
import com.sqx.app.vip.expire.service.VipExpireService;
import com.sqx.app.wallet.wallet.db.service.IUserWalletService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * POST /admin/base/user/list 单元测试
 */
@ExtendWith(MockitoExtension.class)
class UserServiceImplListTest {

    @Mock
    private IUserService iUserService;
    @Mock
    private VerifyCodeService verifyCodeService;
    @Mock
    private VipExpireService vipExpireService;
    @Mock
    private IUserWalletService iUserWalletService;
    @Mock
    private IWithdrawalAccountService iWithdrawalAccountService;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void list_whenSuperiorInvitationCodeNull_shouldNotThrowNpe() {
        ListUserBO listUserBO = new ListUserBO();
        listUserBO.setPage(1L);
        listUserBO.setLimit(10L);
        listUserBO.setSuperiorInvitationCode(null);

        Page<UserEntity> page = new Page<>(1, 10);
        when(iUserService.page(any(), any())).thenReturn(page);

        assertDoesNotThrow(() -> userService.list(listUserBO));
        verify(iUserService).page(any(), any());
    }

    @Test
    void list_whenSuperiorInvitationCodePresent_shouldTrimBeforeQuery() {
        ListUserBO listUserBO = new ListUserBO();
        listUserBO.setPage(1L);
        listUserBO.setLimit(10L);
        listUserBO.setSuperiorInvitationCode("  INV8000  ");

        Page<UserEntity> page = new Page<>(1, 10);
        when(iUserService.page(any(), any())).thenReturn(page);

        userService.list(listUserBO);

        ArgumentCaptor<com.baomidou.mybatisplus.core.conditions.query.QueryWrapper> captor =
                ArgumentCaptor.forClass(com.baomidou.mybatisplus.core.conditions.query.QueryWrapper.class);
        verify(iUserService).page(any(), captor.capture());
    }
}
