package com.sqx.common.sms.provider;

/**
 * @author chengming
 * @since 2025/6/11
 *
 * 短信服务接口
 */
public interface ISmsProvider {

    /**
     * 获取服务提供商名称
     */
    String getProviderName();

    /**
     * 发送短信
     * @param phone 手机号
     * @param content 短信内容
     */
    void sendSms(String phone, String content);
}
