package com.sqx.app.base.analysis.controller;

import com.sqx.app.base.analysis.domain.vo.AppAnalysisOverviewVO;
import com.sqx.app.base.analysis.domain.vo.AppAnalysisReportVO;
import com.sqx.app.base.analysis.domain.vo.AppAnalysisTodayVO;
import com.sqx.app.base.analysis.service.AnalysisOverviewService;
import com.sqx.app.base.analysis.service.AnalysisReportService;
import com.sqx.app.base.analysis.service.AnalysisTodayService;
import com.sqx.app.base.analysis.domain.vo.AppStudyCalendarVO;
import com.sqx.app.base.analysis.domain.vo.AppStudyCheckInVO;
import com.sqx.app.base.analysis.service.StudyCalendarService;
import com.sqx.app.base.auth.auth.annotation.Login;
import com.sqx.common.core.domain.Result;
import com.sqx.common.core.exception.BizException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * 用户端学情统计（GET /app/analysis/today、GET /app/analysis/overview、GET /app/analysis/report，需登录）
 */
@Tag(name = "用户端-学情统计", description = "文档 21.2：GET /app/analysis/overview、GET /app/analysis/report 学情概览与月度报告，见 docs/analysis-api.md")
@RestController
@RequestMapping("/app/analysis")
@SecurityRequirement(name = "appToken")
public class AppAnalysisTodayController {

    @Resource
    private AnalysisTodayService analysisTodayService;

    @Resource
    private AnalysisOverviewService analysisOverviewService;

    @Resource
    private AnalysisReportService analysisReportService;

    @Resource
    private StudyCalendarService studyCalendarService;

    private static final DateTimeFormatter MONTH_PARAM_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM");

    /**
     * 月度学习报告（排名、学科柱状、AI 建议、同龄对比）
     */
    @Operation(summary = "月度学习报告", description = "按自然月聚合练习记录，返回排名文案、学科正确率柱状图、AI 建议与同龄对比；month 格式 yyyy-MM，默认当月；见 docs/analysis-api.md")
    @ApiResponse(content = @Content(schema = @Schema(implementation = AppAnalysisReportVO.class)))
    @Login
    @GetMapping("/report")
    public Result<AppAnalysisReportVO> report(@RequestAttribute("userId") Long userId,
                                              @RequestParam(value = "month", required = false) String month) {
        return Result.success(analysisReportService.getReport(userId, parseMonthParam(month)));
    }

    /**
     * 学情概览（本周题量、正确率、雷达维度、薄弱点 Top3）
     */
    @Operation(summary = "学情概览", description = "聚合练习记录返回本周题量、平均正确率、学科雷达维度与薄弱章 Top3；见 docs/analysis-api.md")
    @ApiResponse(content = @Content(schema = @Schema(implementation = AppAnalysisOverviewVO.class)))
    @Login
    @GetMapping("/overview")
    public Result<AppAnalysisOverviewVO> overview(@RequestAttribute("userId") Long userId) {
        return Result.success(analysisOverviewService.getOverview(userId));
    }

    /**
     * 今日练习统计
     */
    @Operation(summary = "今日练习统计", description = "返回今日累计练习时长（durationMinutes）与已做题目数量（questionCount）")
    @ApiResponse(content = @Content(schema = @Schema(implementation = AppAnalysisTodayVO.class)))
    @Login
    @GetMapping("/today")
    public Result<AppAnalysisTodayVO> today(@RequestAttribute("userId") Long userId) {
        return Result.success(analysisTodayService.getTodayStats(userId));
    }

    @Operation(summary = "学习日历", description = "返回指定月份的学习/打卡日期、连续天数")
    @ApiResponse(content = @Content(schema = @Schema(implementation = AppStudyCalendarVO.class)))
    @Login
    @GetMapping("/calendar")
    public Result<AppStudyCalendarVO> calendar(@RequestAttribute("userId") Long userId,
                                                 @RequestParam(value = "month", required = false) String month) {
        return Result.success(studyCalendarService.getCalendar(userId, parseMonthParam(month)));
    }

    @Operation(summary = "学习打卡", description = "手动打卡；与当日练习记录合并计算连续天数")
    @ApiResponse(content = @Content(schema = @Schema(implementation = AppStudyCheckInVO.class)))
    @Login
    @PostMapping("/check-in")
    public Result<AppStudyCheckInVO> checkIn(@RequestAttribute("userId") Long userId) {
        return Result.success(studyCalendarService.checkIn(userId));
    }

    private YearMonth parseMonthParam(String month) {
        if (StringUtils.isBlank(month)) {
            return YearMonth.now();
        }
        try {
            return YearMonth.parse(StringUtils.trim(month), MONTH_PARAM_FORMAT);
        } catch (DateTimeParseException ex) {
            throw new BizException("month 格式须为 yyyy-MM");
        }
    }
}
