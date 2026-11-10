package com.sqx.app.base.practice.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户端练习答题暂存保存结果（POST /app/practice/draft 出参）
 */
@Data
public class AppPracticeDraftSaveResultVO {

    @Schema(description = "是否保存成功")
    private Boolean saved;

    @Schema(description = "已暂存答案题数")
    private Integer answerCount;

    @Schema(description = "最近暂存时间（yyyy-MM-dd HH:mm:ss）")
    private String savedAt;
}
