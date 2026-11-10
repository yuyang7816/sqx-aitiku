package com.sqx.app.base.auth.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 登录响应中的资料门禁状态（文档 23.1：profile.ready 决定是否可进首页）
 */
@Data
@Schema(description = "用户资料门禁状态（ready=false 时 uniapp 跳转学习计划页）")
public class LoginProfileStateVO {

    @Schema(description = "资料是否已完善（grade+subjects 已填写且 profile_ready=1）")
    private Boolean ready;
}
