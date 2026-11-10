package com.sqx.app.base.question.manage.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 管理端新增题目
 */
@Data
public class QuestionSaveBo {

    @Schema(description = "练习入口：chapter/knowledge/special/exam")
    @NotBlank(message = "请选择练习入口")
    private String practiceEntry;

    @Schema(description = "学科")
    @NotBlank(message = "请选择学科")
    private String subject;

    @Schema(description = "年级")
    private String grade;

    @Schema(description = "教材版本（章节练习筛选用）")
    private String version;

    @Schema(description = "题目编号（留空则系统自动生成）")
    private String questionCode;

    @Schema(description = "所属章（章节练习必填）")
    private String chapterCode;

    @Schema(description = "所属章标题（批量导入时可替代 chapterCode）")
    private String chapterTitle;

    @Schema(description = "所属节（章节练习可选）")
    private String sectionCode;

    @Schema(description = "所属节标题（批量导入时可替代 sectionCode）")
    private String sectionTitle;

    @Schema(description = "知识点（知识点练习必填）")
    private String pointCode;

    @Schema(description = "知识点标题（批量导入时可替代 pointCode）")
    private String pointTitle;

    @Schema(description = "题集（专项/真题练习必填）")
    private String paperCode;

    @Schema(description = "题集标题（批量导入时可替代 paperCode）")
    private String paperTitle;

    @Schema(description = "题干")
    @NotBlank(message = "请输入题干")
    private String title;

    @Schema(description = "题型")
    @NotBlank(message = "请选择题型")
    private String questionType;

    @Schema(description = "难度")
    private String difficulty;

    @Schema(description = "正确答案")
    @NotBlank(message = "请输入正确答案")
    private String rightAnswer;

    @Schema(description = "解析")
    private String analysis;

    @Schema(description = "讲解视频地址")
    private String videoUrl;

    @Schema(description = "视频封面")
    private String videoPosterUrl;

    @Schema(description = "视频时长展示")
    private String videoDuration;

    @Schema(description = "讲解老师")
    private String videoTeacher;

    @Schema(description = "视频简介")
    private String videoSummary;

    @Schema(description = "选项列表")
    @Valid
    private List<QuestionOptionBo> options;

    @Schema(description = "标签列表")
    private List<String> tags;

    @Schema(description = "排序")
    @NotNull(message = "请输入排序")
    private Integer sortOrder;

    @Schema(description = "状态：1启用 2禁用")
    @NotNull(message = "请选择状态")
    private Integer state;
}
