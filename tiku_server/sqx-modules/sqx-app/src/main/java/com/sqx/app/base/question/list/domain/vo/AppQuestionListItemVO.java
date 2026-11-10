package com.sqx.app.base.question.list.domain.vo;

import com.sqx.app.base.question.paper.domain.vo.AppQuestionOptionVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 用户端练习题目列表项（GET /app/question/list 出参）
 * 章节列表用 no/text/status/type；答题页用 questionType/options/rightAnswer/analysis/tags
 */
@Schema(description = "题目列表项（章节详情 + 答题页共用，含做题状态与完整题目详情）")
@Data
public class AppQuestionListItemVO {

    @Schema(description = "展示序号（如 01、02）")
    private String no;

    @Schema(description = "题目主键")
    private Long questionId;

    @Schema(description = "题目业务编码")
    private String questionCode;

    @Schema(description = "题干（章节列表兼容字段）")
    private String text;

    @Schema(description = "题干（答题页字段，与 text 一致）")
    private String title;

    @Schema(description = "做题状态文案：已做对 / 已做错 / 未做")
    private String status;

    @Schema(description = "做题状态类型：right / wrong / todo")
    private String type;

    @Schema(description = "题型（如单选题）")
    private String questionType;

    @Schema(description = "难度")
    private String difficulty;

    @Schema(description = "正确答案")
    private String rightAnswer;

    @Schema(description = "解析")
    private String analysis;

    @Schema(description = "选项列表")
    private List<AppQuestionOptionVO> options;

    @Schema(description = "标签列表")
    private List<String> tags;
}
