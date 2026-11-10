package com.sqx.app.base.auth.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author chengming
 * @since 2025/6/9
 */
@Data
public class ResetPasswordBO {

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "验证码")
    private String code;

    @Schema(description = "新密码")
    private String password;
}
