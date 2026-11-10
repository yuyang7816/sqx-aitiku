package com.sqx.common.pay.channel.wechat.manager;

import com.github.binarywang.wxpay.service.WxPayService;
import com.github.binarywang.wxpay.service.impl.WxPayServiceImpl;
import com.sqx.common.pay.channel.wechat.domain.WxPayConfig;
import com.sqx.common.pay.channel.wechat.service.WxPayConfigService;
import com.sqx.common.pay.constants.PayConstant;
import com.sqx.common.redis.utils.ConfigUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author chengming
 * @since 2026/1/9
 */
@Slf4j
@Component
public class WxPayServiceManager {

    @Resource
    private WxPayConfigService wxPayConfigService;

    private final Map<String, WxPayService> wxPayServiceMap = new ConcurrentHashMap<>();

    public void initializeWxPayServices() {
        try {
            List<WxPayConfig> wxPayConfigs = wxPayConfigService.getWxPayConfigs();

            for (WxPayConfig wxPayConfig : wxPayConfigs) {
                registerServiceIfPresent(wxPayConfig.getAppId(), "小程序");
                registerServiceIfPresent(wxPayConfig.getH5AppId(), "H5");
                registerServiceIfPresent(wxPayConfig.getJsAppId(), "公众号/JSAPI");
            }

            log.info("已初始化 {} 个微信支付客户端", wxPayServiceMap.size());
        } catch (Exception e) {
            log.warn("微信支付客户端初始化失败，支付功能暂不可用: {}", e.getMessage());
        }
    }

    private void registerServiceIfPresent(String appId, String desc) {
        if (StringUtils.isBlank(appId)) {
            return;
        }
        try {
            WxPayService service = createWxPayService(appId);
            wxPayServiceMap.put(appId, service);
            log.info("注册微信支付客户端: {} (appId={})", desc, appId);
        } catch (Exception e) {
            log.warn("跳过微信支付客户端 {} (appId={}): {}", desc, appId, e.getMessage());
        }
    }

    private WxPayService createWxPayService(String appId) {
        com.github.binarywang.wxpay.config.WxPayConfig config = new com.github.binarywang.wxpay.config.WxPayConfig();

        String mchId = ConfigUtil.getConfigValue(PayConstant.Wx.MCH_ID);
        String mchKey = ConfigUtil.getConfigValue(PayConstant.Wx.MCH_KEY);
        String keyPath = ConfigUtil.getConfigValue(PayConstant.Wx.KEY_PATH);

        config.setAppId(appId);
        config.setMchId(mchId);
        config.setMchKey(mchKey);
        config.setKeyPath(keyPath);

        WxPayService service = new WxPayServiceImpl();
        service.setConfig(config);
        return service;
    }

    /**
     * 根据 appId 获取对应的微信支付客户端
     *
     * @param appId 微信 AppID（小程序/公众号/H5）
     * @return WxPayService 实例
     * @throws IllegalArgumentException 如果 appId 未配置或未初始化
     */
    public WxPayService getWxPayService(String appId) {
        WxPayService service = wxPayServiceMap.get(appId);
        if (service == null) {
            throw new IllegalArgumentException("未找到 appId=" + appId + " 对应的微信支付客户端，请检查配置");
        }

        return service;
    }
}
