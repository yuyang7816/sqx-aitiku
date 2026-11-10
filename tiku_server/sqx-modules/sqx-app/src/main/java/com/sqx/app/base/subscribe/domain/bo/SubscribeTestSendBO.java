package com.sqx.app.base.subscribe.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 订阅消息测试发送入参
 */
@Data
public class SubscribeTestSendBO {

    @NotBlank(message = "scene 不能为空")
    @Schema(description = "业务场景：monthly_report / vip_expire / wrong_review", required = true)
    private String scene;
}
