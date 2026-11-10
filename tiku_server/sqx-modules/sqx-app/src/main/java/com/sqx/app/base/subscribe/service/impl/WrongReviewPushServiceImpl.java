package com.sqx.app.base.subscribe.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sqx.app.base.messages.db.service.IMessagesService;
import com.sqx.app.base.messages.domain.entity.Messages;
import com.sqx.app.base.notification.core.enums.NotificationChannel;
import com.sqx.app.base.notification.core.model.NotificationRequest;
import com.sqx.app.base.notification.core.service.INotificationService;
import com.sqx.app.base.subscribe.constants.SubscribeMessageConstant;
import com.sqx.app.base.subscribe.service.SubscribePushService;
import com.sqx.app.base.subscribe.service.WrongReviewPushService;
import com.sqx.system.db.entity.UserQuestionWrong;
import com.sqx.system.db.service.IUserQuestionWrongService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 待复习错题提醒：超过 {@link SubscribeMessageConstant#STALE_WRONG_DAYS} 天未练习的错题
 */
@Slf4j
@Service
public class WrongReviewPushServiceImpl implements WrongReviewPushService {

    public static final String NOTICE_REVIEW_URL = "notice-review";
    private static final int MESSAGE_TYPE_USER = 2;

    @Resource
    private IUserQuestionWrongService iUserQuestionWrongService;
    @Resource
    private INotificationService notificationService;
    @Resource
    private IMessagesService messagesService;
    @Resource
    private SubscribePushService subscribePushService;

    @Override
    public int pushAllDue() {
        List<Long> userIds = listDueUserIds();
        int pushed = 0;
        for (Long userId : userIds) {
            try {
                if (pushToUser(userId)) {
                    pushed++;
                }
            } catch (Exception ex) {
                log.error("错题提醒推送失败 userId={}: {}", userId, ex.getMessage(), ex);
            }
        }
        return pushed;
    }

    @Override
    public boolean pushToUser(Long userId) {
        if (userId == null) {
            return false;
        }
        int staleCount = countStaleWrongQuestions(userId);
        if (staleCount <= 0) {
            return false;
        }
        String title = buildPushTitle(staleCount);
        if (alreadyPushed(userId, title)) {
            log.debug("错题提醒已存在，跳过 userId={} title={}", userId, title);
            return false;
        }
        String content = buildPushContent(staleCount);

        NotificationRequest request = new NotificationRequest();
        request.setUserId(userId);
        request.setTitle(title);
        request.setContent(content);
        request.setUrl(NOTICE_REVIEW_URL);
        request.setType(MESSAGE_TYPE_USER);
        notificationService.send(request, NotificationChannel.INSTATION.code());

        subscribePushService.sendWrongReview(userId, staleCount, title, content);

        log.info("已推送错题复习提醒 userId={} staleCount={}", userId, staleCount);
        return true;
    }

    List<Long> listDueUserIds() {
        LocalDateTime staleBefore = LocalDateTime.now().minusDays(SubscribeMessageConstant.STALE_WRONG_DAYS);
        LambdaQueryWrapper<UserQuestionWrong> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(UserQuestionWrong::getUserId)
                .eq(UserQuestionWrong::getMastered, UserQuestionWrong.MASTERED_NO)
                .and(w -> w.isNull(UserQuestionWrong::getLastPracticeAt)
                        .or()
                        .lt(UserQuestionWrong::getLastPracticeAt, staleBefore))
                .isNotNull(UserQuestionWrong::getUserId);
        List<UserQuestionWrong> records = iUserQuestionWrongService.list(wrapper);
        if (records == null || records.isEmpty()) {
            return Collections.emptyList();
        }
        return records.stream()
                .map(UserQuestionWrong::getUserId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }

    int countStaleWrongQuestions(Long userId) {
        LocalDateTime staleBefore = LocalDateTime.now().minusDays(SubscribeMessageConstant.STALE_WRONG_DAYS);
        LambdaQueryWrapper<UserQuestionWrong> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserQuestionWrong::getUserId, userId)
                .eq(UserQuestionWrong::getMastered, UserQuestionWrong.MASTERED_NO)
                .and(w -> w.isNull(UserQuestionWrong::getLastPracticeAt)
                        .or()
                        .lt(UserQuestionWrong::getLastPracticeAt, staleBefore));
        Long count = iUserQuestionWrongService.count(wrapper);
        return count == null ? 0 : Math.max(0, count.intValue());
    }

    boolean alreadyPushed(Long userId, String title) {
        LambdaQueryWrapper<Messages> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Messages::getUserId, userId)
                .eq(Messages::getUrl, NOTICE_REVIEW_URL)
                .eq(Messages::getTitle, title);
        return messagesService.count(wrapper) > 0;
    }

    String buildPushTitle(int staleCount) {
        return "待复习错题提醒（" + staleCount + "题）";
    }

    String buildPushContent(int staleCount) {
        return "错题本中有 " + staleCount + " 道题已超过 "
                + SubscribeMessageConstant.STALE_WRONG_DAYS
                + " 天未复习，建议今晚集中重做。";
    }
}
