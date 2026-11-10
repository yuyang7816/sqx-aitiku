package com.sqx.app.vip.rights.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author 西安省钱兄网络科技有限公司
 * @since 2025-08-07 16:56:11
 */
@Data
public class VipRightsSaveBo {

    @Schema(description = "图标")
    private String img;

    @Schema(description = "名称")
    private String name;

    @Schema(description = "权益业务键：ai/ocr/video/report/ad_free")
    private String rightsCode;

    @Schema(description = "免费版展示文案")
    private String freeText;

    @Schema(description = "会员版展示文案")
    private String vipText;

    @Schema(description = "排序")
    private Integer sort;
}
