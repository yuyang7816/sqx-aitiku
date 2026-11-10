package com.sqx.app.base.question.paper.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 管理端新增题集
 */
@Data
public class QuestionPaperSaveBo {

    @Schema(description = "题集业务编码（可选，留空自动生成）")
    private String paperCode;

    @Schema(description = "题集标题")
    @NotBlank(message = "题集标题不能为空")
    private String title;

    @Schema(description = "类型：special专项 exam真题")
    @NotBlank(message = "题集类型不能为空")
    private String paperType;

    @Schema(description = "简介")
    private String description;

    @Schema(description = "年级")
    @NotBlank(message = "年级不能为空")
    private String grade;

    @Schema(description = "年级简称")
    private String gradeShort;

    @Schema(description = "学科")
    @NotBlank(message = "学科不能为空")
    private String subject;

    @Schema(description = "排序")
    @NotNull(message = "排序不能为空")
    private Integer sortOrder;

    @Schema(description = "状态：1启用 2禁用")
    @NotNull(message = "状态不能为空")
    private Integer state;

    @Schema(description = "绑定题目列表")
    @Valid
    private List<QuestionPaperItemSaveBo> items;
}
