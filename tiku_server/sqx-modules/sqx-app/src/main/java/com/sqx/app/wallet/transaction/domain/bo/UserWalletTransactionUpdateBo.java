package com.sqx.app.wallet.transaction.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author 西安省钱兄网络科技有限公司
 * @since 2025-08-13 15:12:03
 */
@Data
public class UserWalletTransactionUpdateBo {

    @Schema(description = "交易记录ID")
    private Long id;
    @Schema(description = "用户ID")
    private Long userId;
    @Schema(description = "交易类型: 1=充值, 2=消费, 3=退款, 4=提现, 5=系统调整")
    private Byte transactionType;
    @Schema(description = "交易金额（正数）")
    private BigDecimal amount;
    @Schema(description = "资金流向: 1=收入, 2=支出")
    private Byte direction;
    @Schema(description = "业务类型: 1=会员购买, 2=订单支付, 3=活动奖励, 4=提现, 5=退款")
    private Byte bizType;
    @Schema(description = "关联业务ID（如订单ID、会员订单ID）")
    private Long bizId;
    @Schema(description = "状态: 1=待处理, 2=已完成, 3=已取消, 4=失败")
    private Byte status;
    @Schema(description = "交易标题")
    private String title;
    @Schema(description = "交易描述")
    private String description;
    @Schema(description = "扩展信息（如支付方式、渠道等）")
    private String extraData;
}
