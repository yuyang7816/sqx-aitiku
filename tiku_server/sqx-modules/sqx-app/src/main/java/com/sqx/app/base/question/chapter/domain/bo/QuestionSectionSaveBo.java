package com.sqx.app.base.question.chapter.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 管理端新增节
 */
@Data
public class QuestionSectionSaveBo {

    @Schema(description = "节业务编码（可选，留空自动生成）")
    private String sectionCode;

    @Schema(description = "所属章 id")
    private Long chapterId;

    @Schema(description = "所属章业务编码（与 chapterId 二选一，兼容旧接口）")
    private String chapterCode;

    @Schema(description = "节标题")
    @NotBlank(message = "节标题不能为空")
    private String title;

    @Schema(description = "题目数量")
    private Integer questionCount;

    @Schema(description = "排序")
    @NotNull(message = "排序不能为空")
    private Integer sortOrder;

    @Schema(description = "状态：1启用 2禁用")
    @NotNull(message = "状态不能为空")
    private Integer state;
}
