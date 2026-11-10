package com.sqx.app.vip.pay.support;

import com.sqx.app.base.constants.BaseConstant;
import com.sqx.app.base.enums.ClientEnum;
import com.sqx.app.base.utils.RequestUtil;
import com.sqx.common.pay.constants.PayConstant;
import com.sqx.common.redis.utils.ConfigUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * 微信支付运行时配置（便于单元测试 mock）
 */
@Component
public class AppPayWxConfigSupport {

    private static final String NOTIFY_URL = BaseConstant.BASE_APP_PREFIX + "/pay/notify/%s";

    public String getMaAppId() {
        return ConfigUtil.getConfigValue(PayConstant.Wx.User.MA_APP_ID);
    }

    public String getMpAppId() {
        String mpAppId = ConfigUtil.getConfigValue(PayConstant.Wx.User.MP_APP_ID);
        if (StringUtils.isNotBlank(mpAppId)) {
            return mpAppId;
        }
        return ConfigUtil.getConfigValue(com.sqx.app.base.constants.SysConfigConstant.Sys.SYS_USER_MP_APPID);
    }

    public String getAppAppId() {
        String appAppId = ConfigUtil.getConfigValue(PayConstant.Wx.User.APP_ID);
        if (StringUtils.isNotBlank(appAppId)) {
            return appAppId;
        }
        return ConfigUtil.getConfigValue(com.sqx.app.base.constants.SysConfigConstant.Sys.SYS_USER_APP_APPID);
    }

    public String buildNotifyUrl(String channel) {
        String clientType = RequestUtil.getClientType();
        ClientEnum client = ClientEnum.fromCode(StringUtils.defaultIfBlank(clientType, ClientEnum.USER.getCode()));
        String domain = ConfigUtil.getConfigValue(client.getDomain());
        return domain + String.format(NOTIFY_URL, channel);
    }

    public String resolveClientIp() {
        if (RequestUtil.getCurrentRequest() != null
                && StringUtils.isNotBlank(RequestUtil.getCurrentRequest().getRemoteAddr())) {
            return RequestUtil.getCurrentRequest().getRemoteAddr();
        }
        return "127.0.0.1";
    }
}
