package com.sqx.app.base.question.chapter.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 管理端新增章
 */
@Data
public class QuestionChapterSaveBo {

    @Schema(description = "章业务编码（可选，留空自动生成）")
    private String chapterCode;

    @Schema(description = "章标题")
    @NotBlank(message = "章标题不能为空")
    private String title;

    @Schema(description = "年级")
    @NotBlank(message = "年级不能为空")
    private String grade;

    @Schema(description = "年级简称")
    private String gradeShort;

    @Schema(description = "学科")
    @NotBlank(message = "学科不能为空")
    private String subject;

    @Schema(description = "教材版本")
    @NotBlank(message = "教材版本不能为空")
    private String version;

    @Schema(description = "题目数量")
    private Integer questionCount;

    @Schema(description = "排序")
    @NotNull(message = "排序不能为空")
    private Integer sortOrder;

    @Schema(description = "状态：1启用 2禁用")
    @NotNull(message = "状态不能为空")
    private Integer state;
}
