package com.sqx.app.base.articleCms.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 协议与政策 CMS 文章（供 uniapp article 页一次拉取）
 */
@Data
public class ArticleCmsVO {

    @Schema(description = "文章类型：agreement / privacy / about")
    private String type;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "副标题")
    private String sub;

    @Schema(description = "正文（富文本 HTML，uniapp up-parse 渲染）")
    private String content;
}
