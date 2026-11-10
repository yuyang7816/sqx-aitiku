package com.sqx.app.referral.withdraw.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author chengming
 * @since 2025/8/5
 */
@Data
public class WithdrawRecordConfirmBo {

    private Long id;

    @Schema(description = "状态 1发起提现 2提现成功 3拒绝提现")
    private Integer status;

    @Schema(description = "拒绝理由")
    private String reason;
}
