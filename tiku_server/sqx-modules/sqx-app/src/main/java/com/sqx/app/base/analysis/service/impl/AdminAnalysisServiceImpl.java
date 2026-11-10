package com.sqx.app.base.analysis.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sqx.app.base.analysis.domain.bo.AnalysisReportLogPageBo;
import com.sqx.app.base.analysis.domain.bo.AnalysisSummaryInfoBo;
import com.sqx.app.base.analysis.domain.bo.AnalysisSummaryPageBo;
import com.sqx.app.base.analysis.domain.vo.AdminAnalysisReportLogVo;
import com.sqx.app.base.analysis.domain.vo.AdminAnalysisSummaryVo;
import com.sqx.app.base.analysis.domain.vo.AppAnalysisOverviewVO;
import com.sqx.app.base.analysis.domain.vo.AppAnalysisTodayVO;
import com.sqx.app.base.analysis.domain.vo.AppAnalysisWeakPointVO;
import com.sqx.app.base.analysis.service.AdminAnalysisService;
import com.sqx.app.base.analysis.service.AnalysisOverviewService;
import com.sqx.app.base.analysis.service.AnalysisTodayService;
import com.sqx.app.base.analysis.service.impl.MonthlyReportPushServiceImpl;
import com.sqx.app.base.messages.domain.bo.MessagesPageBo;
import com.sqx.app.base.messages.domain.vo.MessagesVo;
import com.sqx.app.base.messages.service.MessagesService;
import com.sqx.app.base.user.db.service.IUserService;
import com.sqx.app.base.user.domain.entity.UserEntity;
import com.sqx.common.core.exception.BizException;
import com.sqx.common.mybatis.utils.PageUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 管理端学情统计：聚合 C 端学情服务与月报推送消息
 */
@Service
public class AdminAnalysisServiceImpl implements AdminAnalysisService {

    private static final Pattern REPORT_MONTH_PATTERN = Pattern.compile("(\\d{4})年(\\d{1,2})月");

    @Resource
    private IUserService iUserService;
    @Resource
    private AnalysisOverviewService analysisOverviewService;
    @Resource
    private AnalysisTodayService analysisTodayService;
    @Resource
    private MessagesService messagesService;

    @Override
    public IPage<AdminAnalysisSummaryVo> pageSummary(AnalysisSummaryPageBo pageBo) {
        Page<UserEntity> userPage = PageUtil.initPage(pageBo.getPage(), pageBo.getLimit());
        LambdaQueryWrapper<UserEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.isNotBlank(pageBo.getUserName()), UserEntity::getUserName, pageBo.getUserName())
                .like(StringUtils.isNotBlank(pageBo.getPhone()), UserEntity::getPhone, pageBo.getPhone())
                .orderByDesc(UserEntity::getCreateTime);
        Page<UserEntity> entityPage = iUserService.page(userPage, wrapper);

        List<AdminAnalysisSummaryVo> records = new ArrayList<>();
        for (UserEntity user : entityPage.getRecords()) {
            try {
                AdminAnalysisSummaryVo vo = buildSummaryVo(user, false);
                if (matchesSummaryFilters(vo, pageBo)) {
                    records.add(vo);
                }
            } catch (Exception ex) {
                AdminAnalysisSummaryVo fallback = new AdminAnalysisSummaryVo();
                fallback.setUserId(user.getUserId());
                fallback.setUserName(user.getUserName());
                fallback.setPhone(user.getPhone());
                fallback.setGrade(user.getGrade());
                fallback.setWeekQuestionCount(0);
                fallback.setCorrectRate(0);
                fallback.setTodayQuestionCount(0);
                fallback.setTodayDurationMinutes(0);
                fallback.setWeakPointSummary("暂无学情数据");
                if (matchesSummaryFilters(fallback, pageBo)) {
                    records.add(fallback);
                }
            }
        }

