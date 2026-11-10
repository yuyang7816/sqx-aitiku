package com.sqx.app.base.dashboard.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "图表项")
public class ChartItemVo {

    @Schema(description = "名称")
    private String name;

    @Schema(description = "数值")
    private Long value;
}
