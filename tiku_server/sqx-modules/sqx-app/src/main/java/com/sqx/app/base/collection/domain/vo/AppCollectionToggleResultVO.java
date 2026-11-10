package com.sqx.app.base.collection.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户端收藏操作结果（POST /app/collection 出参）
 */
@Data
public class AppCollectionToggleResultVO {

    @Schema(description = "是否已收藏（POST 成功恒为 true）")
    private Boolean collected;

    @Schema(description = "收藏详情")
    private AppCollectionItemVO item;
}
