package com.sqx.app.wallet.wallet.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author 西安省钱兄网络科技有限公司
 * @since 2025-08-13 15:09:58
 */
@Data
public class UserWalletInfoBo {

    @Schema(description = "钱包ID")
    @NotNull(message = "id不能为空")
    private Long id;

}
