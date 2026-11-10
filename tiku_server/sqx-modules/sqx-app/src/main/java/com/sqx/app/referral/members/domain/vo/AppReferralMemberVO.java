package com.sqx.app.referral.members.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 推广下级成员列表项（GET /app/referral/members 出参，兼容 uniapp promoMembers）
 */
@Getter
@Setter
@Schema(description = "推广下级成员列表项")
public class AppReferralMemberVO {

    @Schema(description = "展示名（昵称或脱敏手机号/用户名）")
    private String name;

    @Schema(description = "头像 URL")
    private String avatar;

    @Schema(description = "注册日期（yyyy-MM-dd）")
    private String time;

    @Schema(description = "付费状态：已付费 / 未付费")
    private String status;

    @Schema(description = "该下级贡献的分佣金额（元，仅 withCommission=1 时返回）")
    private BigDecimal commission;
}
