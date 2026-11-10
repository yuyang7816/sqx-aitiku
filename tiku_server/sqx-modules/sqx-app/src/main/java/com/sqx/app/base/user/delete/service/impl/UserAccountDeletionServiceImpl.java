package com.sqx.app.base.user.delete.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.sqx.app.base.enums.AppError;
import com.sqx.app.base.messages.db.service.IMessagesService;
import com.sqx.app.base.messages.domain.entity.Messages;
import com.sqx.app.base.user.db.service.IUserService;
import com.sqx.app.base.user.delete.service.UserAccountDeletionService;
import com.sqx.app.base.user.delete.support.UserDeletedSupport;
import com.sqx.app.base.user.domain.entity.UserEntity;
import com.sqx.app.base.user.enums.UserStatus;
import com.sqx.app.referral.commission.db.service.IReferralCommissionService;
import com.sqx.app.referral.commission.domain.entity.ReferralCommission;
import com.sqx.app.referral.user.db.service.IReferralUserCommissionService;
import com.sqx.app.referral.user.domain.entity.ReferralUserCommission;
import com.sqx.app.referral.withdraw.db.service.IWithdrawRecordService;
import com.sqx.app.referral.withdraw.db.service.IWithdrawalAccountService;
import com.sqx.app.referral.withdraw.domain.entity.WithdrawRecord;
import com.sqx.app.referral.withdraw.domain.entity.WithdrawalAccount;
import com.sqx.app.vip.pay.db.service.IVipOrderService;
import com.sqx.app.vip.pay.domain.entity.VipOrder;
import com.sqx.app.wallet.transaction.db.service.IUserWalletTransactionService;
import com.sqx.app.wallet.transaction.domain.entity.UserWalletTransaction;
import com.sqx.app.wallet.wallet.db.service.IUserWalletService;
import com.sqx.app.wallet.wallet.domain.entity.UserWallet;
import com.sqx.common.core.exception.BizException;
import com.sqx.system.db.entity.UserPracticeDraft;
import com.sqx.system.db.entity.UserPracticeRecord;
import com.sqx.system.db.entity.UserQuestionAnswer;
import com.sqx.system.db.entity.UserQuestionCollection;
import com.sqx.system.db.entity.UserQuestionWrong;
import com.sqx.system.db.entity.UserSearchRecord;
import com.sqx.system.db.entity.UserStudyCheckIn;
import com.sqx.system.db.service.IUserPracticeDraftService;
import com.sqx.system.db.service.IUserPracticeRecordService;
import com.sqx.system.db.service.IUserQuestionAnswerService;
import com.sqx.system.db.service.IUserQuestionCollectionService;
import com.sqx.system.db.service.IUserQuestionWrongService;
import com.sqx.system.db.service.IUserSearchRecordService;
import com.sqx.system.db.service.IUserStudyCheckInService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * 用户端账号注销：清除业务数据并匿名化用户主表
 */
@Service
public class UserAccountDeletionServiceImpl implements UserAccountDeletionService {

    @Resource
    private IUserService iUserService;
    @Resource
    private IUserQuestionAnswerService iUserQuestionAnswerService;
    @Resource
    private IUserQuestionWrongService iUserQuestionWrongService;
    @Resource
    private IUserPracticeRecordService iUserPracticeRecordService;
    @Resource
    private IUserPracticeDraftService iUserPracticeDraftService;
    @Resource
    private IUserQuestionCollectionService iUserQuestionCollectionService;
    @Resource
    private IUserSearchRecordService iUserSearchRecordService;
    @Resource
    private IUserStudyCheckInService iUserStudyCheckInService;
    @Resource
    private IMessagesService iMessagesService;
    @Resource
    private IUserWalletTransactionService iUserWalletTransactionService;
    @Resource
    private IWithdrawRecordService iWithdrawRecordService;
    @Resource
    private IWithdrawalAccountService iWithdrawalAccountService;
    @Resource
    private IReferralCommissionService iReferralCommissionService;
    @Resource
    private IReferralUserCommissionService iReferralUserCommissionService;
    @Resource
    private IUserWalletService iUserWalletService;
    @Resource
    private IVipOrderService iVipOrderService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteAccount(Long userId) {
        UserEntity userEntity = iUserService.getById(userId);
        if (Objects.isNull(userEntity)) {
            throw new BizException(AppError.USER_NOT_FOUND);
        }
        if (UserDeletedSupport.isDeleted(userEntity)) {
            throw new BizException(AppError.USER_ACCOUNT_DELETED);
        }

        purgeUserBusinessData(userId);
        anonymizeUserAccount(userId);
    }

