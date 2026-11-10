package com.sqx.app.base.map.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author chengming
 * @since 2025/7/22
 */
@Data
public class AddressVo {

    @Schema(description = "国家")
    private String country;

    @Schema(description = "省")
    private String province;

    @Schema(description = "市")
    private String city;

    @Schema(description = "区/县")
    private String district;

    @Schema(description = "街道/城镇")
    private String town;

    @Schema(description = "路/街")
    private String road;

    @Schema(description = "地址")
    private String address;
}
