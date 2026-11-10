package com.sqx.app.base.question.video.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 题目讲解视频出参（GET /app/question/video）
 */
@Data
public class AppQuestionVideoVO {

    @Schema(description = "题目标题")
    private String title;

    @Schema(description = "题目业务编码")
    private String questionCode;

    @Schema(description = "视频播放地址")
    private String videoUrl;

    @Schema(description = "封面图")
    private String posterUrl;

    @Schema(description = "时长文案")
    private String duration;

    @Schema(description = "讲师")
    private String teacher;

    @Schema(description = "讲解摘要")
    private String summary;

    @Schema(description = "是否命中服务端配置/题库")
    private Boolean fromServer;
}
