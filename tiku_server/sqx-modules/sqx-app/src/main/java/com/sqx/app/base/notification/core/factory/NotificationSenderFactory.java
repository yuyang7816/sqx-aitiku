package com.sqx.app.base.notification.core.factory;

import com.sqx.app.base.notification.sender.INotificationSender;
import com.sqx.common.core.exception.BizException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author chengming
 * @since 2025/11/18
 */
@Slf4j
@Component
public class NotificationSenderFactory {

    @Resource
    private List<INotificationSender> senders;

    public INotificationSender getNotificationSender(String code) {
        for (INotificationSender sender : senders) {
            if (sender.support(code)) {
                return sender;
            }
        }

        throw new BizException("不支持的通知渠道");
    }
}
