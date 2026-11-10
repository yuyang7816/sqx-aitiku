package com.sqx.app.referral.withdraw.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户端实名绑定提现账户出参
 */
@Data
public class AppWithdrawalAccountBindResultVO {

    @Schema(description = "提现账户 id")
    private Long id;

    @Schema(description = "实名姓名")
    private String realName;

    @Schema(description = "脱敏身份证号")
    private String idCardNoMasked;

    @Schema(description = "账户名称")
    private String accountName;

    @Schema(description = "账户账号")
    private String accountNumber;

    @Schema(description = "账户类型 1支付宝 2微信")
    private Integer type;

    @Schema(description = "账户类型名称")
    private String typeName;

    @Schema(description = "是否已完成实名认证 0否 1是")
    private Integer realNameVerified;

    @Schema(description = "绑定时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
