package com.sqx.app.base.poster.service;

import com.sqx.app.base.poster.domain.vo.AppReportPosterVO;

import java.time.YearMonth;

/**
 * 月度报告海报数据
 */
public interface ReportPosterService {

    AppReportPosterVO getReportPoster(Long userId, YearMonth month);
}
