package com.sqx.app.base.dashboard.service.impl;

import com.sqx.app.base.dashboard.domain.vo.AdminDashboardOverviewVo;
import com.sqx.app.base.dashboard.domain.vo.ChartItemVo;
import com.sqx.app.base.dashboard.domain.vo.TrendItemVo;
import com.sqx.app.base.dashboard.mapper.AdminDashboardMapper;
import com.sqx.app.base.dashboard.service.AdminDashboardService;
import com.sqx.app.base.kefuChat.service.KefuChatService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@Service
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private static final int TREND_DAYS = 7;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Resource
    private AdminDashboardMapper adminDashboardMapper;

    @Resource
    private KefuChatService kefuChatService;

    @Override
    public AdminDashboardOverviewVo getOverview() {
        AdminDashboardOverviewVo vo = new AdminDashboardOverviewVo();
        vo.setAppUserTotal(safeCount(adminDashboardMapper::countAppUsers));
        vo.setTodayNewUsers(safeCount(adminDashboardMapper::countTodayNewUsers));
        vo.setVipUserTotal(safeCount(adminDashboardMapper::countVipUsers));
        vo.setQuestionTotal(safeCount(adminDashboardMapper::countQuestions));
        vo.setPaperSetTotal(safeCount(adminDashboardMapper::countPaperSets));
        vo.setTodayPracticeCount(safeCount(adminDashboardMapper::countTodayPractice));
        vo.setPendingWithdrawCount(safeCount(adminDashboardMapper::countPendingWithdraw));
        vo.setKefuUnreadCount(safeKefuUnreadCount());
        vo.setMessageUnreadCount(safeCount(adminDashboardMapper::countUnreadMessages));
        vo.setWeekVipAmount(safeAmount(() -> adminDashboardMapper.sumWeekVipAmount(TREND_DAYS)));

        vo.setUserComposition(safeList(adminDashboardMapper::selectUserComposition));
        vo.setQuestionTypeDistribution(safeList(adminDashboardMapper::selectQuestionTypeDistribution));
        vo.setPracticeSourceDistribution(safeList(adminDashboardMapper::selectPracticeSourceDistribution));

        vo.setUserRegisterTrend(fillTrendGaps(safeTrend(() -> adminDashboardMapper.selectUserRegisterTrend(TREND_DAYS))));
        vo.setPracticeActiveTrend(fillTrendGaps(safeTrend(() -> adminDashboardMapper.selectPracticeActiveTrend(TREND_DAYS))));
        vo.setVipOrderTrend(fillTrendGaps(safeTrend(() -> adminDashboardMapper.selectVipOrderTrend(TREND_DAYS))));
        return vo;
    }

    private Long safeKefuUnreadCount() {
        try {
            return (long) kefuChatService.countUnreadForStaff();
        } catch (Exception ex) {
            return 0L;
        }
    }

    private Long safeCount(Supplier<Long> supplier) {
        try {
            Long value = supplier.get();
            return value == null ? 0L : value;
        } catch (Exception ex) {
            return 0L;
        }
    }

    private BigDecimal safeAmount(Supplier<BigDecimal> supplier) {
        try {
            BigDecimal value = supplier.get();
            return value == null ? BigDecimal.ZERO : value;
        } catch (Exception ex) {
            return BigDecimal.ZERO;
        }
    }

    private List<ChartItemVo> safeList(Supplier<List<ChartItemVo>> supplier) {
        try {
            List<ChartItemVo> list = supplier.get();
            return list == null ? new ArrayList<>() : list;
        } catch (Exception ex) {
            return new ArrayList<>();
        }
    }

    private List<TrendItemVo> safeTrend(Supplier<List<TrendItemVo>> supplier) {
        try {
            List<TrendItemVo> list = supplier.get();
            return list == null ? new ArrayList<>() : list;
        } catch (Exception ex) {
            return new ArrayList<>();
        }
    }

    private List<TrendItemVo> fillTrendGaps(List<TrendItemVo> source) {
        Map<String, TrendItemVo> map = new LinkedHashMap<>();
        LocalDate today = LocalDate.now();
        for (int i = TREND_DAYS - 1; i >= 0; i--) {
            String date = today.minusDays(i).format(DATE_FMT);
            map.put(date, new TrendItemVo(date, 0L, BigDecimal.ZERO));
        }
        for (TrendItemVo item : source) {
            if (item == null || item.getDate() == null) {
                continue;
            }
            TrendItemVo target = map.get(item.getDate());
            if (target != null) {
                target.setCount(item.getCount() == null ? 0L : item.getCount());
                target.setAmount(item.getAmount() == null ? BigDecimal.ZERO : item.getAmount());
            }
        }
        return new ArrayList<>(map.values());
    }
}
