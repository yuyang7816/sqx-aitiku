package com.sqx.app.base.verifyCode.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author chengming
 * @since 2025/4/24
 */
@Data
public class SendPhoneCodeBO {

    @Schema(description = "手机号")
    @NotBlank(message = "手机号不能为空")
    private String phone;

    @Schema(description = "业务码", example = "common")
    @NotBlank(message = "业务码不能为空")
    private String bizCode;
}
