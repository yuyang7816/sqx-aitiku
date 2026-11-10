package com.sqx.app.base.notebook.domain.vo;

import com.sqx.app.base.question.paper.domain.vo.AppQuestionOptionVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 用户端错题本列表项（GET /app/notebook 出参，对齐 uniapp normalizeNotebookItem）
 */
@Data
public class AppNotebookItemVO {

    @Schema(description = "展示 id（question_code，供前端 key 使用）")
    private String id;

    @Schema(description = "错题记录主键")
    private Long notebookId;

    @Schema(description = "题目主键")
    private Long questionId;

    @Schema(description = "题目业务编码")
    private String questionCode;

    @Schema(description = "学科展示文案")
    private String subject;

    @Schema(description = "收录日期（yyyy-MM-dd）")
    private String date;

    @Schema(description = "题干")
    private String title;

    @Schema(description = "题干（uniapp 兼容字段 question）")
    private String question;

    @Schema(description = "用户错误答案")
    private String myAnswer;

    @Schema(description = "用户错误答案（uniapp 兼容字段 wrong）")
    private String wrong;

    @Schema(description = "正确答案")
    private String rightAnswer;

    @Schema(description = "正确答案（uniapp 兼容字段 right）")
    private String right;

    @Schema(description = "解析")
    private String analysis;

    @Schema(description = "AI 讲解摘要")
    private String aiSummary;

    @Schema(description = "选项列表")
    private List<AppQuestionOptionVO> options;

    @Schema(description = "标签列表")
    private List<String> tags;

    @Schema(description = "累计做错次数")
    private Integer wrongCount;

    @Schema(description = "是否已掌握")
    private Boolean mastered;

    @Schema(description = "最近练习时间（yyyy-MM-dd HH:mm:ss）")
    private String lastPracticeAt;
}
