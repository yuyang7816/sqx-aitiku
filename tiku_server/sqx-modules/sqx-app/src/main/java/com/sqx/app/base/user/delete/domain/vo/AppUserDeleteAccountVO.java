package com.sqx.app.base.user.delete.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户端账号注销状态/申请结果
 */
@Data
@Schema(description = "账号注销状态")
public class AppUserDeleteAccountVO {

    @Schema(description = "是否已执行注销（冷静期内为 false）")
    private Boolean deleted;

    @Schema(description = "是否处于注销冷静期")
    private Boolean pending;

    @Schema(description = "预计执行注销时间 yyyy-MM-dd HH:mm:ss")
    private String effectiveAt;

    @Schema(description = "冷静期总天数")
    private Integer coolingDays;

    @Schema(description = "剩余天数（向上取整，最少 0）")
    private Integer remainingDays;
}
