package com.sqx.app.base.user.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author chengming
 * @since 2025/4/24
 */
@Data
public class ForgetPasswordBO {

    @Schema(description = "用户id", hidden = true)
    private Long userId;

    @Schema(description = "验证码")
    private String code;

    @Schema(description = "新密码")
    private String password;
}
