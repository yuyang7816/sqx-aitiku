package com.sqx.app.referral.members.support;

import com.sqx.app.base.user.domain.entity.UserEntity;
import com.sqx.app.referral.members.constants.ReferralMemberConstant;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * 推广下级成员展示字段转换（昵称/脱敏、注册日期、付费状态）
 */
public final class ReferralMemberDisplaySupport {

    private static final DateTimeFormatter REGISTER_DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private ReferralMemberDisplaySupport() {
    }

    public static String resolveDisplayName(UserEntity member) {
        if (member == null) {
            return "";
        }
        if (StringUtils.isNotBlank(member.getNickname())) {
            return member.getNickname().trim();
        }
        if (StringUtils.isNotBlank(member.getPhone()) && member.getPhone().length() >= 7) {
            String phone = member.getPhone().trim();
            return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
        }
        return maskUserName(member.getUserName());
    }

    public static String formatRegisterDate(LocalDateTime createTime) {
        if (createTime == null) {
            return "";
        }
        return REGISTER_DATE.format(createTime);
    }

    public static String resolvePayStatus(UserEntity member) {
        if (member != null && Objects.equals(member.getIsVip(), 1)) {
            return ReferralMemberConstant.STATUS_PAID;
        }
        return ReferralMemberConstant.STATUS_UNPAID;
    }

    private static String maskUserName(String userName) {
        if (StringUtils.isBlank(userName)) {
            return "用户";
        }
        String trimmed = userName.trim();
        if (trimmed.length() <= 1) {
            return trimmed;
        }
        if (trimmed.length() == 2) {
            return trimmed.charAt(0) + "*";
        }
        return trimmed.charAt(0) + "*" + trimmed.charAt(trimmed.length() - 1);
    }
}
