package com.sqx.app.base.auth.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author chengming
 * @since 2025/4/22
 */
@Data
public class PhoneCodeLoginBO {

    @Schema(description = "手机号")
    private String phoneNo;

    @Schema(description = "验证码")
    private String verifyCode;
}
