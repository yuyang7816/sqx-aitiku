package com.sqx.app.base.collection.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户端收藏列表查询入参（GET /app/collection）
 */
@Data
public class AppCollectionQueryBO {

    @Schema(description = "学科筛选（精确匹配 subject 字段，如 数学 · 初三）")
    private String subject;
}
