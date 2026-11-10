package com.sqx.app.base.notebook.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户端错题本列表查询入参（GET /app/notebook）
 */
@Data
public class AppNotebookQueryBO {

    @Schema(description = "学科筛选（精确匹配 subject 字段，如 数学 · 初三）")
    private String subject;

    @Schema(description = "掌握状态筛选：true 仅已掌握，false 仅未掌握，空则全部")
    private Boolean mastered;
}
