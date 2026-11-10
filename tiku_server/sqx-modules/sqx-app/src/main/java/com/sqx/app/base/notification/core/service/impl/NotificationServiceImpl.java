package com.sqx.app.base.notification.core.service.impl;

import com.sqx.app.base.notification.core.enums.NotificationChannel;
import com.sqx.app.base.notification.core.factory.NotificationSenderFactory;
import com.sqx.app.base.notification.core.model.NotificationRequest;
import com.sqx.app.base.notification.core.service.INotificationService;
import com.sqx.app.base.notification.sender.INotificationSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;

/**
 * @author chengming
 * @since 2025/9/10
 */
@Slf4j
@Component
@Primary
public class NotificationServiceImpl implements INotificationService {

    @Resource
    private NotificationSenderFactory notificationSenderFactory;

    /**
     * 指定单一渠道发送
     *
     * @param request 消息体
     * @param channel    消息推送方式
     */
    public void send(NotificationRequest request, String channel) {
        try {
            INotificationSender notificationSender = getNotificationSender(channel);
            notificationSender.send(request);
        } catch (Exception e) {
            log.error("发送通知到 [{}] 失败: [{}]", channel, e.getMessage(), e);
        }
    }

    /**
     * 向所有渠道发送
     *
     * @param request 消息体
     */
    public void send(NotificationRequest request) {
        String[] notificationChannels = Arrays.stream(NotificationChannel.values())
                .map(NotificationChannel::code)
                .toArray(String[]::new);

        send(request, notificationChannels);
    }

    /**
     * 同时向多个渠道发送
     *
     * @param request           消息体
     * @param notificationChannels 消息推送方式
     */
    public void send(NotificationRequest request, String... notificationChannels) {
        if (null == notificationChannels || 0 == notificationChannels.length) {
            throw new IllegalArgumentException("notificationTypes 不能为空");
        }

        for (String channel : notificationChannels) {
            send(request, channel);
        }
    }

    private INotificationSender getNotificationSender(String code) {
        return notificationSenderFactory.getNotificationSender(code);
    }
}
