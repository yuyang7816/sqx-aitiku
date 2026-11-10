package com.sqx.app.base.ocr.service.impl;

import com.sqx.app.base.enums.AppError;
import com.sqx.app.base.ocr.domain.vo.AppOcrQuotaVO;
import com.sqx.app.base.ocr.enums.OcrError;
import com.sqx.app.base.ocr.service.OcrQuotaService;
import com.sqx.app.base.ocr.support.OcrConfigReader;
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
 * OCR 每日次数：非会员按 sys_config.ocr_daily_limit 限制，VIP 无限；跨日自动重置 ocr_used_today（文档 23.3，见 MemberRulesSupport）
 */
@Service
public class OcrQuotaServiceImpl implements OcrQuotaService {

    @Resource
    private IUserService iUserService;

    @Resource
    private OcrConfigReader ocrConfigReader;

    @Resource
    private VipExpireService vipExpireService;

    @Resource
    private MemberRulesSupport memberRulesSupport;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public AppOcrQuotaVO consumeSearchQuota(Long userId) {
        UserEntity user = vipExpireService.refreshIfExpired(userId);
        if (Objects.isNull(user)) {
            throw new BizException(AppError.USER_NOT_FOUND);
        }

        int dailyLimit = ocrConfigReader.getDailyLimit();
        AppOcrQuotaVO quota = new AppOcrQuotaVO();
        quota.setOcrDailyLimit(dailyLimit);

        LocalDate today = LocalDate.now();
        if (memberRulesSupport.isQuotaUnlimited(user)) {
            quota.setOcrUsedToday(memberRulesSupport.resolveOcrUsedToday(user, today));
            quota.setOcrUnlimited(true);
            return quota;
        }

        int usedToday = memberRulesSupport.resolveOcrUsedToday(user, today);
        if (!memberRulesSupport.canConsumeOcrQuota(user, dailyLimit, today)) {
            throw new BizException(OcrError.QUOTA_EXCEEDED);
        }

        int nextUsed = usedToday + 1;
        UserEntity update = new UserEntity();
        update.setUserId(userId);
        update.setOcrUsedToday(nextUsed);
        update.setOcrQuotaDate(today);
        iUserService.updateById(update);

        quota.setOcrUsedToday(nextUsed);
        quota.setOcrUnlimited(false);
        return quota;
    }
}
