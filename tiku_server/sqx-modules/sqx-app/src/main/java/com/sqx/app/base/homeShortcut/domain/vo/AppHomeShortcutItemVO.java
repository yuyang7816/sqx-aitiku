package com.sqx.app.base.homeShortcut.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "首页快捷入口项")
public class AppHomeShortcutItemVO {

    @Schema(description = "标题")
    private String title;

    @Schema(description = "图标图片路径")
    private String icon;

    @Schema(description = "图标颜色（历史字段）")
    private String color;

    @Schema(description = "图标背景色")
    private String bg;

    @Schema(description = "跳转路径")
    private String url;

    @Schema(description = "特殊动作：ocr-camera / customer-service")
    private String action;

    @Schema(description = "排序")
    private Integer sort;
}
