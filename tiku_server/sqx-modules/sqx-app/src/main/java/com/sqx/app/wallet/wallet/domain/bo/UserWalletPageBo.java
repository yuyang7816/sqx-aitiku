package com.sqx.app.wallet.wallet.domain.bo;

import com.sqx.common.mybatis.domain.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author 西安省钱兄网络科技有限公司
 * @since 2025-08-13 15:09:58
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UserWalletPageBo extends PageParam {

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户名（模糊）")
    private String userName;

    @Schema(description = "手机号")
    private String phone;
}
