package com.sqx.app.base.practice.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 用户端练习答题暂存入参（POST /app/practice/draft）
 */
@Data
public class AppPracticeDraftSaveBO {

    @Schema(description = "章业务编码（chapter_code），与 paperId 至少填一项")
    private String chapterId;

    @Schema(description = "题集业务编码（paper_code），专项/真题练习时使用")
    private String paperId;

    @Schema(description = "单题预览时的题目业务编码（sourceMode=single 时使用）")
    private String questionId;

    @Schema(description = "练习来源：all/wrong/single，缺省 all")
    private String sourceMode;

    @Schema(description = "学科展示文案（如 数学 · 初三），可选")
    private String subject;

    @Schema(description = "当前题号索引（0 起）")
    @NotNull(message = "currentIndex 不能为空")
    @Min(value = 0, message = "currentIndex 不能为负数")
    private Integer currentIndex;

    @Schema(description = "已累计答题耗时（秒），缺省为 0")
    @Min(value = 0, message = "durationSeconds 不能为负数")
    private Integer durationSeconds;

    @Schema(description = "已作答明细（可部分为空答案，仅恢复有作答的题目）")
    @Valid
    private List<AppPracticeDraftAnswerBO> answers;
}
