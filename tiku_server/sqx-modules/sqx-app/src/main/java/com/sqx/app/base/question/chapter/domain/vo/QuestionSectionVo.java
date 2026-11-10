package com.sqx.app.base.question.chapter.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理端节 VO（含所属章业务字段）
 */
@Data
public class QuestionSectionVo {

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "节业务编码")
    private String sectionCode;

    @Schema(description = "所属章 id")
    private Long chapterId;

    @Schema(description = "所属章业务编码")
    private String chapterCode;

    @Schema(description = "所属章标题")
    private String chapterTitle;

    @Schema(description = "节标题")
    private String title;

    @Schema(description = "题目数量")
    private Integer questionCount;

    @Schema(description = "排序")
    private Integer sortOrder;

    @Schema(description = "状态：1启用 2禁用")
    private Integer state;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
