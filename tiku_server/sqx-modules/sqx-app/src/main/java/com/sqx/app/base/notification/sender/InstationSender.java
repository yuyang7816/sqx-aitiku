package com.sqx.app.base.notification.sender;

import com.sqx.app.base.messages.db.service.IMessagesService;
import com.sqx.app.base.messages.domain.entity.Messages;
import com.sqx.app.base.notification.core.enums.NotificationChannel;
import com.sqx.app.base.notification.core.model.NotificationRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * @author chengming
 * @since 2025/9/9
 */
@Slf4j
@Component
public class InstationSender implements INotificationSender {
    @Resource
    private IMessagesService iMessagesService;

    @Override
    public boolean support(String code) {
        return Objects.equals(NotificationChannel.INSTATION.code(), code);
    }

    @Override
    public void send(NotificationRequest request) {
        log.info("发送消息到站内消息");

        Messages messages = new Messages();
        messages.setUserId(request.getUserId());
        messages.setTitle(request.getTitle());
        messages.setContent(request.getContent());
        messages.setType(request.getType());
        messages.setDataId(request.getDataId());
        messages.setImageUrls(request.getImageUrls());
        messages.setUrl(request.getUrl());
        messages.setIsRead(0);

        iMessagesService.save(messages);
    }
}
