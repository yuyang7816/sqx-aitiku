package com.sqx.app.base.subscribe.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * 订阅消息授权登记（POST /app/subscribe/register）
 */
@Data
public class SubscribeRegisterBO {

    @NotNull(message = "pushEnabled 不能为空")
    @Schema(description = "是否开启学习提醒")
    private Boolean pushEnabled;

    @Schema(description = "用户已授权的模板场景：monthly_report/vip_expire/wrong_review")
    private List<String> acceptedScenes = new ArrayList<>();
}
