package com.sqx.app.base.subscribe.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户端订阅消息模板配置（供 uniapp requestSubscribeMessage 使用）
 */
@Data
public class SubscribeTemplateVO {

    @Schema(description = "月报订阅消息模板 ID")
    private String monthlyReportTemplateId;

    @Schema(description = "会员到期/续费提醒模板 ID")
    private String vipExpireTemplateId;

    @Schema(description = "错题复习提醒模板 ID")
    private String wrongReviewTemplateId;
}
