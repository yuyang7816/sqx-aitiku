package com.sqx.app.base.notebook.domain.bo;

import com.sqx.common.mybatis.domain.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class AdminUserNotebookPageBo extends PageParam {

    @Schema(description = "用户 ID")
    private Long userId;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "用户名关键词")
    private String userName;

    @Schema(description = "学科")
    private String subject;

    @Schema(description = "是否已掌握")
    private Boolean mastered;

    @Schema(description = "掌握状态查询值（内部使用）", hidden = true)
    private Integer masteredFlag;
}
