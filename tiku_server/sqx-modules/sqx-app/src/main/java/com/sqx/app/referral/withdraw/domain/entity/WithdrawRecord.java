package com.sqx.app.referral.withdraw.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 提现记录
 *
 * @author 西安省钱兄网络科技有限公司
 * @since 2025-08-05 10:10:32
 */
@Getter
@Setter
@TableName("withdraw_record")
@Schema(name = "WithdrawRecord", description = "提现记录")
public class WithdrawRecord implements Serializable {

    private static final long serialVersionUID = 1L;
    @Schema(description = "id")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "用户id")
    @TableField("user_id")
    private Long userId;

    @Schema(description = "金额")
    @TableField("amount")
    private BigDecimal amount;

    @Schema(description = "实际到账金额")
    private BigDecimal actualAmount;

    @Schema(description = "手续费")
    private BigDecimal serviceFee;

    @Schema(description = "提现账户 1微信 2支付宝")
    private Integer accountType;

    @Schema(description = "状态 1发起提现 2提现成功 3拒绝提现")
    @TableField("status")
    private Integer status;

    @Schema(description = "拒绝理由")
    @TableField("reason")
    private String reason;

    @Schema(description = "转账时间")
    @TableField("withdraw_time")
    private LocalDateTime withdrawTime;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "update_time", fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;

}
