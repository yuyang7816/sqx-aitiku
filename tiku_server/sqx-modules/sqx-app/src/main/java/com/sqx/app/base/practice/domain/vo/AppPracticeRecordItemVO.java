package com.sqx.app.base.practice.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户端练习记录条目（GET /app/practice/records 出参项）
 */
@Data
public class AppPracticeRecordItemVO {

    @Schema(description = "练习记录业务编码")
    private String recordCode;

    @Schema(description = "章业务编码（chapter_code）")
    private String chapterId;

    @Schema(description = "章标题")
    private String chapterTitle;

    @Schema(description = "练习来源：all/wrong/single")
    private String sourceMode;

    @Schema(description = "学科展示文案")
    private String subject;

    @Schema(description = "本次答题总数")
    private Integer totalCount;

    @Schema(description = "答对题数")
    private Integer correctCount;

    @Schema(description = "答错题数")
    private Integer wrongCount;

    @Schema(description = "得分（0~100）")
    private Integer score;

    @Schema(description = "答题耗时（秒）")
    private Integer durationSeconds;

    @Schema(description = "提交时间（yyyy-MM-dd HH:mm:ss）")
    private String submitTime;
}
