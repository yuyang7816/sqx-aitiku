package com.sqx.app.base.practice.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户端章节练习进度（GET /app/practice/progress 出参，与 uniapp 章节卡片 meta 对齐）
 */
@Data
public class AppPracticeProgressVO {

    @Schema(description = "章业务编码（chapter_code）")
    private String chapterId;

    @Schema(description = "章标题（如 第一章：有理数）")
    private String chapterTitle;

    @Schema(description = "已学百分比（0~100，四舍五入）")
    private Integer percent;

    @Schema(description = "已做题目数")
    private Integer doneCount;

    @Schema(description = "章节启用题目总数")
    private Integer totalCount;
}
