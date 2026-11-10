package com.sqx.app.base.practice.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户端练习提交结果（POST /app/practice/submit 出参）
 */
@Data
public class AppPracticeSubmitResultVO {

    @Schema(description = "练习记录业务编码")
    private String recordCode;

    @Schema(description = "成功持久化的答题条数")
    private Integer savedCount;

    @Schema(description = "本次答题总数")
    private Integer totalCount;

    @Schema(description = "答对题数")
    private Integer correctCount;

    @Schema(description = "答错题数")
    private Integer wrongCount;

    @Schema(description = "得分（0~100，服务端判分）")
    private Integer score;

    @Schema(description = "答题耗时（秒）")
    private Integer durationSeconds;

    @Schema(description = "练习来源：all/wrong/single")
    private String sourceMode;

    @Schema(description = "提交后最新章节练习进度")
    private AppPracticeProgressVO progress;
}
