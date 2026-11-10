package com.sqx.app.vip.plan.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 会员套餐
 *
 * @author 西安省钱兄网络科技有限公司
 * @since 2025-08-07 16:51:13
 */
@Getter
@Setter
@TableName("vip_plan")
@Schema(name = "VipPlan", description = "会员套餐	")
public class VipPlan implements Serializable {

    private static final long serialVersionUID = 1L;
    @Schema(description = "id")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "会员名称")
    @TableField("name")
    private String name;

    @Schema(description = "月数")
    @TableField("month")
    private Integer month;

    @Schema(description = "会员价格")
    @TableField("amount")
    private BigDecimal amount;

    @Schema(description = "原价")
    @TableField("original_amount")
    private BigDecimal originalAmount;

    @Schema(description = "排序")
    @TableField("sort")
    private Integer sort;

    @Schema(description = "状态：1启用 0禁用")
    @TableField("status")
    private Integer status;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "update_time", fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;

}
