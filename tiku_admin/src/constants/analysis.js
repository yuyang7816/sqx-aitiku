/** 学情统计 Tab 配置 */
export const ANALYSIS_TABS = [
  {
    key: 'summary',
    label: '用户学情汇总',
    desc: '查看用户本周做题量、正确率、今日练习与薄弱点摘要',
  },
  {
    key: 'reportLog',
    label: '报告生成日志',
    desc: '查询月度学习报告推送记录，含报告月份与已读状态',
  },
]

export const ANALYSIS_READ_OPTIONS = [
  { label: '全部', value: '' },
  { label: '未读', value: 0 },
  { label: '已读', value: 1 },
]

export function getAnalysisReadLabel(value) {
  if (value === 0 || value === '0') return '未读'
  if (value === 1 || value === '1') return '已读'
  return '—'
}

export function formatPercent(value, suffix = '%') {
  if (value == null || value === '') return '—'
  const num = Number(value)
  if (Number.isNaN(num)) return '—'
  return `${num}${suffix}`
}

export function formatChangePercent(value) {
  if (value == null || value === '') return '—'
  const num = Number(value)
  if (Number.isNaN(num)) return '—'
  return num > 0 ? `+${num}%` : `${num}%`
}

export function formatRateChange(value) {
  if (value == null || value === '') return '—'
  const num = Number(value)
  if (Number.isNaN(num)) return '—'
  return num > 0 ? `+${num} 百分点` : `${num} 百分点`
}
