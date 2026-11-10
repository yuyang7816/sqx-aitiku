package com.sqx.app.vip.renew.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sqx.app.base.messages.db.service.IMessagesService;
import com.sqx.app.base.messages.domain.entity.Messages;
import com.sqx.app.base.notification.core.enums.NotificationChannel;
import com.sqx.app.base.notification.core.model.NotificationRequest;
import com.sqx.app.base.notification.core.service.INotificationService;
import com.sqx.app.base.subscribe.service.SubscribePushService;
import com.sqx.app.base.user.db.service.IUserService;
import com.sqx.app.base.user.domain.entity.UserEntity;
import com.sqx.app.vip.renew.service.VipRenewNoticeService;
import com.sqx.app.vip.renew.support.VipRenewConfigReader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * 会员续费提醒：vipExpireAt 距今不超过 N 天且未过期时推送 notice-member
 */
@Slf4j
@Service
public class VipRenewNoticeServiceImpl implements VipRenewNoticeService {

    public static final String NOTICE_MEMBER_URL = "notice-member";
    private static final int VIP_FLAG = 1;
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
    private VipRenewConfigReader vipRenewConfigReader;
    @Resource
    private SubscribePushService subscribePushService;

    @Override
    public int pushAllDue() {
        LocalDateTime now = LocalDateTime.now();
        int noticeDays = vipRenewConfigReader.getNoticeDaysBeforeExpire();
        List<UserEntity> dueUsers = listDueVipUsers(now, noticeDays);
        if (dueUsers.isEmpty()) {
            return 0;
        }
        int pushed = 0;
        for (UserEntity user : dueUsers) {
            try {
                if (pushToUser(user, now, noticeDays)) {
                    pushed++;
                }
            } catch (Exception ex) {
                log.error("续费提醒推送失败 userId={}: {}", user.getUserId(), ex.getMessage(), ex);
            }
        }
        return pushed;
    }

    @Override
    public boolean pushToUser(Long userId) {
        if (userId == null) {
            return false;
        }
        UserEntity user = iUserService.getById(userId);
        if (user == null) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        int noticeDays = vipRenewConfigReader.getNoticeDaysBeforeExpire();
        return pushToUser(user, now, noticeDays);
    }

    boolean pushToUser(UserEntity user, LocalDateTime now, int noticeDays) {
        if (user == null || now == null || noticeDays <= 0) {
            return false;
        }
        if (!isDueForRenewNotice(user, now, noticeDays)) {
            return false;
        }
        LocalDateTime expireAt = user.getVipExpireAt();
        String title = buildRenewNoticeTitle(expireAt);
        if (alreadyPushed(user.getUserId(), title)) {
            log.debug("续费提醒已存在，跳过 userId={} title={}", user.getUserId(), title);
            return false;
        }
        long remainingDays = resolveRemainingDays(now, expireAt);
        NotificationRequest request = new NotificationRequest();
        request.setUserId(user.getUserId());
        request.setTitle(title);
        request.setContent(buildRenewNoticeContent(remainingDays));
        request.setUrl(NOTICE_MEMBER_URL);
        request.setType(MESSAGE_TYPE_USER);
        notificationService.send(request, NotificationChannel.INSTATION.code());
        subscribePushService.sendVipExpire(user.getUserId(), title, expireAt, request.getContent());
        log.info("已推送续费提醒 userId={} vipExpireAt={} remainingDays={}",
                user.getUserId(), expireAt, remainingDays);
        return true;
    }

    List<UserEntity> listDueVipUsers(LocalDateTime now, int noticeDays) {
        LocalDateTime windowEnd = resolveWindowEnd(now, noticeDays);
        LambdaQueryWrapper<UserEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserEntity::getIsVip, VIP_FLAG)
                .isNotNull(UserEntity::getVipExpireAt)
                .gt(UserEntity::getVipExpireAt, now)
                .le(UserEntity::getVipExpireAt, windowEnd);
        List<UserEntity> users = iUserService.list(wrapper);
        return users == null ? Collections.emptyList() : users;
    }

    boolean isDueForRenewNotice(UserEntity user, LocalDateTime now, int noticeDays) {
        if (user == null || now == null || noticeDays <= 0) {
            return false;
        }
        if (user.getIsVip() == null || !Objects.equals(user.getIsVip(), VIP_FLAG)) {
            return false;
        }
        LocalDateTime expireAt = user.getVipExpireAt();
        if (expireAt == null || !expireAt.isAfter(now)) {
            return false;
        }
        long remainingDays = resolveRemainingDays(now, expireAt);
        return remainingDays > 0 && remainingDays <= noticeDays;
    }

    LocalDateTime resolveWindowEnd(LocalDateTime now, int noticeDays) {
        LocalDate maxExpireDate = now.toLocalDate().plusDays(noticeDays);
        return maxExpireDate.atTime(LocalTime.MAX);
    }

    long resolveRemainingDays(LocalDateTime now, LocalDateTime expireAt) {
        if (now == null || expireAt == null) {
            return 0L;
        }
        long days = ChronoUnit.DAYS.between(now.toLocalDate(), expireAt.toLocalDate());
        return Math.max(days, 1L);
    }

    boolean alreadyPushed(Long userId, String title) {
        LambdaQueryWrapper<Messages> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Messages::getUserId, userId)
                .eq(Messages::getUrl, NOTICE_MEMBER_URL)
                .eq(Messages::getTitle, title);
        return messagesService.count(wrapper) > 0;
    }

    String buildRenewNoticeTitle(LocalDateTime expireAt) {
        if (expireAt == null) {
            return "会员即将到期提醒";
        }
        return "会员即将到期提醒（" + expireAt.format(EXPIRE_TITLE_FORMAT) + "）";
    }

    String buildRenewNoticeContent(long remainingDays) {
        return "你的会员还有 " + remainingDays + " 天到期，续费后可继续使用无限次 AI 搜题与答疑。";
    }
}
