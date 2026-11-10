package com.sqx.common.sms;

import com.sqx.common.redis.utils.ConfigUtil;
import com.sqx.common.sms.provider.ISmsProvider;
import com.sqx.common.sms.factory.SmsFactory;
import org.springframework.stereotype.Component;

/**
 * @author chengming
 * @since 2025/6/11
 */
@Component
public class SmsService {

    /**
     * 发送短信
     * @param phone 手机号
     * @param content 内容
     */
    public void sendSms(String phone, String content) {
        String smsProvider = ConfigUtil.getConfigValue("sms_provider");

        ISmsProvider smsService = SmsFactory.getSmsService(smsProvider);
        smsService.sendSms(phone, content);
    }
}
