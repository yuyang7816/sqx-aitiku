import { post, resolveBusinessResponse } from '@/api/http'
import { resolvePageData } from '@/api/admin'

/** 用户学情汇总分页 */
export async function getAnalysisSummaryPage(params = {}) {
  return post('admin/analysis/summary/page', params)
    .then(resolveBusinessResponse)
    .then(resolvePageData)
}

/** 用户学情汇总详情（雷达图、薄弱点 Top3） */
export async function getAnalysisSummaryInfo(userId) {
  return post('admin/analysis/summary/info', { userId }).then(resolveBusinessResponse)
}

/** 月度报告生成日志分页 */
export async function getAnalysisReportLogPage(params = {}) {
  return post('admin/analysis/reportLog/page', params)
    .then(resolveBusinessResponse)
    .then(resolvePageData)
}
