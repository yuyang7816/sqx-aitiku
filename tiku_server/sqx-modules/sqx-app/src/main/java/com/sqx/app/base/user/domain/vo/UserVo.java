package com.sqx.app.base.user.domain.vo;

import com.sqx.app.base.user.domain.entity.UserEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author chengming
 * @since 2025/10/20
 */
@Data
public class UserVo extends UserEntity {

    @Schema(description = "钱包 ID")
    private Long walletId;

    @Schema(description = "钱包余额（元）")
    private BigDecimal walletAmount;

    @Schema(description = "上级用户 ID")
    private Long superiorUserId;

    @Schema(description = "上级展示名")
    private String superiorDisplayName;

    @Schema(description = "提现账户摘要（列表展示）")
    private String withdrawalAccountSummary;

    @Schema(description = "直接下级人数")
    private Integer directMemberCount;
}
