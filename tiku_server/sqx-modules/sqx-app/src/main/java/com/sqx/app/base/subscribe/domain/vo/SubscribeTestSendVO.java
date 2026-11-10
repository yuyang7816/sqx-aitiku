package com.sqx.app.base.subscribe.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 订阅消息测试发送结果
 */
@Data
public class SubscribeTestSendVO {

    @Schema(description = "业务场景：monthly_report / vip_expire / wrong_review")
    private String scene;

    @Schema(description = "是否已向微信发起发送")
    private Boolean sent;

    @Schema(description = "说明（如模板未配置、openId 缺失等）")
    private String message;
}
