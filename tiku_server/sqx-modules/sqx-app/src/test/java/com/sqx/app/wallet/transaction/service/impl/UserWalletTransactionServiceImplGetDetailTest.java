package com.sqx.app.wallet.transaction.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sqx.app.base.user.db.service.IUserService;
import com.sqx.app.base.user.domain.entity.UserEntity;
import com.sqx.app.wallet.transaction.constants.UserWalletTransactionConstant;
import com.sqx.app.wallet.transaction.db.mapper.UserWalletTransactionMapper;
import com.sqx.app.wallet.transaction.db.service.IUserWalletTransactionService;
import com.sqx.app.wallet.transaction.domain.entity.UserWalletTransaction;
import com.sqx.app.wallet.transaction.domain.vo.AppUserWalletTransactionDetailVO;
import com.sqx.app.wallet.transaction.domain.vo.AppUserWalletTransactionItemVO;
import com.sqx.common.core.exception.BizException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * GET /app/userWalletTransaction 收益流水详情单元测试
 */
@ExtendWith(MockitoExtension.class)
class UserWalletTransactionServiceImplGetDetailTest {

    @Mock
    private IUserWalletTransactionService iUserWalletTransactionService;

    @Mock
    private UserWalletTransactionMapper userWalletTransactionMapper;

    @Mock
    private IUserService iUserService;

    @InjectMocks
    private UserWalletTransactionServiceImpl userWalletTransactionService;

    @Test
    void getDetail_whenUserNotFound_shouldThrow() {
        when(iUserService.getById(999L)).thenReturn(null);

        assertThrows(BizException.class, () -> userWalletTransactionService.getDetail(999L, 1L, 10L));
    }

    @Test
    void getDetail_whenNoRecords_shouldReturnZeroSummaryAndEmptyPage() {
        UserEntity user = buildUser(100L);
        when(iUserService.getById(100L)).thenReturn(user);
        when(userWalletTransactionMapper.sumTotalIncome(100L)).thenReturn(null);
        when(userWalletTransactionMapper.sumPendingIncome(100L)).thenReturn(null);

        Page<UserWalletTransaction> emptyPage = new Page<>(1, 10);
        emptyPage.setTotal(0);
        when(iUserWalletTransactionService.page(any(Page.class), any())).thenReturn(emptyPage);

        AppUserWalletTransactionDetailVO result = userWalletTransactionService.getDetail(100L, 1L, 10L);

        assertNotNull(result);
        assertEquals(BigDecimal.ZERO, result.getTotalAmount());
        assertEquals(BigDecimal.ZERO, result.getPendingAmount());
        assertNotNull(result.getRecords());
        assertEquals(0, result.getRecords().getTotal());
    }

    @Test
    void getDetail_whenHasRecords_shouldReturnSummaryAndMappedPage() {
        UserEntity user = buildUser(200L);
        when(iUserService.getById(200L)).thenReturn(user);
        when(userWalletTransactionMapper.sumTotalIncome(200L)).thenReturn(new BigDecimal("36.50"));
        when(userWalletTransactionMapper.sumPendingIncome(200L)).thenReturn(new BigDecimal("6.00"));

        UserWalletTransaction settled = buildTransaction(1L, "张华开通季卡会员",
                new BigDecimal("18.00"), UserWalletTransactionConstant.STATUS_COMPLETED,
                LocalDateTime.of(2026, 6, 12, 11, 25));
        UserWalletTransaction pending = buildTransaction(2L, "王*五注册成功",
                new BigDecimal("6.00"), UserWalletTransactionConstant.STATUS_PENDING,
                LocalDateTime.of(2026, 6, 11, 18, 8));

        Page<UserWalletTransaction> entityPage = new Page<>(1, 10);
        entityPage.setTotal(2);
        entityPage.setRecords(Arrays.asList(settled, pending));
        when(iUserWalletTransactionService.page(any(Page.class), any())).thenReturn(entityPage);

        AppUserWalletTransactionDetailVO result = userWalletTransactionService.getDetail(200L, 1L, 10L);

        assertEquals(new BigDecimal("36.50"), result.getTotalAmount());
        assertEquals(new BigDecimal("6.00"), result.getPendingAmount());
        IPage<AppUserWalletTransactionItemVO> records = result.getRecords();
        assertEquals(2, records.getRecords().size());
        assertEquals("张华开通季卡会员", records.getRecords().get(0).getTitle());
        assertEquals("2026-06-12 11:25", records.getRecords().get(0).getTime());
        assertEquals("18.00", records.getRecords().get(0).getAmount());
        assertEquals(UserWalletTransactionConstant.LABEL_SETTLED, records.getRecords().get(0).getStatus());
        assertEquals(UserWalletTransactionConstant.LABEL_PENDING, records.getRecords().get(1).getStatus());
        verify(userWalletTransactionMapper).sumTotalIncome(200L);
        verify(userWalletTransactionMapper).sumPendingIncome(200L);
    }

    @Test
    void getDetail_whenPageNull_shouldUseDefaultPagination() {
        UserEntity user = buildUser(300L);
        when(iUserService.getById(300L)).thenReturn(user);
        when(userWalletTransactionMapper.sumTotalIncome(300L)).thenReturn(BigDecimal.ZERO);
        when(userWalletTransactionMapper.sumPendingIncome(300L)).thenReturn(BigDecimal.ZERO);

        Page<UserWalletTransaction> emptyPage = new Page<>(1, 10);
        emptyPage.setTotal(0);
        when(iUserWalletTransactionService.page(any(Page.class), any())).thenReturn(emptyPage);

        AppUserWalletTransactionDetailVO result = userWalletTransactionService.getDetail(300L, null, null);

        assertNotNull(result.getRecords());
        assertEquals(1, result.getRecords().getCurrent());
        assertEquals(10, result.getRecords().getSize());
    }

    private UserEntity buildUser(Long userId) {
        UserEntity user = new UserEntity();
        user.setUserId(userId);
        return user;
    }

    private UserWalletTransaction buildTransaction(Long id, String title, BigDecimal amount, Integer status,
                                                   LocalDateTime createTime) {
        UserWalletTransaction transaction = new UserWalletTransaction();
        transaction.setId(id);
        transaction.setUserId(200L);
        transaction.setTitle(title);
        transaction.setAmount(amount);
        transaction.setDirection(UserWalletTransactionConstant.DIRECTION_INCOME);
        transaction.setStatus(status);
        transaction.setCreateTime(createTime);
        return transaction;
    }
}
