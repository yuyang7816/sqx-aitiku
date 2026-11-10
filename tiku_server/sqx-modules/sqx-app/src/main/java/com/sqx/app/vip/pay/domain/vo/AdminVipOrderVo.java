package com.sqx.app.vip.pay.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sqx.app.vip.pay.domain.entity.VipOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
public class AdminVipOrderVo extends VipOrder {

    @Schema(description = "用户名")
    private String userName;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "会员到期时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime userVipExpireAt;
}
