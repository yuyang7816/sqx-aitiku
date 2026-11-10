package com.sqx.app.vip.rules.support;

import com.sqx.app.base.user.domain.entity.UserEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 文档 23.3 会员规则：免费 5 次/日、VIP 无限、expireAt 计算、开通重置次数（见 docs/member-rules-api.md）
 */
@Component
public class MemberRulesSupport {

    /** tb_user.is_vip=1 表示有效会员（须配合 vip_expire_at 未过期） */
    public static final int VIP_FLAG = 1;

    /** 开通 VIP 后 user_type */
    public static final int USER_TYPE_VIP = 2;

    /** 非会员 user_type */
    public static final int USER_TYPE_NORMAL = 1;

    /** VIP 展示等级（uniapp 首页 Hero 等文案） */
    public static final String VIP_LEVEL_LABEL = "AI至尊会员";

    /** sys_config 默认每日次数（ai_daily_limit / ocr_daily_limit） */
    public static final int DEFAULT_DAILY_LIMIT = 5;

    /**
     * 规则 23.3-2：是否会员（is_vip=1；过期降级由 VipExpireService 处理后再判断）
     */
    public boolean isVip(UserEntity user) {
        return user != null && user.getIsVip() != null && user.getIsVip() == VIP_FLAG;
    }

    /**
     * 规则 23.3-1/2：VIP AI/OCR 无限次，非会员按 dailyLimit 限制
     */
    public boolean isQuotaUnlimited(UserEntity user) {
        return isVip(user);
    }

    /**
     * 规则 23.3-2：会员等级文案
     */
    public String resolveMemberLevel(UserEntity user) {
        return isVip(user) ? VIP_LEVEL_LABEL : "普通会员";
    }

    /**
     * 规则 23.3-1：AI 今日已用次数；quota_date 非今日视为 0（跨日重置）
     */
    public int resolveAiUsedToday(UserEntity user, LocalDate today) {
        if (user == null) {
            return 0;
        }
        return resolveUsedToday(user.getAiQuotaDate(), user.getAiUsedToday(), today);
    }

    /**
     * 规则 23.3-1：OCR 今日已用次数；quota_date 非今日视为 0（跨日重置）
     */
    public int resolveOcrUsedToday(UserEntity user, LocalDate today) {
        if (user == null) {
            return 0;
        }
        return resolveUsedToday(user.getOcrQuotaDate(), user.getOcrUsedToday(), today);
    }

    /**
     * 跨日重置：配额日期不是 today 则视为 0 次
     */
    int resolveUsedToday(LocalDate quotaDate, Integer usedToday, LocalDate today) {
        if (today == null || quotaDate == null || !quotaDate.equals(today)) {
            return 0;
        }
        return usedToday == null ? 0 : usedToday;
    }

    /**
     * 非会员是否仍可消耗 AI 配额（已用 &lt; dailyLimit）
     */
    public boolean canConsumeAiQuota(UserEntity user, int dailyLimit, LocalDate today) {
        if (isQuotaUnlimited(user)) {
            return true;
        }
        return resolveAiUsedToday(user, today) < dailyLimit;
    }

    /**
     * 非会员是否仍可消耗 OCR 配额（已用 &lt; dailyLimit）
     */
    public boolean canConsumeOcrQuota(UserEntity user, int dailyLimit, LocalDate today) {
        if (isQuotaUnlimited(user)) {
            return true;
        }
        return resolveOcrUsedToday(user, today) < dailyLimit;
    }

    /**
     * 规则 23.3-3：开通/续费 expireAt
     * <ul>
     *   <li>无有效会员或已过期：now + planDays</li>
     *   <li>会员未过期续费：原 vipExpireAt + planDays（叠加剩余天数）</li>
     * </ul>
     */
    public LocalDateTime calculateVipExpireAt(UserEntity user, int planDays, LocalDateTime now) {
        if (now == null) {
            now = LocalDateTime.now();
        }
        LocalDateTime baseExpire = user == null ? null : user.getVipExpireAt();
        LocalDateTime startAt = (baseExpire != null && baseExpire.isAfter(now)) ? baseExpire : now;
        return startAt.plusDays(planDays);
    }

    /**
     * 规则 23.3-4：开通/续费当日重置 AI/OCR 已用次数为 0，配额日期设为 today
     */
    public ActivationQuotaReset buildActivationQuotaReset(LocalDate today) {
        ActivationQuotaReset reset = new ActivationQuotaReset();
        reset.setAiUsedToday(0);
        reset.setOcrUsedToday(0);
        reset.setAiQuotaDate(today);
        reset.setOcrQuotaDate(today);
        return reset;
    }
}
