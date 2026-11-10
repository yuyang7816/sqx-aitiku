package com.sqx.app.referral.withdraw.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

/**
 * 用户端实名绑定提现账户入参（POST /app/withdrawalAccount）
 */
@Data
public class AppWithdrawalAccountBindBO {

    @Schema(description = "实名姓名（与身份证一致）")
    @NotBlank(message = "真实姓名不能为空")
    private String realName;

    @Schema(description = "身份证号（15位或18位）")
    @NotBlank(message = "身份证号不能为空")
    @Pattern(regexp = "^[1-9]\\d{5}(18|19|20)\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])\\d{3}[\\dXx]$|^[1-9]\\d{7}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])\\d{3}$",
            message = "请输入正确的身份证号")
    private String idCardNo;

    @Schema(description = "提现账户名称（如微信实名昵称）")
    @NotBlank(message = "账户名称不能为空")
    private String accountName;

    @Schema(description = "提现账户账号（如微信 openid/手机号）")
    @NotBlank(message = "账户账号不能为空")
    private String accountNumber;

    @Schema(description = "账户类型 1支付宝 2微信，默认2")
    @NotNull(message = "账户类型不能为空")
    private Integer type;
}
