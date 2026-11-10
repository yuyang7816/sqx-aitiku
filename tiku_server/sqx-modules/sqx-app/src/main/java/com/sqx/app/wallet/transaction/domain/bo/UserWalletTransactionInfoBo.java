package com.sqx.app.wallet.transaction.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author 西安省钱兄网络科技有限公司
 * @since 2025-08-13 15:12:03
 */
@Data
public class UserWalletTransactionInfoBo {

    @Schema(description = "交易记录ID")
    @NotNull(message = "id不能为空")
    private Long id;

}
