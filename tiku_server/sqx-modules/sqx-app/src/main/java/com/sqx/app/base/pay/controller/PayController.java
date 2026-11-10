package com.sqx.app.base.pay.controller;

import com.sqx.app.base.constants.BaseConstant;
import com.sqx.app.base.enums.ClientEnum;
import com.sqx.app.base.pay.business.service.BusinessService;
import com.sqx.app.base.pay.domain.BusinessParam;
import com.sqx.app.base.pay.enums.BusinessType;
import com.sqx.app.base.utils.RequestUtil;
import com.sqx.common.core.domain.Result;
import com.sqx.common.core.exception.BizException;
import com.sqx.common.core.utils.GenUtil;
import com.sqx.common.pay.domain.UnifiedPayRequest;
import com.sqx.common.pay.enums.PayChannel;
import com.sqx.common.pay.enums.PayTradeType;
import com.sqx.common.pay.service.IPayService;
import com.sqx.common.redis.utils.ConfigUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

/**
 * @author chengming
 * @since 2026/1/9
 */
@Slf4j
@Tag(name = "统一支付接口", description = "统一支付接口")
@RestController
@RequestMapping(BaseConstant.BASE_APP_PREFIX + "/pay")
public class PayController {

    // 支付回调地址
    private static final String NOTIFY_URL = BaseConstant.BASE_APP_PREFIX + "/pay/notify/%s";

    @Resource
    private IPayService iPayService;
    @Resource
    private BusinessService businessService;

    @Operation(description = "统一支付")
    @PostMapping("pay")
    public Result<String> pay(BusinessParam param) {
        String resp = null;
        try {
            // 支付业务
            businessService.process(param);

            // 统一支付
            UnifiedPayRequest request = convertRequest(param);
            resp = iPayService.unifiedPay(request);
        } catch (Exception e) {
            e.printStackTrace();
            throw new BizException("支付失败");
        }

        return Result.success(resp);
    }

    private UnifiedPayRequest convertRequest(BusinessParam param) {
        UnifiedPayRequest.UnifiedPayRequestBuilder builder = UnifiedPayRequest.builder()
                .orderNo(StringUtils.isBlank(param.getOrderNo()) ? GenUtil.genOrderNo() : param.getOrderNo())
                .tradeType(param.getTradeType())
                .amount(param.getAmount())
                .channel(param.getChannel())
                .returnUrl(param.getRedirectUri())
                .clientIp("127.0.0.1")
                .openId(param.getOpenId());

        // 回调地址
        String clientType = RequestUtil.getClientType();
        ClientEnum client = ClientEnum.fromCode(clientType);
        String domain = ConfigUtil.getConfigValue(client.getDomain());
        PayChannel payChannel = PayChannel.fromCode(param.getChannel());
        builder.notifyUrl(domain + String.format(NOTIFY_URL, payChannel.getValue()));

        // 订单标题
        BusinessType businessType = BusinessType.fromCode(param.getBusinessType());
        builder.subject(businessType.getDesc());

        // 微信appId
        String appId = getWxAppId(param.getTradeType());
        builder.appId(appId);

        return builder.build();
    }

    private String getWxAppId(String tradeType) {
        if (Objects.equals(tradeType, PayTradeType.JSAPI.getCode())) {
            return ConfigUtil.getConfigValue("pay_wx_jsapi_app_id");
        }

        if (Objects.equals(tradeType, PayTradeType.H5.getCode())) {
            return ConfigUtil.getConfigValue("pay_wx_h5_app_id");
        }

        if (Objects.equals(tradeType, PayTradeType.APP.getCode())) {
            return ConfigUtil.getConfigValue("pay_wx_app_id");
        }

        log.error("不支持的交易类型: {}", tradeType);
        throw new BizException("不支持的交易类型");
    }

    @Operation(hidden = true)
    @PostMapping("/notify/{channel}")
    public String handleNotify(HttpServletRequest request, @PathVariable String channel) {
        try {
            PayChannel payChannel = PayChannel.fromCode(channel);
            return iPayService.notify(request, payChannel.getValue());
        } catch (Exception e) {
            log.error("处理 {} 回调异常", channel, e);
            return "fail";
        }
    }
}
