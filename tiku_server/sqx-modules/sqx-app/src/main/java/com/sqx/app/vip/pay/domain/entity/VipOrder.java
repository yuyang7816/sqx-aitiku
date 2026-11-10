package com.sqx.app.vip.pay.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 会员支付订单（tb_vip_order）
 */
@Getter
@Setter
@TableName("tb_vip_order")
@Schema(name = "VipOrder", description = "会员支付订单")
public class VipOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 待支付 */
    public static final int STATUS_PENDING = 0;
    /** 已支付 */
    public static final int STATUS_PAID = 1;
    /** 已关闭 */
    public static final int STATUS_CLOSED = 2;

    @Schema(description = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "业务订单号")
    @TableField("order_no")
    private String orderNo;

    @Schema(description = "用户 ID")
    @TableField("user_id")
    private Long userId;

    @Schema(description = "套餐名称")
    @TableField("plan_name")
    private String planName;

    @Schema(description = "有效天数")
    @TableField("plan_days")
    private Integer planDays;

    @Schema(description = "订单金额（元）")
    @TableField("amount")
    private BigDecimal amount;

    @Schema(description = "支付渠道 wechat/alipay")
    @TableField("pay_channel")
    private String payChannel;

    @Schema(description = "支付状态 0待支付 1已支付 2已关闭")
    @TableField("pay_status")
    private Integer payStatus;

    @Schema(description = "第三方支付流水号")
    @TableField("trade_no")
    private String tradeNo;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @Schema(description = "支付成功时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("pay_time")
    private LocalDateTime payTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "update_time", fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;
}
