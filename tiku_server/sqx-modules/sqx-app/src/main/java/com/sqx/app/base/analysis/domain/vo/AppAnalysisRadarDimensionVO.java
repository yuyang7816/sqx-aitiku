package com.sqx.app.base.analysis.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 学情概览雷达图维度（按学科聚合正确率得分）
 */
@Data
public class AppAnalysisRadarDimensionVO {

    @Schema(description = "维度名称（通常为学科，如数学、英语）")
    private String name;

    @Schema(description = "维度得分 0~100（该维度答题正确率）")
    private Integer score;
}
