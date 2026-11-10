package com.sqx.app.vip.rights.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 会员权益对比表单项（GET /app/vipRights 出参）
 */
@Data
@Schema(description = "会员权益对比项")
public class AppVipRightsCompareItemVO {

    @Schema(description = "权益业务键：ai/ocr/video/report/ad_free")
    private String code;

    @Schema(description = "权益名称（对比表第一列）")
    private String label;

    @Schema(description = "免费版展示文案")
    private String free;

    @Schema(description = "会员版展示文案")
    private String vip;
}
