package com.sqx.app.base.notification.core.service;

import com.sqx.app.base.notification.core.model.NotificationRequest;

/**
 * @author chengming
 * @since 2025/9/9
 */
public interface INotificationService {

    void send(NotificationRequest request);

    void send(NotificationRequest request, String type);

    void send(NotificationRequest request, String... notificationTypes);
}
