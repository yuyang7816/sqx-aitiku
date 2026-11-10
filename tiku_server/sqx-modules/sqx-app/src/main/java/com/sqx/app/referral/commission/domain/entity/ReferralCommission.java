package com.sqx.app.referral.commission.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 佣金信息
 *
 * @author 西安省钱兄网络科技有限公司
 * @since 2025-08-04 14:44:40
 */
@Getter
@Setter
@TableName("referral_commission")
@Schema(name = "ReferralCommission", description = "佣金信息")
public class ReferralCommission implements Serializable {

    private static final long serialVersionUID = 1L;
    @Schema(description = "id")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "用户id")
    @TableField("user_id")
    private Long userId;

    @Schema(description = "订单id")
    @TableField("order_id")
    private Long orderId;

    @Schema(description = "佣金比例")
    @TableField("rate")
    private BigDecimal rate;

    @Schema(description = "佣金金额")
    @TableField("amount")
    private BigDecimal amount;

    @Schema(description = "类型 0: 提现 1: 订单佣金")
    private Integer type;

    @Schema(description = "资金流向: 1=收入, 2=支出")
    private Integer direction;

    @Schema(description = "交易标题")
    @TableField("title")
    private String title;

    @Schema(description = "交易描述")
    @TableField("description")
    private String description;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "update_time", fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;

}
