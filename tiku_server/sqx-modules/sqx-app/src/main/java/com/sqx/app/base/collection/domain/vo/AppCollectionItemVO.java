package com.sqx.app.base.collection.domain.vo;

import com.sqx.app.base.question.paper.domain.vo.AppQuestionOptionVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 用户端收藏列表项（GET /app/collection 出参，兼容 uniapp collection.vue）
 */
@Data
public class AppCollectionItemVO {

    @Schema(description = "收藏展示 id（question_code，供前端 key 使用）")
    private String id;

    @Schema(description = "收藏记录主键")
    private Long collectionId;

    @Schema(description = "题目主键")
    private Long questionId;

    @Schema(description = "题目业务编码")
    private String questionCode;

    @Schema(description = "学科展示文案")
    private String subject;

    @Schema(description = "收藏日期（yyyy-MM-dd）")
    private String date;

    @Schema(description = "题干")
    private String title;

    @Schema(description = "用户作答（若有答题记录）")
    private String myAnswer;

    @Schema(description = "正确答案")
    private String rightAnswer;

    @Schema(description = "解析")
    private String analysis;

    @Schema(description = "AI 讲解摘要")
    private String aiSummary;

    @Schema(description = "选项列表")
    private List<AppQuestionOptionVO> options;

    @Schema(description = "标签列表")
    private List<String> tags;
}
