package com.sqx.app.base.analysis.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.base.analysis.domain.bo.AnalysisReportLogPageBo;
import com.sqx.app.base.analysis.domain.bo.AnalysisSummaryInfoBo;
import com.sqx.app.base.analysis.domain.bo.AnalysisSummaryPageBo;
import com.sqx.app.base.analysis.domain.vo.AdminAnalysisReportLogVo;
import com.sqx.app.base.analysis.domain.vo.AdminAnalysisSummaryVo;
import com.sqx.app.base.analysis.service.AdminAnalysisService;
import com.sqx.common.core.domain.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 管理端学情统计（用户学情汇总、月度报告生成日志）
 */
@Tag(name = "管理端-学情统计", description = "用户学情汇总与月度报告推送日志查询")
@RestController
@RequestMapping("/admin/analysis")
public class AdminAnalysisController {

    @Resource
    private AdminAnalysisService adminAnalysisService;

    @Operation(summary = "用户学情汇总分页")
    @ApiResponse(content = @Content(schema = @Schema(implementation = AdminAnalysisSummaryVo.class)))
    @PostMapping("summary/page")
    public Result<IPage<AdminAnalysisSummaryVo>> summaryPage(
            @RequestBody @Validated AnalysisSummaryPageBo pageBo) {
        return Result.success(adminAnalysisService.pageSummary(pageBo));
    }

    @Operation(summary = "用户学情汇总详情")
    @ApiResponse(content = @Content(schema = @Schema(implementation = AdminAnalysisSummaryVo.class)))
    @PostMapping("summary/info")
    public Result<AdminAnalysisSummaryVo> summaryInfo(
            @RequestBody @Validated AnalysisSummaryInfoBo infoBo) {
        return Result.success(adminAnalysisService.summaryInfo(infoBo));
    }

    @Operation(summary = "月度报告生成日志分页")
    @ApiResponse(content = @Content(schema = @Schema(implementation = AdminAnalysisReportLogVo.class)))
    @PostMapping("reportLog/page")
    public Result<IPage<AdminAnalysisReportLogVo>> reportLogPage(
            @RequestBody @Validated AnalysisReportLogPageBo pageBo) {
        return Result.success(adminAnalysisService.pageReportLog(pageBo));
    }
}
