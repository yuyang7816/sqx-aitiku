package com.sqx.app.referral.withdraw.domain.bo;

import com.sqx.common.mybatis.domain.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author 西安省钱兄网络科技有限公司
 * @since 2025-08-05 10:10:32
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class WithdrawRecordPageBo extends PageParam {

    @Schema(description = "用户id")
    private Long userId;

    @Schema(description = "申请人手机号")
    private String phone;

    @Schema(description = "申请人用户名")
    private String userName;

    @Schema(description = "状态 1发起提现 2提现成功 3拒绝提现")
    private Integer status;
}
