package com.sqx.app.base.practiceEntry.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "刷题中心练习入口卡片")
public class AppPracticeEntryItemVO {

    @Schema(description = "标题")
    private String title;

    @Schema(description = "副标题")
    private String sub;

    @Schema(description = "图标图片路径")
    private String icon;

    @Schema(description = "图标颜色（历史字段）")
    private String iconColor;

    @Schema(description = "卡片背景色")
    private String bg;

    @Schema(description = "跳转路径")
    private String url;

    @Schema(description = "入口类型：chapter/knowledge/special/exam/custom")
    private String entryType;

    @Schema(description = "排序")
    private Integer sort;
}
