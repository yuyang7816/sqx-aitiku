package com.sqx.app.base.notebook.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户端错题本写入结果（POST /app/notebook 出参）
 */
@Data
public class AppNotebookAddResultVO {

    @Schema(description = "是否新增成功（重复写入时为 false）")
    private Boolean added;

    @Schema(description = "是否为重复题目（已存在错题记录）")
    private Boolean duplicate;

    @Schema(description = "错题详情")
    private AppNotebookItemVO item;
}
