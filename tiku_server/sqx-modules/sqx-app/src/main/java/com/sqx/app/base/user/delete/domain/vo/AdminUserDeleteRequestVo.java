package com.sqx.app.base.user.delete.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理端注销申请列表项
 */
@Data
@Schema(description = "管理端注销申请")
public class AdminUserDeleteRequestVo {

    private Long id;
    private Long userId;
    private String userName;
    private String nickname;
    private String phone;
    private String status;
    private String statusLabel;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime requestTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime effectiveTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime cancelTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime completeTime;

    private String adminRemark;
    private Integer remainingDays;
}
