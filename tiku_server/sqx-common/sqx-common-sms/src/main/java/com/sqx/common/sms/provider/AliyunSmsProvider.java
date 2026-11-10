package com.sqx.common.sms.provider;

import com.aliyun.ecs20140526.Client;
import com.aliyun.ecs20140526.models.DescribeRegionsRequest;
import com.aliyun.ecs20140526.models.DescribeRegionsResponse;
import com.aliyun.teaopenapi.models.Config;
import com.aliyun.teautil.models.RuntimeOptions;
import com.sqx.common.core.exception.BizException;
import com.sqx.common.redis.utils.ConfigUtil;
import com.sqx.common.sms.enums.SmsError;
import com.sqx.common.sms.enums.SmsProviderEnum;
import lombok.extern.slf4j.Slf4j;

/**
 * @author chengming
 * @since 2025/6/11
 */
@Slf4j
public class AliyunSmsProvider implements ISmsProvider {

    // 默认地域
    public static final String DEFAULT_REGION_ID = "cn-hangzhou";

    @Override
    public String getProviderName() {
        return SmsProviderEnum.ALIYUN.getDesc();
    }

    @Override
    public void sendSms(String phone, String content) {
        //阿里云短信accessKeyId
        String accessKeyId = ConfigUtil.getConfigValue("sms_ali_access_key");
        //阿里云短信accessSecret
        String accessSecret = ConfigUtil.getConfigValue("sms_ali_access_secret");

        Config config = new Config()
                .setAccessKeyId(accessKeyId)
                .setAccessKeySecret(accessSecret)
                .setRegionId(DEFAULT_REGION_ID);

        DescribeRegionsResponse describeRegionsResponse = null;
        try {
            Client client = new Client(config);
            // 运行时参数重试设置，仅对使用了该运行时参数实例的请求有效
            RuntimeOptions runtimeOptions = new RuntimeOptions();
            DescribeRegionsRequest describeRegionsRequest = new DescribeRegionsRequest();
            // 同步调用
            describeRegionsResponse = client.describeRegionsWithOptions(describeRegionsRequest, runtimeOptions);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("[sms]- 短信发送失败: {}", e.getMessage());
            throw new BizException(SmsError.SMS_SEND_ERROR);
        }

        log.info("[sms] - 阿里云短信返回值: {}", describeRegionsResponse.getBody());
        if (0 == describeRegionsResponse.getStatusCode()) {
            log.info("[sms] - 短信发送成功");
            return;
        }

        throw new BizException(SmsError.SMS_SEND_ERROR);
    }
}
