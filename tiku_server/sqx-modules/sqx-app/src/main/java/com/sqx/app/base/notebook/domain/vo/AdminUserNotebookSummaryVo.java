package com.sqx.app.base.notebook.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理端：有错题的用户汇总（每用户一行）
 */
@Data
public class AdminUserNotebookSummaryVo {

    @Schema(description = "用户 ID")
    private Long userId;

    @Schema(description = "用户名")
    private String userName;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "年级简称")
    private String gradeShort;

    @Schema(description = "错题总数")
    private Integer wrongTotal;

    @Schema(description = "待复习数")
    private Integer pendingCount;

    @Schema(description = "已掌握数")
    private Integer masteredCount;

    @Schema(description = "涉及学科（逗号分隔）")
    private String subjects;

    @Schema(description = "最近练习时间")
    private LocalDateTime lastPracticeAt;
}
