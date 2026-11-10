package com.sqx.app.base.practice.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户端练习记录分页查询（GET /app/practice/records）
 */
@Data
public class AppPracticeRecordQueryBO {

    @Schema(description = "页码，默认 1")
    private Long page;

    @Schema(description = "每页条数，默认 10")
    private Long limit;

    @Schema(description = "章业务编码（chapter_code）筛选，可选")
    private String chapterId;

    @Schema(description = "练习来源筛选：all/wrong/single，可选")
    private String sourceMode;
}
