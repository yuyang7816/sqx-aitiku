package com.sqx.app.base.practice.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 用户端练习提交入参（POST /app/practice/submit）
 */
@Data
public class AppPracticeSubmitBO {

    @Schema(description = "章业务编码（chapter_code）")
    @NotBlank(message = "chapterId 不能为空")
    private String chapterId;

    @Schema(description = "本次提交的答题明细（至少一题）")
    @NotEmpty(message = "答题明细不能为空")
    @Valid
    private List<AppPracticeSubmitAnswerBO> answers;

    @Schema(description = "答题耗时（秒），缺省为 0")
    @Min(value = 0, message = "durationSeconds 不能为负数")
    private Integer durationSeconds;

    @Schema(description = "练习来源：all/wrong/single，缺省 all")
    private String sourceMode;

    @Schema(description = "学科展示文案（如 数学 · 初三），可选")
    private String subject;
}
