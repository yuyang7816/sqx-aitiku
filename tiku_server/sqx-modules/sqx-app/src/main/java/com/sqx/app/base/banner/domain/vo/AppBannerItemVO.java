package com.sqx.app.base.banner.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户端 Banner 轮播项（GET /app/banner 出参，不暴露 Entity）
 */
@Data
public class AppBannerItemVO {

    @Schema(description = "Banner 主键")
    private Long id;

    @Schema(description = "轮播图片地址")
    private String imageUrl;

    @Schema(description = "点击跳转链接（会员中心、外链等）")
    private String link;

    @Schema(description = "展示排序，数值越小越靠前")
    private Integer sort;
}
