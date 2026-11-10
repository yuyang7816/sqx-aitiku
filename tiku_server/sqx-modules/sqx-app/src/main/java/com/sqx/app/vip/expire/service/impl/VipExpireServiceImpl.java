package com.sqx.app.vip.expire.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.sqx.app.base.messages.db.service.IMessagesService;
import com.sqx.app.base.messages.domain.entity.Messages;
import com.sqx.app.base.notification.core.enums.NotificationChannel;
import com.sqx.app.base.notification.core.model.NotificationRequest;
import com.sqx.app.base.notification.core.service.INotificationService;
import com.sqx.app.base.subscribe.service.SubscribePushService;
import com.sqx.app.base.user.db.service.IUserService;
import com.sqx.app.base.user.domain.entity.UserEntity;
import com.sqx.app.vip.expire.service.VipExpireService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * 会员到期降级：检查 vipExpireAt，过期后将 isVip 置 0 并推送 notice-member 站内消息
 */
@Slf4j
@Service
public class VipExpireServiceImpl implements VipExpireService {

    public static final String NOTICE_MEMBER_URL = "notice-member";
    private static final int VIP_FLAG = 1;
    private static final int USER_TYPE_NORMAL = 1;
    private static final int MESSAGE_TYPE_USER = 2;
    private static final DateTimeFormatter EXPIRE_TITLE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy年M月d日", Locale.CHINA);

    @Resource
    private IUserService iUserService;
    @Resource
    private INotificationService notificationService;
    @Resource
    private IMessagesService messagesService;
    @Resource
    private SubscribePushService subscribePushService;

    @Override
    public UserEntity refreshIfExpired(Long userId) {
        if (userId == null) {
            return null;
        }
        UserEntity user = iUserService.getById(userId);
        return refreshIfExpired(user);
    }

    @Override
    public UserEntity refreshIfExpired(UserEntity user) {
        if (user == null) {
            return null;
        }
        if (!isExpired(user, LocalDateTime.now())) {
            return user;
        }
        if (downgradeIfExpired(user.getUserId())) {
            return iUserService.getById(user.getUserId());
        }
        return user;
    }

    @Override
    public int downgradeAllExpired() {
        LocalDateTime now = LocalDateTime.now();
        List<UserEntity> expiredUsers = listExpiredVipUsers(now);
        if (expiredUsers.isEmpty()) {
            return 0;
        }
        int downgraded = 0;
        for (UserEntity user : expiredUsers) {
            try {
                if (downgradeIfExpired(user.getUserId(), now)) {
                    downgraded++;
                }
            } catch (Exception ex) {
                log.error("会员到期降级失败 userId={}: {}", user.getUserId(), ex.getMessage(), ex);
            }
        }
        return downgraded;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean downgradeIfExpired(Long userId) {
        return downgradeIfExpired(userId, LocalDateTime.now());
    }

    @Transactional(rollbackFor = Exception.class)
    boolean downgradeIfExpired(Long userId, LocalDateTime now) {
        if (userId == null || now == null) {
            return false;
        }
        UserEntity user = iUserService.getById(userId);
        if (user == null || !isExpired(user, now)) {
            return false;
        }

        boolean updated = iUserService.update(new LambdaUpdateWrapper<UserEntity>()
                .eq(UserEntity::getUserId, userId)
                .eq(UserEntity::getIsVip, VIP_FLAG)
                .isNotNull(UserEntity::getVipExpireAt)
                .le(UserEntity::getVipExpireAt, now)
                .set(UserEntity::getIsVip, 0)
                .set(UserEntity::getUserType, USER_TYPE_NORMAL));
        if (!updated) {
            return false;
        }

        pushExpireNotice(userId, user.getVipExpireAt());
        log.info("会员已到期降级 userId={} vipExpireAt={}", userId, user.getVipExpireAt());
        return true;
    }

    List<UserEntity> listExpiredVipUsers(LocalDateTime now) {
        LambdaQueryWrapper<UserEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserEntity::getIsVip, VIP_FLAG)
                .isNotNull(UserEntity::getVipExpireAt)
                .le(UserEntity::getVipExpireAt, now);
        List<UserEntity> users = iUserService.list(wrapper);
        return users == null ? Collections.emptyList() : users;
    }

    boolean isExpired(UserEntity user, LocalDateTime now) {
        if (user == null || now == null) {
            return false;
        }
        if (user.getIsVip() == null || !Objects.equals(user.getIsVip(), VIP_FLAG)) {
            return false;
        }
        LocalDateTime expireAt = user.getVipExpireAt();
        return expireAt != null && !expireAt.isAfter(now);
    }

    void pushExpireNotice(Long userId, LocalDateTime expireAt) {
        String title = buildExpireNoticeTitle(expireAt);
        if (alreadyPushed(userId, title)) {
            log.debug("会员到期通知已存在，跳过 userId={} title={}", userId, title);
            return;
        }
        NotificationRequest request = new NotificationRequest();
        request.setUserId(userId);
        request.setTitle(title);
        request.setContent(buildExpireNoticeContent(expireAt));
        request.setUrl(NOTICE_MEMBER_URL);
        request.setType(MESSAGE_TYPE_USER);
        notificationService.send(request, NotificationChannel.INSTATION.code());
        subscribePushService.sendVipExpire(userId, title, expireAt, request.getContent());
    }

    boolean alreadyPushed(Long userId, String title) {
        LambdaQueryWrapper<Messages> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Messages::getUserId, userId)
                .eq(Messages::getUrl, NOTICE_MEMBER_URL)
                .eq(Messages::getTitle, title);
        return messagesService.count(wrapper) > 0;
    }

    String buildExpireNoticeTitle(LocalDateTime expireAt) {
        if (expireAt == null) {
            return "会员已到期";
        }
        return "会员已到期（" + expireAt.format(EXPIRE_TITLE_FORMAT) + "）";
    }

    String buildExpireNoticeContent(LocalDateTime expireAt) {
        if (expireAt == null) {
            return "你的会员已到期，AI 与 OCR 等功能已恢复为免费额度限制，续费后可继续享受会员权益。";
        }
        return "你的会员已于 " + expireAt.format(EXPIRE_TITLE_FORMAT)
                + " 到期，AI 与 OCR 等功能已恢复为免费额度限制，续费后可继续享受会员权益。";
    }
}
