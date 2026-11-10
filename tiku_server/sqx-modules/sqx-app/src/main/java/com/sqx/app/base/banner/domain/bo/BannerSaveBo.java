package com.sqx.app.base.banner.domain.bo;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * @author sqx
 * @since 2025-05-16 13:49:04
 */
@Data
public class BannerSaveBo {

    @Schema(description = "名称")
    @NotBlank(message = "名称不能为空")
    private String name;

    @Schema(description = "图片地址")
    private String imageUrl;

    @Schema(description = "分类1banner图 2金刚区分类")
    @NotNull(message = "分类不能为空")
    private Integer classify;

    @Schema(description = "跳转地址 ")
    private String url;

    @Schema(description = "顺序")
    private Integer sort;

    @Schema(description = "描述")
    private String description;
}
