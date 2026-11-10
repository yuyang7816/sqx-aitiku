package com.sqx.app.base.subscribe.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 订阅消息授权登记出参
 */
@Data
public class SubscribeRegisterVO {

    @Schema(description = "是否已登记")
    private Boolean registered;

    @Schema(description = "已登记的场景")
    private List<String> acceptedScenes = new ArrayList<>();
}
