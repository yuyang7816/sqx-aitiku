package com.sqx.app.base.dashboard.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@Schema(description = "管理端运营看板概览")
public class AdminDashboardOverviewVo {

    @Schema(description = "C端用户总数")
    private Long appUserTotal = 0L;

    @Schema(description = "今日新增用户")
    private Long todayNewUsers = 0L;

    @Schema(description = "VIP 会员数")
    private Long vipUserTotal = 0L;

    @Schema(description = "题库题目数")
    private Long questionTotal = 0L;

    @Schema(description = "题集数量")
    private Long paperSetTotal = 0L;

    @Schema(description = "今日练习提交次数")
    private Long todayPracticeCount = 0L;

    @Schema(description = "待审核提现")
    private Long pendingWithdrawCount = 0L;

    @Schema(description = "客服 IM 未读消息数（用户发来）")
    private Long kefuUnreadCount = 0L;

    @Schema(description = "C 端用户未读系统消息数")
    private Long messageUnreadCount = 0L;

    @Schema(description = "近7日VIP付费金额（元）")
    private BigDecimal weekVipAmount = BigDecimal.ZERO;

    @Schema(description = "用户构成（饼图）")
    private List<ChartItemVo> userComposition = new ArrayList<>();

    @Schema(description = "题目题型分布（饼图）")
    private List<ChartItemVo> questionTypeDistribution = new ArrayList<>();

    @Schema(description = "练习来源分布（饼图）")
    private List<ChartItemVo> practiceSourceDistribution = new ArrayList<>();

    @Schema(description = "近7日新增用户（折线）")
    private List<TrendItemVo> userRegisterTrend = new ArrayList<>();

    @Schema(description = "近7日练习活跃（折线）")
    private List<TrendItemVo> practiceActiveTrend = new ArrayList<>();

    @Schema(description = "近7日VIP订单金额（折线）")
    private List<TrendItemVo> vipOrderTrend = new ArrayList<>();
}
