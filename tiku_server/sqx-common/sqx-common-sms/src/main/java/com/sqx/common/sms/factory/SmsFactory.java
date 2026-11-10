package com.sqx.common.sms.factory;

import com.sqx.common.core.exception.BizException;
import com.sqx.common.sms.provider.ISmsProvider;
import com.sqx.common.sms.enums.SmsError;
import com.sqx.common.sms.enums.SmsProviderEnum;
import com.sqx.common.sms.provider.AliyunSmsProvider;
import com.sqx.common.sms.provider.DXBSmsProvider;
import com.sqx.common.sms.provider.MockSmsProvider;
import com.sqx.common.sms.provider.TencentSmsProvider;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author chengming
 * @since 2025/6/11
 */
@Component
public class SmsFactory {

    private static final Map<String, ISmsProvider> smsServiceCache = new ConcurrentHashMap<>();

    // 默认的短信平台
    public static final String DEFAULT_PROVIDER = SmsProviderEnum.DXB.getCode();

    /**
     * 获取sms服务
     *
     * @return
     */
    public static ISmsProvider getSmsService(String providerName) {
        if (StringUtils.isBlank(providerName)) {
            providerName = DEFAULT_PROVIDER;
        }

        if (smsServiceCache.containsKey(providerName)) {
            return smsServiceCache.get(providerName);
        }

        ISmsProvider smsService = createSmsService(providerName);
        smsServiceCache.put(providerName, smsService);
        return smsService;
    }

    private static ISmsProvider createSmsService(String providerName) {
        if (Objects.equals(providerName, SmsProviderEnum.MOCK.getCode())) {
            return new MockSmsProvider();
        } else if (Objects.equals(providerName, SmsProviderEnum.DXB.getCode())) {
            return new DXBSmsProvider();
        } else if (Objects.equals(providerName, SmsProviderEnum.ALIYUN.getCode())) {
            return new AliyunSmsProvider();
        } else if (Objects.equals(providerName, SmsProviderEnum.TENCENT.getCode())) {
            return new TencentSmsProvider();
        } else {
            throw new BizException(SmsError.NOT_SUPPORT_PROVIDER);
        }
    }
}