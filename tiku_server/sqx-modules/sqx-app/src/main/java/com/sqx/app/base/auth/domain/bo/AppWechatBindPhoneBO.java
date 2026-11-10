package com.sqx.app.base.auth.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 小程序 getPhoneNumber 返回的 code，服务端调用微信 getuserphonenumber 官方接口换号
 */
@Data
public class AppWechatBindPhoneBO {

    @Schema(description = "button open-type=getPhoneNumber 回调 detail.code")
    @NotBlank(message = "手机号授权凭证不能为空")
    private String phoneCode;
}
