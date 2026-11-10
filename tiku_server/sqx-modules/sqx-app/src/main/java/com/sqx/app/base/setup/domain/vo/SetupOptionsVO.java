package com.sqx.app.base.setup.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 学习计划选项（GET /app/setup/options 出参）
 */
@Data
public class SetupOptionsVO {

    @Schema(description = "学段选项列表（小学/初中/高中）")
    private List<SetupGradeStageOptionVO> gradeStages;

    @Schema(description = "年级选项列表")
    private List<SetupGradeOptionVO> grades;

    @Schema(description = "学科选项列表")
    private List<SetupSubjectOptionVO> subjects;

    @Schema(description = "教材版本选项列表")
    private List<SetupVersionOptionVO> versions;
}
