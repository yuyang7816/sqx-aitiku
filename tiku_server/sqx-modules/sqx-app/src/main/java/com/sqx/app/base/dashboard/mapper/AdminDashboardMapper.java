package com.sqx.app.base.dashboard.mapper;

import com.sqx.app.base.dashboard.domain.vo.ChartItemVo;
import com.sqx.app.base.dashboard.domain.vo.TrendItemVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface AdminDashboardMapper {

    Long countAppUsers();

    Long countTodayNewUsers();

    Long countVipUsers();

    Long countQuestions();

    Long countPaperSets();

    Long countTodayPractice();

    Long countPendingWithdraw();

    Long countUnreadMessages();

    BigDecimal sumWeekVipAmount(@Param("days") int days);

    List<ChartItemVo> selectUserComposition();

    List<ChartItemVo> selectQuestionTypeDistribution();

    List<ChartItemVo> selectPracticeSourceDistribution();

    List<TrendItemVo> selectUserRegisterTrend(@Param("days") int days);

    List<TrendItemVo> selectPracticeActiveTrend(@Param("days") int days);

    List<TrendItemVo> selectVipOrderTrend(@Param("days") int days);
}
