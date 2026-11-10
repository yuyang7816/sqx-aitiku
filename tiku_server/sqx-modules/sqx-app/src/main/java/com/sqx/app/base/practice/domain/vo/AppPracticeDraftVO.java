package com.sqx.app.base.practice.domain.vo;

import com.sqx.app.base.practice.domain.bo.AppPracticeDraftAnswerBO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Collections;
import java.util.List;

/**
 * 用户端练习答题暂存出参（GET /app/practice/draft）
 */
@Data
public class AppPracticeDraftVO {

    @Schema(description = "是否存在可恢复的暂存进度")
    private Boolean hasDraft;

    @Schema(description = "章业务编码")
    private String chapterId;

    @Schema(description = "题集业务编码")
    private String paperId;

    @Schema(description = "单题预览时的题目业务编码")
    private String questionId;

    @Schema(description = "练习来源：all/wrong/single")
    private String sourceMode;

    @Schema(description = "学科展示文案")
    private String subject;

    @Schema(description = "当前题号索引（0 起）")
    private Integer currentIndex;

    @Schema(description = "已累计答题耗时（秒）")
    private Integer durationSeconds;

    @Schema(description = "已暂存答案明细")
    private List<AppPracticeDraftAnswerBO> answers;

    @Schema(description = "最近暂存时间（yyyy-MM-dd HH:mm:ss）")
    private String savedAt;

    public static AppPracticeDraftVO empty() {
        AppPracticeDraftVO vo = new AppPracticeDraftVO();
        vo.setHasDraft(false);
        vo.setCurrentIndex(0);
        vo.setDurationSeconds(0);
        vo.setAnswers(Collections.emptyList());
        return vo;
    }
}
