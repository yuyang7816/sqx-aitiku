package com.sqx.app.wallet.wallet.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author 西安省钱兄网络科技有限公司
 * @since 2025-08-13 15:09:58
 */
@Data
public class UserWalletSaveBo {

    @Schema(description = "用户ID，全局唯一")
    private Long userId;

    @Schema(description = "钱包余额（单位：元）")
    private BigDecimal amount;
}
