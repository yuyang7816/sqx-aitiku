package com.sqx.app.base.notification.sender;

import com.sqx.app.base.notification.core.enums.NotificationChannel;
import com.sqx.app.base.notification.core.model.NotificationRequest;
import com.sqx.app.base.utils.wx.WxMaSubscribeMessage;
import com.sqx.app.base.utils.wx.WxUtil;
import com.sqx.common.core.exception.BizException;
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
public class WxMaSender implements INotificationSender {

    @Resource
    private WxUtil wxUtil;

    @Override
    public boolean support(String code) {
        return Objects.equals(NotificationChannel.MA.code(), code);
    }

    @Override
    public void send(NotificationRequest request) {
        log.info("发送消息到小程序");

        if (Objects.isNull(request.getWxMaSubscribeMessage())) {
            log.info("消息为空，不发送微信订阅消息");
            return;
        }

        if (StringUtils.isBlank(request.getWxMaSubscribeMessage().getToUser())) {
            log.info("用户openId为空，不发送微信订阅消息");
            return;
        }

        String clientType = request.getClientType();
        WxMaSubscribeMessage message = request.getWxMaSubscribeMessage();

        try {
            wxUtil.sendSubscribeMessage(message, clientType);
        } catch (Exception e) {
            log.error("小程序消息发送失败, 错误信息: [{}]", e.getMessage());
            throw new BizException("小程序消息发送失败");
        }
    }
}