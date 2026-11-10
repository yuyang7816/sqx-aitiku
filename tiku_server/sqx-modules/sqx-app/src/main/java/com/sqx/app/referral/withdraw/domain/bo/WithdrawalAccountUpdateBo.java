package com.sqx.app.referral.withdraw.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author 西安省钱兄网络科技有限公司
 * @since 2025-06-20 14:02:39
 */
@Data
public class WithdrawalAccountUpdateBo {

    @Schema(description = "id")
    private Long id;

    @Schema(description = "用户id")
    private Long userId;

    @Schema(description = "账户名称")
    private String accountName;

    @Schema(description = "账户账号")
    private String accountNumber;

    @Schema(description = "账户类型 1支付宝 2微信")
    private Integer type;
}
