package com.sqx.app.base.user.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sqx.app.base.enums.AppError;
import com.sqx.app.base.user.db.service.IUserService;
import com.sqx.app.base.user.delete.domain.vo.AppUserDeleteAccountVO;
import com.sqx.app.base.user.delete.service.UserAccountDeletionRequestService;
import com.sqx.app.base.user.domain.bo.DeleteUserBO;
import com.sqx.app.base.user.domain.bo.ForgetPasswordBO;
import com.sqx.app.base.user.domain.bo.ListUserBO;
import com.sqx.app.base.user.domain.bo.UpdateAppUserProfileBO;
import com.sqx.app.base.user.domain.bo.UpdateUserBO;
import com.sqx.app.base.user.domain.entity.UserEntity;
import com.sqx.app.base.user.domain.vo.AppUserProfileVO;
import com.sqx.app.base.user.domain.vo.UserVo;
import com.sqx.app.base.user.service.UserService;
import com.sqx.app.base.verifyCode.service.VerifyCodeService;
import com.sqx.app.vip.expire.service.VipExpireService;
import com.sqx.app.referral.withdraw.db.service.IWithdrawalAccountService;
import com.sqx.app.referral.withdraw.domain.entity.WithdrawalAccount;
import com.sqx.app.wallet.wallet.db.service.IUserWalletService;
import com.sqx.app.wallet.wallet.domain.entity.UserWallet;
import com.sqx.common.core.domain.Result;
import com.sqx.common.core.exception.BizException;
import com.sqx.common.mybatis.utils.PageUtil;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

/**
 * @author chengming
 * @since 2025/4/24
 */
@Service
public class UserServiceImpl implements UserService {

    @Resource
    private IUserService iUserService;
    @Resource
    private VerifyCodeService verifyCodeService;
    @Resource
    private VipExpireService vipExpireService;
    @Resource
    private UserAccountDeletionRequestService userAccountDeletionRequestService;
    @Resource
    private IUserWalletService iUserWalletService;
    @Resource
    private IWithdrawalAccountService iWithdrawalAccountService;

    @Override
    public Result<UserVo> info(Long userId) {
        UserEntity userEntity = vipExpireService.refreshIfExpired(userId);
        if (Objects.isNull(userEntity)) {
            throw new BizException(AppError.USER_NOT_FOUND);
        }

        UserVo vo = new UserVo();
        BeanUtils.copyProperties(userEntity, vo);
        attachWalletInfo(vo, userId);
        return Result.success(vo);
    }

