package com.sqx.app.base.appConfig.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户端微信公开配置（不含 secret，供 H5 公众号 OAuth 跳转）
 */
@Data
public class WechatPublicConfigVO {

    @Schema(description = "公众号 AppId（H5 微信内置浏览器 OAuth）")
    private String mpAppId;

    @Schema(description = "小程序 AppId（可选，供调试）")
    private String maAppId;

    @Schema(description = "是否开启公众号 OAuth 登录（H5 微信内置浏览器）")
    private Boolean mpLoginEnabled;
}