    private void purgeUserBusinessData(Long userId) {
        iUserQuestionAnswerService.remove(new LambdaQueryWrapper<UserQuestionAnswer>()
                .eq(UserQuestionAnswer::getUserId, userId));
        iUserQuestionWrongService.remove(new LambdaQueryWrapper<UserQuestionWrong>()
                .eq(UserQuestionWrong::getUserId, userId));
        iUserPracticeRecordService.remove(new LambdaQueryWrapper<UserPracticeRecord>()
                .eq(UserPracticeRecord::getUserId, userId));
        iUserPracticeDraftService.remove(new LambdaQueryWrapper<UserPracticeDraft>()
                .eq(UserPracticeDraft::getUserId, userId));
        iUserQuestionCollectionService.remove(new LambdaQueryWrapper<UserQuestionCollection>()
                .eq(UserQuestionCollection::getUserId, userId));
        iUserSearchRecordService.remove(new LambdaQueryWrapper<UserSearchRecord>()
                .eq(UserSearchRecord::getUserId, userId));
        iUserStudyCheckInService.remove(new LambdaQueryWrapper<UserStudyCheckIn>()
                .eq(UserStudyCheckIn::getUserId, userId));
        iMessagesService.remove(new LambdaQueryWrapper<Messages>()
                .eq(Messages::getUserId, userId));
        iUserWalletTransactionService.remove(new LambdaQueryWrapper<UserWalletTransaction>()
                .eq(UserWalletTransaction::getUserId, userId));
        iWithdrawRecordService.remove(new LambdaQueryWrapper<WithdrawRecord>()
                .eq(WithdrawRecord::getUserId, userId));
        iWithdrawalAccountService.remove(new LambdaQueryWrapper<WithdrawalAccount>()
                .eq(WithdrawalAccount::getUserId, userId));
        iReferralCommissionService.remove(new LambdaQueryWrapper<ReferralCommission>()
                .eq(ReferralCommission::getUserId, userId));
        iReferralUserCommissionService.remove(new LambdaQueryWrapper<ReferralUserCommission>()
                .eq(ReferralUserCommission::getUserId, userId));
        iUserWalletService.remove(new LambdaQueryWrapper<UserWallet>()
                .eq(UserWallet::getUserId, userId));
        iVipOrderService.remove(new LambdaQueryWrapper<VipOrder>()
                .eq(VipOrder::getUserId, userId));
    }

    private void anonymizeUserAccount(Long userId) {
        iUserService.update(new UserEntity(), new LambdaUpdateWrapper<UserEntity>()
                .eq(UserEntity::getUserId, userId)
                .set(UserEntity::getPhone, UserDeletedSupport.buildDeletedPhone(userId))
                .set(UserEntity::getUserName, "已注销用户")
                .set(UserEntity::getNickname, null)
                .set(UserEntity::getAvatar, null)
                .set(UserEntity::getSex, null)
                .set(UserEntity::getAge, null)
                .set(UserEntity::getMaOpenId, null)
                .set(UserEntity::getMpOpenId, null)
                .set(UserEntity::getAppleId, null)
                .set(UserEntity::getPassword, null)
                .set(UserEntity::getGrade, null)
                .set(UserEntity::getGradeShort, null)
                .set(UserEntity::getTextbookVersion, null)
                .set(UserEntity::getSubjects, null)
                .set(UserEntity::getProfileReady, 0)
                .set(UserEntity::getIsVip, 0)
                .set(UserEntity::getVipExpireAt, null)
                .set(UserEntity::getOcrUsedToday, 0)
                .set(UserEntity::getOcrQuotaDate, null)
                .set(UserEntity::getAiUsedToday, 0)
                .set(UserEntity::getAiQuotaDate, null)
                .set(UserEntity::getInvitationCode, null)
                .set(UserEntity::getSuperiorInvitationCode, null)
                .set(UserEntity::getCid, null)
                .set(UserEntity::getStatus, UserStatus.DISABLE.getCode())
        );
    }
}
