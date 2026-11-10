package com.sqx.app.base.user.delete.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.sqx.app.base.enums.AppError;
import com.sqx.app.base.messages.db.service.IMessagesService;
import com.sqx.app.base.user.db.service.IUserService;
import com.sqx.app.base.user.delete.support.UserDeletedSupport;
import com.sqx.app.base.user.domain.entity.UserEntity;
import com.sqx.app.base.user.enums.UserStatus;
import com.sqx.app.referral.commission.db.service.IReferralCommissionService;
import com.sqx.app.referral.user.db.service.IReferralUserCommissionService;
import com.sqx.app.referral.withdraw.db.service.IWithdrawRecordService;
import com.sqx.app.referral.withdraw.db.service.IWithdrawalAccountService;
import com.sqx.app.vip.pay.db.service.IVipOrderService;
import com.sqx.app.wallet.transaction.db.service.IUserWalletTransactionService;
import com.sqx.app.wallet.wallet.db.service.IUserWalletService;
import com.sqx.common.core.exception.BizException;
import com.sqx.system.db.service.IUserPracticeDraftService;
import com.sqx.system.db.service.IUserPracticeRecordService;
import com.sqx.system.db.service.IUserQuestionAnswerService;
import com.sqx.system.db.service.IUserQuestionCollectionService;
import com.sqx.system.db.service.IUserQuestionWrongService;
import com.sqx.system.db.service.IUserSearchRecordService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * POST /app/user/delete 账号注销单元测试
 */
@ExtendWith(MockitoExtension.class)
class UserAccountDeletionServiceImplTest {

    private static final Long USER_ID = 100L;

    @Mock
    private IUserService iUserService;
    @Mock
    private IUserQuestionAnswerService iUserQuestionAnswerService;
    @Mock
    private IUserQuestionWrongService iUserQuestionWrongService;
    @Mock
    private IUserPracticeRecordService iUserPracticeRecordService;
    @Mock
    private IUserPracticeDraftService iUserPracticeDraftService;
    @Mock
    private IUserQuestionCollectionService iUserQuestionCollectionService;
    @Mock
    private IUserSearchRecordService iUserSearchRecordService;
    @Mock
    private IMessagesService iMessagesService;
    @Mock
    private IUserWalletTransactionService iUserWalletTransactionService;
    @Mock
    private IWithdrawRecordService iWithdrawRecordService;
    @Mock
    private IWithdrawalAccountService iWithdrawalAccountService;
    @Mock
    private IReferralCommissionService iReferralCommissionService;
    @Mock
    private IReferralUserCommissionService iReferralUserCommissionService;
    @Mock
    private IUserWalletService iUserWalletService;
    @Mock
    private IVipOrderService iVipOrderService;

    @InjectMocks
    private UserAccountDeletionServiceImpl userAccountDeletionService;

    @Test
    void deleteAccount_whenUserNotFound_shouldThrow() {
        when(iUserService.getById(USER_ID)).thenReturn(null);

        BizException ex = assertThrows(BizException.class, () -> userAccountDeletionService.deleteAccount(USER_ID));
        assertEquals(AppError.USER_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void deleteAccount_whenAlreadyDeleted_shouldThrow() {
        UserEntity deleted = buildActiveUser();
        deleted.setStatus(UserStatus.DISABLE.getCode());
        deleted.setPhone(UserDeletedSupport.DELETED_PHONE_PREFIX + USER_ID + "_123");
        when(iUserService.getById(USER_ID)).thenReturn(deleted);

        BizException ex = assertThrows(BizException.class, () -> userAccountDeletionService.deleteAccount(USER_ID));
        assertEquals(AppError.USER_ACCOUNT_DELETED, ex.getErrorCode());
    }

    @Test
    void deleteAccount_whenActiveUser_shouldPurgeDataAndAnonymize() {
        when(iUserService.getById(USER_ID)).thenReturn(buildActiveUser());
        when(iUserService.update(any(UserEntity.class), any(Wrapper.class))).thenReturn(true);

        userAccountDeletionService.deleteAccount(USER_ID);

        verify(iUserQuestionAnswerService, times(1)).remove(any(Wrapper.class));
        verify(iUserQuestionWrongService, times(1)).remove(any(Wrapper.class));
        verify(iUserPracticeRecordService, times(1)).remove(any(Wrapper.class));
        verify(iUserPracticeDraftService, times(1)).remove(any(Wrapper.class));
        verify(iUserQuestionCollectionService, times(1)).remove(any(Wrapper.class));
        verify(iUserSearchRecordService, times(1)).remove(any(Wrapper.class));
        verify(iMessagesService, times(1)).remove(any(Wrapper.class));
        verify(iUserWalletTransactionService, times(1)).remove(any(Wrapper.class));
        verify(iWithdrawRecordService, times(1)).remove(any(Wrapper.class));
        verify(iWithdrawalAccountService, times(1)).remove(any(Wrapper.class));
        verify(iReferralCommissionService, times(1)).remove(any(Wrapper.class));
        verify(iReferralUserCommissionService, times(1)).remove(any(Wrapper.class));
        verify(iUserWalletService, times(1)).remove(any(Wrapper.class));
        verify(iVipOrderService, times(1)).remove(any(Wrapper.class));
        verify(iUserService, times(1)).update(any(UserEntity.class), any(Wrapper.class));
    }

    @Test
    void isDeleted_whenDisabledWithDeletedPhonePrefix_shouldReturnTrue() {
        UserEntity user = new UserEntity();
        user.setStatus(UserStatus.DISABLE.getCode());
        user.setPhone(UserDeletedSupport.buildDeletedPhone(USER_ID));

        assertTrue(UserDeletedSupport.isDeleted(user));
    }

    private UserEntity buildActiveUser() {
        UserEntity user = new UserEntity();
        user.setUserId(USER_ID);
        user.setPhone("13800138000");
        user.setStatus(UserStatus.ENABLE.getCode());
        return user;
    }
}
