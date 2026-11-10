package com.sqx.app.base.search.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 用户端搜题记录列表项（GET /app/search/records 出参，兼容 uniapp aiQuestionSearchRecords）
 */
@Getter
@Setter
@Schema(description = "搜题记录列表项")
public class AppSearchRecordItemVO {

    @Schema(description = "题干预览")
    private String title;

    @Schema(description = "展示用相对时间（如刚刚、今天 10:24）")
    private String time;

    @Schema(description = "来源：拍照搜题/手动搜题/AI答疑")
    private String source;

    @Schema(description = "原图地址（拍照搜题可选）")
    private String imageUrl;
}
