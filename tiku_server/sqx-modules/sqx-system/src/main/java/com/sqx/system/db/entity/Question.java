package com.sqx.system.db.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 题库-题目
 *
 * @author sqx
 */
@Getter
@Setter
@TableName("tb_question")
@Schema(name = "Question", description = "题库-题目")
public class Question implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "题目业务编码")
    @TableField("question_code")
    private String questionCode;

    @Schema(description = "所属章 id")
    @TableField("chapter_id")
    private Long chapterId;

    @Schema(description = "所属节 id")
    @TableField("section_id")
    private Long sectionId;

    @Schema(description = "题干")
    @TableField("title")
    private String title;

    @Schema(description = "题型")
    @TableField("question_type")
    private String questionType;

    @Schema(description = "难度")
    @TableField("difficulty")
    private String difficulty;

    @Schema(description = "正确答案")
    @TableField("right_answer")
    private String rightAnswer;

    @Schema(description = "解析")
    @TableField("analysis")
    private String analysis;

    @Schema(description = "讲解视频地址")
    @TableField("video_url")
    private String videoUrl;

    @Schema(description = "视频封面")
    @TableField("video_poster_url")
    private String videoPosterUrl;

    @Schema(description = "视频时长展示")
    @TableField("video_duration")
    private String videoDuration;

    @Schema(description = "讲解老师")
    @TableField("video_teacher")
    private String videoTeacher;

    @Schema(description = "视频简介")
    @TableField("video_summary")
    private String videoSummary;

    @Schema(description = "选项 JSON")
    @TableField("options_json")
    private String optionsJson;

    @Schema(description = "标签 JSON")
    @TableField("tags_json")
    private String tagsJson;

    @Schema(description = "章内排序")
    @TableField("sort_order")
    private Integer sortOrder;

    @Schema(description = "状态：1启用 2禁用")
    @TableField("state")
    private Integer state;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "update_time", fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;
}
