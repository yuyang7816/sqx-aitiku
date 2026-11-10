package com.sqx.app.base.ai.service.impl;

import com.sqx.app.base.ai.domain.vo.AppAiQuotaVO;
import com.sqx.app.base.ai.enums.AiError;
import com.sqx.app.base.ai.service.AiQuotaService;
import com.sqx.app.base.ai.support.AiConfigReader;
import com.sqx.app.base.enums.AppError;
import com.sqx.app.base.user.db.service.IUserService;
import com.sqx.app.base.user.domain.entity.UserEntity;
import com.sqx.app.vip.expire.service.VipExpireService;
import com.sqx.app.vip.rules.support.MemberRulesSupport;
import com.sqx.common.core.exception.BizException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.Objects;

/**
 * AI 每日次数：非会员按 sys_config.ai_daily_limit 限制，VIP 无限；跨日自动重置 ai_used_today（文档 23.3，见 MemberRulesSupport）
 */
@Service
public class AiQuotaServiceImpl implements AiQuotaService {

    @Resource
    private IUserService iUserService;

    @Resource
    private AiConfigReader aiConfigReader;

    @Resource
    private VipExpireService vipExpireService;

    @Resource
    private MemberRulesSupport memberRulesSupport;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public AppAiQuotaVO consumeChatQuota(Long userId) {
        UserEntity user = vipExpireService.refreshIfExpired(userId);
        if (Objects.isNull(user)) {
            throw new BizException(AppError.USER_NOT_FOUND);
        }

        int dailyLimit = aiConfigReader.getDailyLimit();
        AppAiQuotaVO quota = new AppAiQuotaVO();
        quota.setAiDailyLimit(dailyLimit);

        LocalDate today = LocalDate.now();
        if (memberRulesSupport.isQuotaUnlimited(user)) {
            quota.setAiUsedToday(memberRulesSupport.resolveAiUsedToday(user, today));
            quota.setAiUnlimited(true);
            return quota;
        }

        int usedToday = memberRulesSupport.resolveAiUsedToday(user, today);
        if (!memberRulesSupport.canConsumeAiQuota(user, dailyLimit, today)) {
            throw new BizException(AiError.QUOTA_EXCEEDED);
        }

        int nextUsed = usedToday + 1;
        UserEntity update = new UserEntity();
        update.setUserId(userId);
        update.setAiUsedToday(nextUsed);
        update.setAiQuotaDate(today);
        iUserService.updateById(update);

        quota.setAiUsedToday(nextUsed);
        quota.setAiUnlimited(false);
        return quota;
    }
}
