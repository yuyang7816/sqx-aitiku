package com.sqx.app.base.notebook.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 管理端用户错题分页项（含用户信息）
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AdminUserNotebookItemVo extends AppNotebookItemVO {

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
}
