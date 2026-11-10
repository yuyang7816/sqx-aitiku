package com.sqx.app.base.homeShortcut.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class HomeShortcutSaveBo {

    @NotBlank(message = "标题不能为空")
    private String title;

    @NotBlank(message = "请上传图标")
    private String icon;

    @Schema(description = "图标颜色（历史字段，上传图标时可留空）")
    private String iconColor;

    @NotBlank(message = "请选择背景色")
    private String bgStyle;

    private String url;

    private String actionType;

    @NotNull(message = "排序不能为空")
    private Integer sortOrder;
}
