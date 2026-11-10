package com.sqx.app.referral.withdraw.domain.bo;

import com.sqx.common.mybatis.domain.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author 西安省钱兄网络科技有限公司
 * @since 2025-06-20 14:02:39
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class WithdrawalAccountPageBo extends PageParam {

    @Schema(description = "用户id")
    private Long userId;

    @Schema(description = "用户名（模糊）")
    private String userName;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "账户名称")
    private String accountName;

    @Schema(description = "账户账号")
    private String accountNumber;

    @Schema(description = "账户类型 1支付宝 2微信")
    private Integer type;

    @Schema(description = "管理端查询传 1")
    private Integer source;
}
