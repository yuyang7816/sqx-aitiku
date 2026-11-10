package com.sqx.app.base.verifyCode.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author chengming
 * @since 2025/4/24
 */
@Data
public class SendEmailCodeBO {
    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "业务码")
    private String bizCode;
}
