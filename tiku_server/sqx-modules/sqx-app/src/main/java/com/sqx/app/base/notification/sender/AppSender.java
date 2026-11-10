package com.sqx.app.base.notification.sender;

import com.sqx.app.base.notification.core.enums.NotificationChannel;
import com.sqx.app.base.notification.core.model.NotificationRequest;
import com.sqx.app.base.notification.remote.GetuiApi;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * @author chengming
 * @since 2025/9/9
 */
@Slf4j
@Component
public class AppSender implements INotificationSender {

    @Resource
    private GetuiApi getuiApi;

    @Override
    public boolean support(String code) {
        return Objects.equals(NotificationChannel.APP.code(), code);
    }

    @Override
    public void send(NotificationRequest request) {
        log.info("发送消息到app");
        if (StringUtils.isBlank(request.getCid())) {
            log.info("cid为空，停止app推送");
            return;
        }

        String cid = request.getCid();
        String title = request.getTitle();
        String content = request.getContent();
        String url = request.getUrl();
        String msgNo = request.getMsgNo();

        getuiApi.singlePush(cid, msgNo, title, content, url);
    }
}
