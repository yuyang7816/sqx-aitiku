package com.sqx.app.base.poster.controller;

import com.sqx.app.base.auth.auth.annotation.Login;
import com.sqx.app.base.poster.domain.vo.AppReportPosterVO;
import com.sqx.app.base.poster.service.ReportPosterService;
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
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * 用户端报告海报（GET /app/poster/report，需登录）
 */
@Tag(name = "用户端-报告海报", description = "月度报告海报数据与小程序码")
@RestController
@RequestMapping("/app/poster")
@SecurityRequirement(name = "appToken")
public class AppReportPosterController {

    private static final DateTimeFormatter MONTH_PARAM_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM");

    @Resource
    private ReportPosterService reportPosterService;

    @Operation(summary = "月度报告海报数据", description = "返回报告摘要与小程序码图片 URL，供 uniapp canvas 绘制")
    @ApiResponse(content = @Content(schema = @Schema(implementation = AppReportPosterVO.class)))
    @Login
    @GetMapping("/report")
    public Result<AppReportPosterVO> report(@RequestAttribute("userId") Long userId,
                                          @RequestParam(value = "month", required = false) String month) {
        return Result.success(reportPosterService.getReportPoster(userId, parseMonthParam(month)));
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
