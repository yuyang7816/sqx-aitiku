package com.sqx.common.sms.provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 开发环境 Mock 短信通道：仅打日志，不调用外部 API
 */
public class MockSmsProvider implements ISmsProvider {

    private static final Logger log = LoggerFactory.getLogger(MockSmsProvider.class);

    @Override
    public String getProviderName() {
        return "mock";
    }

    @Override
    public void sendSms(String phone, String content) {
        log.info("[MockSmsProvider] phone={}, content={}", phone, content);
    }
}
