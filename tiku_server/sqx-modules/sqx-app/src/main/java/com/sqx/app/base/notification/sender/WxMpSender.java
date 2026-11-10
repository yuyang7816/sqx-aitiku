package com.sqx.app.base.notification.sender;

import com.sqx.app.base.notification.core.enums.NotificationChannel;
import com.sqx.app.base.notification.core.model.NotificationRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * @author chengming
 * @since 2025/9/9
 */
@Slf4j
@Component
public class WxMpSender implements INotificationSender {

    @Override
    public boolean support(String code) {
        return Objects.equals(NotificationChannel.MP.code(), code);
    }

    @Override
    public void send(NotificationRequest request) {
        log.info("发送消息到公众号");
    }
}
