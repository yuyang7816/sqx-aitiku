package com.sqx.app.wallet.transaction.domain.bo;

import com.sqx.common.mybatis.domain.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author 西安省钱兄网络科技有限公司
 * @since 2025-08-13 15:12:03
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UserWalletTransactionPageBo extends PageParam {

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户名（模糊）")
    private String userName;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "标题（模糊）")
    private String title;

    @Schema(description = "交易类型")
    private Integer transactionType;

    @Schema(description = "资金流向")
    private Integer direction;

    @Schema(description = "状态")
    private Integer status;
}
