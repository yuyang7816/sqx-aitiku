package com.sqx.app.wallet.transaction.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户钱包交易记录表
 *
 * @author 西安省钱兄网络科技有限公司
 * @since 2025-08-13 15:12:03
 */
@Getter
@Setter
@TableName("user_wallet_transaction")
@Schema(name = "UserWalletTransaction", description = "用户钱包交易记录表")
public class UserWalletTransaction implements Serializable {

    private static final long serialVersionUID = 1L;
    @Schema(description = "交易记录ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "用户ID")
    @TableField("user_id")
    private Long userId;

    @Schema(description = "交易类型: 1=充值, 2=消费, 3=退款, 5=系统调整 6=退保证金")
    @TableField("transaction_type")
    private Integer transactionType;

    @Schema(description = "交易金额（正数）")
    @TableField("amount")
    private BigDecimal amount;

    @Schema(description = "资金流向: 1=收入, 2=支出")
    @TableField("direction")
    private Integer direction;

    @Schema(description = "业务类型: 1=会员购买, 2=订单支付, 3=活动奖励, 5=退款 6=退保证金")
    @TableField("biz_type")
    private Integer bizType;

    @Schema(description = "关联业务ID（如订单ID、会员订单ID）")
    @TableField("biz_id")
    private Long bizId;

    @Schema(description = "状态: 1=待处理, 2=已完成, 3=已取消, 4=失败")
    @TableField("status")
    private Integer status;

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

    @Schema(description = "最后更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "update_time", fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;

    @Schema(description = "扩展信息（如支付方式、渠道等）")
    @TableField("extra_data")
    private String extraData;

}
