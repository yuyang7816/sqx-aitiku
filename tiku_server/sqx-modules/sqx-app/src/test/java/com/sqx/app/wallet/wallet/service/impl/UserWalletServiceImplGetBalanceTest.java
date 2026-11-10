package com.sqx.app.wallet.wallet.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sqx.app.base.user.db.service.IUserService;
import com.sqx.app.base.user.domain.entity.UserEntity;
import com.sqx.app.wallet.wallet.db.service.IUserWalletService;
import com.sqx.app.wallet.wallet.domain.entity.UserWallet;
import com.sqx.app.wallet.wallet.domain.vo.AppUserWalletBalanceVO;
import com.sqx.common.core.exception.BizException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * GET /app/userWallet 可提现余额单元测试
 */
@ExtendWith(MockitoExtension.class)
class UserWalletServiceImplGetBalanceTest {

    @Mock
    private IUserWalletService iUserWalletService;

    @Mock
    private IUserService iUserService;

    @InjectMocks
    private UserWalletServiceImpl userWalletService;

    @Test
    void getBalance_whenUserNotFound_shouldThrow() {
        when(iUserService.getById(999L)).thenReturn(null);

        assertThrows(BizException.class, () -> userWalletService.getBalance(999L));
    }

    @Test
    void getBalance_whenWalletMissing_shouldReturnZero() {
        when(iUserService.getById(100L)).thenReturn(buildUser(100L));
        when(iUserWalletService.getOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        AppUserWalletBalanceVO result = userWalletService.getBalance(100L);

        assertNotNull(result);
        assertEquals(BigDecimal.ZERO, result.getWithdrawableAmount());
    }

    @Test
    void getBalance_whenWalletExists_shouldReturnAmount() {
        when(iUserService.getById(200L)).thenReturn(buildUser(200L));

        UserWallet wallet = new UserWallet();
        wallet.setUserId(200L);
        wallet.setAmount(new BigDecimal("128.50"));
        when(iUserWalletService.getOne(any(LambdaQueryWrapper.class))).thenReturn(wallet);

        AppUserWalletBalanceVO result = userWalletService.getBalance(200L);

        assertEquals(new BigDecimal("128.50"), result.getWithdrawableAmount());
        verify(iUserWalletService).getOne(any(LambdaQueryWrapper.class));
    }

    @Test
    void getBalance_whenAmountNull_shouldReturnZero() {
        when(iUserService.getById(300L)).thenReturn(buildUser(300L));

        UserWallet wallet = new UserWallet();
        wallet.setUserId(300L);
        wallet.setAmount(null);
        when(iUserWalletService.getOne(any(LambdaQueryWrapper.class))).thenReturn(wallet);

        AppUserWalletBalanceVO result = userWalletService.getBalance(300L);

        assertEquals(BigDecimal.ZERO, result.getWithdrawableAmount());
    }

    private UserEntity buildUser(Long userId) {
        UserEntity user = new UserEntity();
        user.setUserId(userId);
        return user;
    }
}