        Page<AdminAnalysisSummaryVo> result = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        result.setRecords(records);
        return result;
    }

    @Override
    public AdminAnalysisSummaryVo summaryInfo(AnalysisSummaryInfoBo infoBo) {
        UserEntity user = iUserService.getById(infoBo.getUserId());
        if (user == null) {
            throw new BizException("用户不存在");
        }
        return buildSummaryVo(user, true);
    }

    @Override
    public IPage<AdminAnalysisReportLogVo> pageReportLog(AnalysisReportLogPageBo pageBo) {
        MessagesPageBo messagesPageBo = new MessagesPageBo();
        messagesPageBo.setPage(pageBo.getPage());
        messagesPageBo.setLimit(pageBo.getLimit());
        messagesPageBo.setUrl(MonthlyReportPushServiceImpl.NOTICE_REPORT_URL);
        messagesPageBo.setUserName(pageBo.getUserName());
        messagesPageBo.setPhone(pageBo.getPhone());
        messagesPageBo.setIsRead(pageBo.getIsRead());
        messagesPageBo.setStartTime(pageBo.getStartTime());
        messagesPageBo.setEndTime(pageBo.getEndTime());
        if (StringUtils.isNotBlank(pageBo.getReportMonth())) {
            messagesPageBo.setTitle(normalizeReportMonthKeyword(pageBo.getReportMonth()));
        }

        IPage<MessagesVo> messagePage = messagesService.pageInfo(messagesPageBo);
        List<AdminAnalysisReportLogVo> records = messagePage.getRecords().stream()
                .map(this::toReportLogVo)
                .collect(Collectors.toList());
        return PageUtil.copyPage(messagePage, records);
    }

    private AdminAnalysisSummaryVo buildSummaryVo(UserEntity user, boolean includeDetails) {
        AppAnalysisOverviewVO overview = analysisOverviewService.getOverview(user.getUserId());
        AppAnalysisTodayVO today = analysisTodayService.getTodayStats(user.getUserId());

        AdminAnalysisSummaryVo vo = new AdminAnalysisSummaryVo();
        vo.setUserId(user.getUserId());
        vo.setUserName(user.getUserName());
        vo.setPhone(user.getPhone());
        vo.setGrade(user.getGrade());
        vo.setWeekStartDate(overview.getWeekStartDate());
        vo.setWeekEndDate(overview.getWeekEndDate());
        vo.setWeekQuestionCount(overview.getWeekQuestionCount());
        vo.setWeekQuestionChangePercent(overview.getWeekQuestionChangePercent());
        vo.setCorrectRate(overview.getCorrectRate());
        vo.setCorrectRateChange(overview.getCorrectRateChange());
        vo.setTodayQuestionCount(today.getQuestionCount());
        vo.setTodayDurationMinutes(today.getDurationMinutes());
        vo.setWeakPointSummary(buildWeakPointSummary(overview.getWeakPoints()));
        if (includeDetails) {
            vo.setRadarDimensions(overview.getRadarDimensions());
            vo.setWeakPoints(overview.getWeakPoints());
        }
        return vo;
    }

    private boolean matchesSummaryFilters(AdminAnalysisSummaryVo vo, AnalysisSummaryPageBo pageBo) {
        if (pageBo.getMinWeekQuestionCount() != null
                && safeInt(vo.getWeekQuestionCount()) < pageBo.getMinWeekQuestionCount()) {
            return false;
        }
        if (pageBo.getMinCorrectRate() != null
                && safeInt(vo.getCorrectRate()) < pageBo.getMinCorrectRate()) {
            return false;
        }
        return true;
    }

    private String buildWeakPointSummary(List<AppAnalysisWeakPointVO> weakPoints) {
        if (weakPoints == null || weakPoints.isEmpty()) {
            return "暂无薄弱点";
        }
        return weakPoints.stream()
                .map(AppAnalysisWeakPointVO::getTitle)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.joining("、"));
    }

    private AdminAnalysisReportLogVo toReportLogVo(MessagesVo message) {
        AdminAnalysisReportLogVo vo = new AdminAnalysisReportLogVo();
        vo.setMessageId(message.getId());
        vo.setTitle(message.getTitle());
        vo.setReportMonth(parseReportMonth(message.getTitle()));
        vo.setContent(message.getContent());
        vo.setUserId(message.getUserId());
        vo.setUserName(message.getUserName());
        vo.setPhone(message.getPhone());
        vo.setIsRead(message.getIsRead());
        vo.setCreateTime(message.getCreateTime());
        return vo;
    }

    private String normalizeReportMonthKeyword(String reportMonth) {
        String value = StringUtils.trimToEmpty(reportMonth);
        if (value.matches("\\d{4}-\\d{1,2}")) {
            String[] parts = value.split("-");
            int month = Integer.parseInt(parts[1]);
            return parts[0] + "年" + month + "月";
        }
        return value;
    }

    private String parseReportMonth(String title) {
        if (StringUtils.isBlank(title)) {
            return "";
        }
        Matcher matcher = REPORT_MONTH_PATTERN.matcher(title);
        if (!matcher.find()) {
            return "";
        }
        int month = Integer.parseInt(matcher.group(2));
        return String.format("%s-%02d", matcher.group(1), month);
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }
}
