package com.sqx.app.base.dashboard.controller;

import com.sqx.app.base.dashboard.domain.vo.AdminDashboardOverviewVo;
import com.sqx.app.base.dashboard.service.AdminDashboardService;
import com.sqx.common.core.domain.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@Tag(name = "管理端-运营看板", description = "运营 KPI 与图表统计")
@RestController
@RequestMapping("/admin/dashboard")
public class AdminDashboardController {

    @Resource
    private AdminDashboardService adminDashboardService;

    @Operation(summary = "运营看板概览")
    @GetMapping("overview")
    public Result<AdminDashboardOverviewVo> overview() {
        return Result.success(adminDashboardService.getOverview());
    }
}
