package com.sqx.app.vip.pay.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 创建会员支付订单入参（POST /app/pay）
 */
@Data
public class AppPayCreateBO {

    @NotBlank(message = "套餐名称不能为空")
    @Schema(description = "套餐名称（业务键，如月卡、季卡）", example = "月卡")
    private String planName;

    @NotBlank(message = "支付方式不能为空")
    @Schema(description = "支付方式：wechat 微信支付 / alipay 支付宝", example = "wechat")
    private String payMethod;

    @Schema(description = "客户端平台：mp-weixin|h5-wechat|app-plus|h5|mp-alipay，与 uniapp 运行时一致")
    private String clientPlatform;
}
