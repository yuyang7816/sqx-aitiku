package com.sqx.app.base.customerConfig.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 客服配置（结构化 JSON，供 uniapp 一次拉取）
 */
@Data
public class CustomerConfigVO {

    @Schema(description = "客服入口是否展示")
    private Boolean enabled;

    @Schema(description = "客服类型，如 wework / phone")
    private String type;

    @Schema(description = "客服电话")
    private String phone;

    @Schema(description = "企业微信客服链接")
    private String weworkUrl;

    @Schema(description = "企业微信客服 AppId")
    private String weworkAppId;

    @Schema(description = "在线 IM 是否开启")
    private Boolean imEnabled;

    @Schema(description = "在线 IM 欢迎语")
    private String imWelcome;

    @Schema(description = "客服微信号")
    private String wechatId;

    @Schema(description = "客服微信二维码图片 URL")
    private String wechatQrUrl;
}
