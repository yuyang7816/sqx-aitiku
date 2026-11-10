package com.sqx.app.base.dashboard.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "趋势项")
public class TrendItemVo {

    @Schema(description = "日期 yyyy-MM-dd")
    private String date;

    @Schema(description = "计数")
    private Long count;

    @Schema(description = "金额（元，可选）")
    private BigDecimal amount;
}
