package com.sqx.app.base.question.manage.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sqx.app.base.question.paper.domain.vo.AppQuestionOptionVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 管理端题目 VO（含章/节关联业务字段）
 */
@Data
public class QuestionVo {

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "练习入口：chapter/knowledge/special/exam")
    private String practiceEntry;

    @Schema(description = "练习入口名称")
    private String practiceEntryLabel;

    @Schema(description = "绑定目标展示（章/知识点/题集名称）")
    private String bindTarget;

    @Schema(description = "知识点编码")
    private String pointCode;

    @Schema(description = "知识点名称")
    private String pointTitle;

    @Schema(description = "题集编码")
    private String paperCode;

    @Schema(description = "题集名称")
    private String paperTitle;

    @Schema(description = "题目编号")
    private String questionCode;

    @Schema(description = "所属章 id")
    private Long chapterId;

    @Schema(description = "所属章编码")
    private String chapterCode;

    @Schema(description = "所属章标题")
    private String chapterTitle;

    @Schema(description = "所属节 id")
    private Long sectionId;

    @Schema(description = "所属节编码")
    private String sectionCode;

    @Schema(description = "所属节标题")
    private String sectionTitle;

    @Schema(description = "年级")
    private String grade;

    @Schema(description = "学科")
    private String subject;

    @Schema(description = "教材版本")
    private String version;

    @Schema(description = "题干")
    private String title;

    @Schema(description = "题型")
    private String questionType;

    @Schema(description = "难度")
    private String difficulty;

    @Schema(description = "正确答案")
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
    private List<AppQuestionOptionVO> options;

    @Schema(description = "标签列表")
    private List<String> tags;

    @Schema(description = "章内排序")
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