    private void attachWalletInfo(UserVo vo, Long userId) {
        LambdaQueryWrapper<UserWallet> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserWallet::getUserId, userId);
        UserWallet wallet = iUserWalletService.getOne(queryWrapper);
        if (wallet != null) {
            vo.setWalletId(wallet.getId());
            vo.setWalletAmount(wallet.getAmount());
            return;
        }
        vo.setWalletAmount(BigDecimal.ZERO);
    }

    @Override
    public Result<AppUserProfileVO> getAppUserProfile(Long userId) {
        UserEntity userEntity = vipExpireService.refreshIfExpired(userId);
        if (Objects.isNull(userEntity)) {
            throw new BizException(AppError.USER_NOT_FOUND);
        }
        return Result.success(toAppUserProfileVO(userEntity));
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Result<AppUserProfileVO> updateAppUserProfile(Long userId, UpdateAppUserProfileBO updateAppUserProfileBO) {
        UserEntity userEntity = iUserService.getById(userId);
        if (Objects.isNull(userEntity)) {
            throw new BizException(AppError.USER_NOT_FOUND);
        }

        UserEntity updateEntity = new UserEntity();
        updateEntity.setUserId(userId);
        updateEntity.setGrade(updateAppUserProfileBO.getGrade());
        updateEntity.setGradeShort(gradeToShort(updateAppUserProfileBO.getGrade()));
        updateEntity.setTextbookVersion(updateAppUserProfileBO.getVersion());
        updateEntity.setSubjects(JSON.toJSONString(updateAppUserProfileBO.getSubjects()));
        updateEntity.setProfileReady(Boolean.TRUE.equals(updateAppUserProfileBO.getReady()) ? 1 : 0);
        if (StringUtils.isNotBlank(updateAppUserProfileBO.getNickname())) {
            updateEntity.setNickname(updateAppUserProfileBO.getNickname().trim());
        }
        if (StringUtils.isNotBlank(updateAppUserProfileBO.getAvatar())) {
            updateEntity.setAvatar(updateAppUserProfileBO.getAvatar().trim());
        }
        iUserService.updateById(updateEntity);

        UserEntity updated = iUserService.getById(userId);
        return Result.success(toAppUserProfileVO(updated));
    }

    @Override
    public Result<AppUserDeleteAccountVO> deleteAppAccount(Long userId) {
        return Result.success(userAccountDeletionRequestService.requestDeletion(userId));
    }


    /**
     * 实体转用户端资料 VO（不暴露密码、openId 等敏感字段）
     */
    AppUserProfileVO toAppUserProfileVO(UserEntity userEntity) {
        AppUserProfileVO vo = new AppUserProfileVO();
        vo.setUserId(userEntity.getUserId());
        vo.setPhone(userEntity.getPhone());
        vo.setAvatar(userEntity.getAvatar());
        vo.setNickname(resolveNickname(userEntity));
        vo.setGrade(userEntity.getGrade());
        vo.setGradeShort(resolveGradeShort(userEntity));
        vo.setVersion(normalizeVersion(userEntity.getTextbookVersion()));
        vo.setSubjects(parseSubjects(userEntity.getSubjects()));
        vo.setReady(isProfileReady(userEntity));
        vo.setVip(userEntity.getIsVip() != null && userEntity.getIsVip() == 1);
        vo.setVipExpireAt(formatVipExpireAt(userEntity.getVipExpireAt()));
        return vo;
    }

    String formatVipExpireAt(LocalDateTime expireAt) {
        if (expireAt == null) {
            return null;
        }
        return expireAt.toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    private String resolveNickname(UserEntity userEntity) {
        if (StringUtils.isNotBlank(userEntity.getNickname())) {
            return userEntity.getNickname();
        }
        return userEntity.getUserName();
    }

    private String resolveGradeShort(UserEntity userEntity) {
        if (StringUtils.isNotBlank(userEntity.getGradeShort())) {
            return userEntity.getGradeShort();
        }
        return gradeToShort(userEntity.getGrade());
    }

    private String gradeToShort(String grade) {
        if (StringUtils.isBlank(grade)) {
            return null;
        }
        if (grade.startsWith("小学")) {
            if (grade.contains("一")) {
                return "小一";
            }
            if (grade.contains("二")) {
                return "小二";
            }
            if (grade.contains("三")) {
                return "小三";
            }
            if (grade.contains("四")) {
                return "小四";
            }
            if (grade.contains("五")) {
                return "小五";
            }
            if (grade.contains("六")) {
                return "小六";
            }
            return grade.replace("小学", "小");
        }
        if (grade.startsWith("高中")) {
            if (grade.contains("一")) {
                return "高一";
            }
            if (grade.contains("二")) {
                return "高二";
            }
            if (grade.contains("三")) {
                return "高三";
            }
            return grade.replace("高中", "高");
        }
        if (grade.contains("三")) {
            return "初三";
        }
        if (grade.contains("二")) {
            return "初二";
        }
        if (grade.contains("一")) {
            return "初一";
        }
        return grade.replace("初中", "初");
    }

    private String normalizeVersion(String version) {
        if (StringUtils.isBlank(version)) {
            return null;
        }
        return version.replace("（默认）", "").trim();
    }

    private List<String> parseSubjects(String subjectsJson) {
        if (StringUtils.isBlank(subjectsJson)) {
            return Collections.emptyList();
        }
        return JSON.parseArray(subjectsJson, String.class);
    }

    private boolean isProfileReady(UserEntity userEntity) {
        return userEntity.getProfileReady() != null && userEntity.getProfileReady() == 1;
    }

    @Override
    public Result<Void> forgetPassword(ForgetPasswordBO forgetPasswordBO) {
        UserEntity userEntity = iUserService.getById(forgetPasswordBO.getUserId());
        if (Objects.isNull(userEntity)) {
            throw new BizException(AppError.USER_NOT_FOUND);
        }

        if (!verifyCodeService.verify(userEntity.getPhone(), forgetPasswordBO.getCode())) {
            throw new BizException(AppError.VERIFY_CODE_ERROR);
        }

        iUserService.update(new UserEntity(), new LambdaUpdateWrapper<UserEntity>()
                .eq(UserEntity::getUserId, forgetPasswordBO.getUserId())
                .set(UserEntity::getPassword, DigestUtils.md5Hex(forgetPasswordBO.getPassword()))
        );

        return Result.success();
    }

    @Override
    public Page<UserVo> list(ListUserBO listUserBO) {
        Page<UserEntity> entityPage = PageUtil.initPage(listUserBO.getPage(), listUserBO.getLimit());

        String superiorInvitationCode = StringUtils.trimToNull(listUserBO.getSuperiorInvitationCode());

        QueryWrapper<UserEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(StringUtils.isNotBlank(listUserBO.getPhone()), UserEntity::getPhone, listUserBO.getPhone())
                .eq(StringUtils.isNotBlank(listUserBO.getUsername()), UserEntity::getUserName, listUserBO.getUsername())
                .eq(Objects.nonNull(listUserBO.getStatus()), UserEntity::getStatus, listUserBO.getStatus())
                .eq(Objects.nonNull(listUserBO.getUserType()), UserEntity::getUserType, listUserBO.getUserType())
                .eq(StringUtils.isNotBlank(superiorInvitationCode),
                        UserEntity::getSuperiorInvitationCode, superiorInvitationCode)
                .eq(Objects.nonNull(listUserBO.getIsVip()), UserEntity::getIsVip, listUserBO.getIsVip())
                .orderByDesc(UserEntity::getCreateTime);

        Page<UserEntity> page = iUserService.page(entityPage, queryWrapper);

        Page<UserVo> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        if (CollectionUtils.isEmpty(page.getRecords())) {
            voPage.setRecords(Collections.emptyList());
            return voPage;
        }
        voPage.setRecords(enrichUserList(page.getRecords()));
        return voPage;
    }

    private List<UserVo> enrichUserList(List<UserEntity> records) {
        List<Long> userIds = records.stream()
                .map(UserEntity::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        Map<Long, BigDecimal> walletAmountMap = loadWalletAmountMap(userIds);
        Map<Long, String> withdrawalSummaryMap = loadWithdrawalSummaryMap(userIds);

        Set<String> superiorCodes = records.stream()
                .map(UserEntity::getSuperiorInvitationCode)
                .map(StringUtils::trimToNull)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<String, UserEntity> inviterMap = loadInviterMap(superiorCodes);

        Set<String> invitationCodes = records.stream()
                .map(UserEntity::getInvitationCode)
                .map(StringUtils::trimToNull)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<String, Long> directMemberCountMap = loadDirectMemberCountMap(invitationCodes);

        List<UserVo> voList = new ArrayList<>();
        for (UserEntity record : records) {
            UserVo vo = new UserVo();
            BeanUtils.copyProperties(record, vo);
            vo.setWalletAmount(walletAmountMap.getOrDefault(record.getUserId(), BigDecimal.ZERO));

            String superiorCode = StringUtils.trimToNull(record.getSuperiorInvitationCode());
            if (superiorCode != null) {
                UserEntity inviter = inviterMap.get(superiorCode);
                if (inviter != null) {
                    vo.setSuperiorUserId(inviter.getUserId());
                    vo.setSuperiorDisplayName(resolveDisplayName(inviter));
                }
            }

            vo.setWithdrawalAccountSummary(withdrawalSummaryMap.get(record.getUserId()));

            String invitationCode = StringUtils.trimToNull(record.getInvitationCode());
            if (invitationCode != null) {
                vo.setDirectMemberCount(directMemberCountMap.getOrDefault(invitationCode, 0L).intValue());
            } else {
                vo.setDirectMemberCount(0);
            }
            voList.add(vo);
        }
        return voList;
    }

    private Map<Long, BigDecimal> loadWalletAmountMap(List<Long> userIds) {
        if (CollectionUtils.isEmpty(userIds)) {
            return Collections.emptyMap();
        }
        List<UserWallet> wallets = iUserWalletService.list(new LambdaQueryWrapper<UserWallet>()
                .in(UserWallet::getUserId, userIds));
        if (CollectionUtils.isEmpty(wallets)) {
            return Collections.emptyMap();
        }
        Map<Long, BigDecimal> map = new HashMap<>();
        for (UserWallet wallet : wallets) {
            map.put(wallet.getUserId(), wallet.getAmount() != null ? wallet.getAmount() : BigDecimal.ZERO);
        }
        return map;
    }

    private Map<Long, String> loadWithdrawalSummaryMap(List<Long> userIds) {
        if (CollectionUtils.isEmpty(userIds)) {
            return Collections.emptyMap();
        }
        List<WithdrawalAccount> accounts = iWithdrawalAccountService.list(new LambdaQueryWrapper<WithdrawalAccount>()
                .in(WithdrawalAccount::getUserId, userIds)
                .orderByDesc(WithdrawalAccount::getCreateTime));
        if (CollectionUtils.isEmpty(accounts)) {
            return Collections.emptyMap();
        }
        Map<Long, List<WithdrawalAccount>> grouped = accounts.stream()
                .collect(Collectors.groupingBy(WithdrawalAccount::getUserId));
        Map<Long, String> summaryMap = new HashMap<>();
        grouped.forEach((userId, list) -> summaryMap.put(userId, buildWithdrawalSummary(list)));
        return summaryMap;
    }

    private String buildWithdrawalSummary(List<WithdrawalAccount> accounts) {
        return accounts.stream()
                .map(this::formatWithdrawalAccount)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.joining("；"));
    }

    private String formatWithdrawalAccount(WithdrawalAccount account) {
        if (account == null) {
            return "";
        }
        String typeLabel = Objects.equals(account.getType(), 2) ? "微信" : "支付宝";
        if (Objects.equals(account.getType(), 2) && StringUtils.isNotBlank(account.getAccountNumber())) {
            return typeLabel + "收款码";
        }
        String name = StringUtils.firstNonBlank(account.getAccountName(), account.getRealName());
        if (StringUtils.isNotBlank(name)) {
            return typeLabel + "·" + name;
        }
        return StringUtils.defaultString(account.getAccountNumber(), typeLabel);
    }

    private Map<String, UserEntity> loadInviterMap(Set<String> superiorCodes) {
        if (CollectionUtils.isEmpty(superiorCodes)) {
            return Collections.emptyMap();
        }
        List<UserEntity> inviters = iUserService.list(new LambdaQueryWrapper<UserEntity>()
                .in(UserEntity::getInvitationCode, superiorCodes));
        if (CollectionUtils.isEmpty(inviters)) {
            return Collections.emptyMap();
        }
        Map<String, UserEntity> map = new HashMap<>();
        for (UserEntity inviter : inviters) {
            String code = StringUtils.trimToNull(inviter.getInvitationCode());
            if (code != null) {
                map.putIfAbsent(code, inviter);
            }
        }
        return map;
    }

    private Map<String, Long> loadDirectMemberCountMap(Set<String> invitationCodes) {
        if (CollectionUtils.isEmpty(invitationCodes)) {
            return Collections.emptyMap();
        }
        List<UserEntity> members = iUserService.list(new LambdaQueryWrapper<UserEntity>()
                .select(UserEntity::getSuperiorInvitationCode)
                .in(UserEntity::getSuperiorInvitationCode, invitationCodes));
        if (CollectionUtils.isEmpty(members)) {
            return Collections.emptyMap();
        }
        return members.stream()
                .map(item -> StringUtils.trimToNull(item.getSuperiorInvitationCode()))
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(code -> code, Collectors.counting()));
    }

    private String resolveDisplayName(UserEntity user) {
        if (user == null) {
            return null;
        }
        if (StringUtils.isNotBlank(user.getNickname())) {
            return user.getNickname();
        }
        if (StringUtils.isNotBlank(user.getUserName())) {
            return user.getUserName();
        }
        return user.getUserId() != null ? "用户" + user.getUserId() : null;
    }

    @Override
    public void update(UpdateUserBO updateUserBO) {
        UserEntity existing = iUserService.getById(updateUserBO.getUserId());
        if (Objects.isNull(existing)) {
            throw new BizException(AppError.USER_NOT_FOUND);
        }

        UserEntity userEntity = new UserEntity();
        BeanUtils.copyProperties(updateUserBO, userEntity, "subjects");

        if (StringUtils.isNotBlank(updateUserBO.getGrade())) {
            userEntity.setGrade(updateUserBO.getGrade().trim());
            userEntity.setGradeShort(StringUtils.isNotBlank(updateUserBO.getGradeShort())
                    ? updateUserBO.getGradeShort().trim()
                    : gradeToShort(updateUserBO.getGrade()));
        }
        if (StringUtils.isNotBlank(updateUserBO.getTextbookVersion())) {
            userEntity.setTextbookVersion(updateUserBO.getTextbookVersion().trim());
        }
        if (updateUserBO.getSubjects() != null) {
            userEntity.setSubjects(JSON.toJSONString(updateUserBO.getSubjects()));
        }
        if (updateUserBO.getIsVip() != null) {
            userEntity.setUserType(updateUserBO.getIsVip() == 1 ? 2 : 1);
        }
        userEntity.setCommissionRateLevel1(normalizeCommissionRate(updateUserBO.getCommissionRateLevel1()));
        userEntity.setCommissionRateLevel2(normalizeCommissionRate(updateUserBO.getCommissionRateLevel2()));

        iUserService.updateById(userEntity);
    }

    private BigDecimal normalizeCommissionRate(BigDecimal rate) {
        if (rate == null) {
            return null;
        }
        if (rate.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }
        return rate;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(DeleteUserBO deleteUserBO) {
        iUserService.removeById(deleteUserBO.getUserId());

        // 其他需要删除的信息
    }
}
