package com.sqx.app.referral.members.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sqx.app.base.user.db.service.IUserService;
import com.sqx.app.base.user.domain.entity.UserEntity;
import com.sqx.app.referral.commission.db.mapper.ReferralCommissionMapper;
import com.sqx.app.referral.members.constants.ReferralMemberConstant;
import com.sqx.app.referral.members.domain.vo.AppReferralMemberVO;
import com.sqx.app.referral.members.service.ReferralMembersService;
import com.sqx.app.referral.members.support.ReferralMemberDisplaySupport;
import com.sqx.common.core.exception.BizException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 推广下级成员列表
 */
@Service
public class ReferralMembersServiceImpl implements ReferralMembersService {

    @Resource
    private IUserService iUserService;
    @Resource
    private ReferralCommissionMapper referralCommissionMapper;

    @Override
    public List<AppReferralMemberVO> listMembers(Long userId) {
        return listMembers(userId, 1, false);
    }

    @Override
    public List<AppReferralMemberVO> listMembers(Long userId, int level, boolean withCommission) {
        UserEntity inviter = iUserService.getById(userId);
        if (Objects.isNull(inviter)) {
            throw new BizException("用户不存在");
        }
        if (StringUtils.isBlank(inviter.getInvitationCode())) {
            return Collections.emptyList();
        }

        int normalizedLevel = level == 2 ? 2 : 1;
        List<UserEntity> members = normalizedLevel == 1
                ? listLevel1Members(inviter.getInvitationCode().trim())
                : listLevel2Members(inviter.getInvitationCode().trim());

        return members.stream()
                .map(member -> toMemberVO(member, userId, withCommission))
                .collect(Collectors.toList());
    }

    private List<UserEntity> listLevel1Members(String invitationCode) {
        LambdaQueryWrapper<UserEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserEntity::getSuperiorInvitationCode, invitationCode)
                .orderByDesc(UserEntity::getCreateTime)
                .last("LIMIT " + ReferralMemberConstant.MAX_MEMBERS);
        return iUserService.list(wrapper);
    }

    private List<UserEntity> listLevel2Members(String invitationCode) {
        LambdaQueryWrapper<UserEntity> level1Wrapper = new LambdaQueryWrapper<>();
        level1Wrapper.eq(UserEntity::getSuperiorInvitationCode, invitationCode)
                .select(UserEntity::getInvitationCode);
        List<String> level1Codes = iUserService.list(level1Wrapper).stream()
                .map(UserEntity::getInvitationCode)
                .filter(StringUtils::isNotBlank)
                .map(String::trim)
                .distinct()
                .collect(Collectors.toList());
        if (level1Codes.isEmpty()) {
            return Collections.emptyList();
        }

        LambdaQueryWrapper<UserEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(UserEntity::getSuperiorInvitationCode, level1Codes)
                .orderByDesc(UserEntity::getCreateTime)
                .last("LIMIT " + ReferralMemberConstant.MAX_MEMBERS);
        return iUserService.list(wrapper);
    }

    private AppReferralMemberVO toMemberVO(UserEntity member, Long inviterUserId, boolean withCommission) {
        AppReferralMemberVO vo = new AppReferralMemberVO();
        vo.setName(ReferralMemberDisplaySupport.resolveDisplayName(member));
        vo.setAvatar(StringUtils.defaultString(member.getAvatar()).trim());
        vo.setTime(ReferralMemberDisplaySupport.formatRegisterDate(member.getCreateTime()));
        vo.setStatus(ReferralMemberDisplaySupport.resolvePayStatus(member));
        if (withCommission && member.getUserId() != null) {
            BigDecimal commission = referralCommissionMapper.sumMemberCommission(inviterUserId, member.getUserId());
            vo.setCommission(commission == null ? BigDecimal.ZERO : commission);
        }
        return vo;
    }
}
