package com.sqx.app.referral.invitecode.support;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.sqx.app.base.user.db.service.IUserService;
import com.sqx.app.base.user.domain.entity.UserEntity;
import com.sqx.app.referral.invitecode.constants.ReferralInviteCodeConstant;
import com.sqx.common.core.exception.BizException;
import com.sqx.common.core.utils.GenUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 确保用户拥有全局唯一邀请码（懒生成并持久化至 tb_user.invitation_code）
 */
@Component
public class InviteCodeSupport {

    @Resource
    private IUserService iUserService;

    /**
     * 返回用户邀请码；若为空则生成唯一码并写库
     */
    public String ensureUniqueInviteCode(UserEntity user) {
        if (StringUtils.isNotBlank(user.getInvitationCode())) {
            return user.getInvitationCode().trim();
        }

        String inviteCode = generateUniqueInviteCode(user);

        UserEntity update = new UserEntity();
        update.setUserId(user.getUserId());
        update.setInvitationCode(inviteCode);
        iUserService.updateById(update);
        user.setInvitationCode(inviteCode);
        return inviteCode;
    }

    private String generateUniqueInviteCode(UserEntity user) {
        String seed = buildInviteSeed(user);
        for (int attempt = 0; attempt < ReferralInviteCodeConstant.INVITE_CODE_MAX_RETRY; attempt++) {
            String candidate = GenUtil.genInviteCode(seed + attempt);
            if (!invitationCodeExists(candidate)) {
                return candidate;
            }
        }
        throw new BizException("邀请码生成失败，请稍后重试");
    }

    /**
     * 优先手机号，其次 openId，最后 userId（与注册逻辑一致）
     */
    String buildInviteSeed(UserEntity user) {
        if (StringUtils.isNotBlank(user.getPhone())) {
            return user.getPhone().trim();
        }
        if (StringUtils.isNotBlank(user.getMaOpenId())) {
            long openIdSeed = Math.abs((long) user.getMaOpenId().hashCode());
            return "1" + String.format("%010d", openIdSeed % 10_000_000_000L);
        }
        return "9" + String.format("%010d", user.getUserId() % 10_000_000_000L);
    }

    private boolean invitationCodeExists(String invitationCode) {
        QueryWrapper<UserEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(UserEntity::getInvitationCode, invitationCode);
        return iUserService.exists(queryWrapper);
    }
}
