package com.sqx.app.base.question.manage.domain.bo;

import com.sqx.common.mybatis.domain.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 管理端题目分页查询
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class QuestionPageBo extends PageParam {

    @Schema(description = "题目业务编码")
    private String questionCode;

    @Schema(description = "练习入口：chapter/knowledge/special/exam")
    private String practiceEntry;

    @Schema(description = "题干")
    private String title;

    @Schema(description = "题型")
    private String questionType;

    @Schema(description = "难度")
    private String difficulty;

    @Schema(description = "标签关键字")
    private String tagKeyword;

    @Schema(description = "所属章业务编码")
    private String chapterCode;

    @Schema(description = "所属章标题")
    private String chapterTitle;

    @Schema(description = "所属节业务编码")
    private String sectionCode;

    @Schema(description = "所属节标题")
    private String sectionTitle;

    @Schema(description = "年级（筛章）")
    private String grade;

    @Schema(description = "学科（筛章）")
    private String subject;

    @Schema(description = "教材版本（筛章）")
    private String version;

    @Schema(description = "状态：1启用 2禁用")
    private Integer state;
}
