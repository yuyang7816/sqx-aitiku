package com.sqx.app.base.notification.sender;

import com.sqx.app.base.notification.core.model.NotificationRequest;

/**
 * @author chengming
 * @since 2025/9/9
 */
public interface INotificationSender {

    boolean support(String code);

    void send(NotificationRequest request);
}
