package com.sqx.app.base.subscribe.service.impl;

import com.sqx.app.base.enums.AppError;
import com.sqx.app.base.enums.ClientEnum;
import com.sqx.app.base.notification.core.enums.NotificationChannel;
import com.sqx.app.base.notification.core.model.NotificationRequest;
import com.sqx.app.base.notification.core.service.INotificationService;
import com.sqx.app.base.subscribe.constants.SubscribeMessageScene;
import com.sqx.app.base.subscribe.domain.bo.SubscribeRegisterBO;
import com.sqx.app.base.subscribe.domain.vo.SubscribeRegisterVO;
import com.sqx.app.base.subscribe.domain.vo.SubscribeTemplateVO;
import com.sqx.app.base.subscribe.domain.vo.SubscribeTestSendVO;
import com.sqx.app.base.subscribe.service.SubscribePushService;
import com.sqx.app.base.subscribe.support.SubscribeMessageBuilder;
import com.sqx.app.base.subscribe.support.SubscribeMessageConfigReader;
import com.sqx.app.base.user.db.service.IUserService;
import com.sqx.app.base.user.domain.entity.UserEntity;
import com.sqx.app.base.utils.wx.WxMaSubscribeMessage;
import com.sqx.common.core.exception.BizException;
import com.sqx.common.redis.utils.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SubscribePushServiceImpl implements SubscribePushService {

    private static final DateTimeFormatter REPORT_MONTH_QUERY =
            DateTimeFormatter.ofPattern("yyyy-MM");
    private static final String SUBSCRIBE_PREF_KEY_PREFIX = "subscribe:pref:";

    @Resource
    private SubscribeMessageConfigReader subscribeMessageConfigReader;
    @Resource
    private SubscribeMessageBuilder subscribeMessageBuilder;
    @Resource
    private INotificationService notificationService;
    @Resource
    private IUserService iUserService;

    @Override
    public SubscribeTemplateVO getTemplates() {
        SubscribeTemplateVO vo = new SubscribeTemplateVO();
        vo.setMonthlyReportTemplateId(subscribeMessageConfigReader.getTemplateId(SubscribeMessageScene.MONTHLY_REPORT));
        vo.setVipExpireTemplateId(subscribeMessageConfigReader.getTemplateId(SubscribeMessageScene.VIP_EXPIRE));
        vo.setWrongReviewTemplateId(subscribeMessageConfigReader.getTemplateId(SubscribeMessageScene.WRONG_REVIEW));
        return vo;
    }

    @Override
    public SubscribeTestSendVO sendTest(Long userId, String sceneCode) {
        SubscribeMessageScene scene = SubscribeMessageScene.fromCode(sceneCode);
        if (scene == null) {
            throw new BizException(AppError.UNSUPPORTED_CODE);
        }
        String templateId = subscribeMessageConfigReader.getTemplateId(scene);
        if (StringUtils.isBlank(templateId)) {
            throw new BizException(AppError.SUBSCRIBE_TEMPLATE_NOT_CONFIGURED);
        }
        UserEntity user = requireUserWithOpenId(userId);

        WxMaSubscribeMessage message = subscribeMessageBuilder.buildTestMessage(user.getMaOpenId(), templateId, scene);
        dispatch(userId, message);

        SubscribeTestSendVO vo = new SubscribeTestSendVO();
        vo.setScene(scene.code());
        vo.setSent(Boolean.TRUE);
        vo.setMessage("已向微信发送测试订阅消息");
        return vo;
    }

    @Override
    public SubscribeRegisterVO registerPreferences(Long userId, SubscribeRegisterBO body) {
        if (userId == null) {
            throw new BizException(AppError.USER_NOT_FOUND);
        }
        List<String> scenes = normalizeAcceptedScenes(body.getAcceptedScenes());
        String cacheKey = SUBSCRIBE_PREF_KEY_PREFIX + userId;
        if (Boolean.TRUE.equals(body.getPushEnabled())) {
            RedisUtil.setCacheObject(cacheKey, String.join(",", scenes));
        } else {
            RedisUtil.deleteObject(cacheKey);
        }

        SubscribeRegisterVO vo = new SubscribeRegisterVO();
        vo.setRegistered(Boolean.TRUE.equals(body.getPushEnabled()));
        vo.setAcceptedScenes(scenes);
        log.info("登记订阅消息偏好 userId={} pushEnabled={} scenes={}", userId, body.getPushEnabled(), scenes);
        return vo;
    }

    List<String> normalizeAcceptedScenes(List<String> scenes) {
        if (scenes == null || scenes.isEmpty()) {
            return Collections.emptyList();
        }
        Set<String> allowed = new LinkedHashSet<>();
        for (SubscribeMessageScene scene : SubscribeMessageScene.values()) {
            allowed.add(scene.code());
        }
        return scenes.stream()
                .map(StringUtils::trim)
                .filter(StringUtils::isNotBlank)
                .filter(allowed::contains)
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public boolean sendMonthlyReport(Long userId, YearMonth month, String title, String summary) {
        if (userId == null || month == null) {
            return false;
        }
        return sendIfConfigured(SubscribeMessageScene.MONTHLY_REPORT, userId, () -> {
            UserEntity user = iUserService.getById(userId);
            if (user == null || StringUtils.isBlank(user.getMaOpenId())) {
                return null;
            }
            String templateId = subscribeMessageConfigReader.getTemplateId(SubscribeMessageScene.MONTHLY_REPORT);
            return subscribeMessageBuilder.buildMonthlyReport(
                    user.getMaOpenId(),
                    templateId,
                    title,
                    month,
                    summary,
                    month.format(REPORT_MONTH_QUERY));
        });
    }

    @Override
    public boolean sendVipExpire(Long userId, String title, LocalDateTime expireAt, String remark) {
        if (userId == null) {
            return false;
        }
        return sendIfConfigured(SubscribeMessageScene.VIP_EXPIRE, userId, () -> {
            UserEntity user = iUserService.getById(userId);
            if (user == null || StringUtils.isBlank(user.getMaOpenId())) {
                return null;
            }
            String templateId = subscribeMessageConfigReader.getTemplateId(SubscribeMessageScene.VIP_EXPIRE);
            return subscribeMessageBuilder.buildVipExpire(
                    user.getMaOpenId(),
                    templateId,
                    title,
                    expireAt,
                    remark);
        });
    }

    @Override
    public boolean sendWrongReview(Long userId, int staleCount, String title, String remark) {
        if (userId == null || staleCount <= 0) {
            return false;
        }
        return sendIfConfigured(SubscribeMessageScene.WRONG_REVIEW, userId, () -> {
            UserEntity user = iUserService.getById(userId);
            if (user == null || StringUtils.isBlank(user.getMaOpenId())) {
                return null;
            }
            String templateId = subscribeMessageConfigReader.getTemplateId(SubscribeMessageScene.WRONG_REVIEW);
            return subscribeMessageBuilder.buildWrongReview(
                    user.getMaOpenId(),
                    templateId,
                    title,
                    staleCount,
                    remark);
        });
    }

    private boolean sendIfConfigured(SubscribeMessageScene scene, Long userId, Supplier<WxMaSubscribeMessage> supplier) {
        if (!subscribeMessageConfigReader.isConfigured(scene)) {
            log.debug("订阅消息模板未配置，跳过 scene={} userId={}", scene.code(), userId);
            return false;
        }
        WxMaSubscribeMessage message = supplier.get();
        if (message == null) {
            log.debug("用户无小程序 openId，跳过订阅消息 scene={} userId={}", scene.code(), userId);
            return false;
        }
        dispatch(userId, message);
        log.info("已发送微信订阅消息 scene={} userId={}", scene.code(), userId);
        return true;
    }

    private void dispatch(Long userId, WxMaSubscribeMessage message) {
        NotificationRequest request = new NotificationRequest();
        request.setUserId(userId);
        request.setWxMaSubscribeMessage(message);
        request.setClientType(ClientEnum.USER.getCode());
        notificationService.send(request, NotificationChannel.MA.code());
    }

    private UserEntity requireUserWithOpenId(Long userId) {
        if (userId == null) {
            throw new BizException(AppError.USER_NOT_FOUND);
        }
        UserEntity user = iUserService.getById(userId);
        if (user == null) {
            throw new BizException(AppError.USER_NOT_FOUND);
        }
        if (StringUtils.isBlank(user.getMaOpenId())) {
            throw new BizException(AppError.SUBSCRIBE_OPENID_MISSING);
        }
        return user;
    }
}
