package com.sqx.app.base.question.chapter.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 用户端章节树-章节点（code/title/sort/questionCount/gradeShort/sections）
 */
@Schema(description = "章节树-章节点（业务键 code 对应 uniapp 章节 id）")
@Data
public class AppQuestionChapterNodeVO {

    @Schema(description = "章编码")
    private String code;

    @Schema(description = "章标题")
    private String title;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "题目数量")
    private Integer questionCount;

    @Schema(description = "年级简称")
    private String gradeShort;

    @Schema(description = "节列表")
    private List<AppQuestionSectionNodeVO> sections;
}
