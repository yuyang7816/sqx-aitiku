export const APP_USER_STATUS_OPTIONS = [
  { label: '正常', value: 1, tagType: 'success' },
  { label: '禁用', value: 2, tagType: 'danger' },
]

export const APP_USER_TYPE_OPTIONS = [
  { label: '普通用户', value: 1 },
  { label: 'VIP 用户', value: 2 },
]

export const APP_USER_SEX_OPTIONS = [
  { label: '男', value: 1 },
  { label: '女', value: 2 },
]

export function getAppUserStatusLabel(status) {
  return APP_USER_STATUS_OPTIONS.find((item) => item.value === status)?.label || '未知'
}

export function getAppUserStatusTagType(status) {
  return APP_USER_STATUS_OPTIONS.find((item) => item.value === status)?.tagType || 'info'
}

export function getAppUserTypeLabel(userType) {
  return APP_USER_TYPE_OPTIONS.find((item) => item.value === userType)?.label || '—'
}

export function getAppUserSexLabel(sex) {
  return APP_USER_SEX_OPTIONS.find((item) => item.value === sex)?.label || '—'
}

export function resolveAppUserDisplayName(user = {}) {
  return user.nickname || user.userName || (user.userId ? `用户${user.userId}` : '—')
}

export function formatAppUserWalletAmount(amount) {
  if (amount === null || amount === undefined || amount === '') {
    return '—'
  }
  return `¥${amount}`
}

export function formatSuperiorSummary(row = {}) {
  if (row.superiorDisplayName) {
    return row.superiorDisplayName
  }
  if (row.superiorInvitationCode) {
    return row.superiorInvitationCode
  }
  return '—'
}

/** 与后端 ReferralCommissionRateSupport 一致的代码兜底默认值 */
export const COMMISSION_RATE_CODE_DEFAULTS = {
  level1: 0.10,
  level2: 0.05,
}

let globalCommissionDefaults = { ...COMMISSION_RATE_CODE_DEFAULTS }

export function setGlobalCommissionDefaults(defaults = {}) {
  globalCommissionDefaults = {
    level1: parseCommissionRate(defaults.level1) ?? COMMISSION_RATE_CODE_DEFAULTS.level1,
    level2: parseCommissionRate(defaults.level2) ?? COMMISSION_RATE_CODE_DEFAULTS.level2,
  }
}

export function getGlobalCommissionDefaults() {
  return globalCommissionDefaults
}

function parseCommissionRate(value) {
  const num = Number(value)
  return Number.isFinite(num) && num > 0 ? num : null
}

export function resolveUserCommissionRate(userRate, defaultRate) {
  const user = parseCommissionRate(userRate)
  if (user != null) return user
  return parseCommissionRate(defaultRate)
}

export function formatCommissionRatePercent(rate) {
  const num = parseCommissionRate(rate)
  if (num == null) return '—'
  const percent = num * 100
  const text = Number.isInteger(percent) ? String(percent) : percent.toFixed(1).replace(/\.0$/, '')
  return `${text}%`
}

/** 展示佣金比例：用户未单独配置时展示全局默认比例 */
export function formatUserCommissionRate(rate, defaultRate) {
  return formatCommissionRatePercent(resolveUserCommissionRate(rate, defaultRate))
}

export function formatUserCommissionRateLevel1(rate) {
  return formatUserCommissionRate(rate, getGlobalCommissionDefaults().level1)
}

export function formatUserCommissionRateLevel2(rate) {
  return formatUserCommissionRate(rate, getGlobalCommissionDefaults().level2)
}

export function commissionRateToPercent(rate) {
  const num = Number(rate)
  if (!Number.isFinite(num) || num <= 0) {
    return null
  }
  return Number((num * 100).toFixed(2))
}

export function commissionRateFromPercent(percent) {
  if (percent === null || percent === undefined || percent === '') {
    return null
  }
  const num = Number(percent)
  if (!Number.isFinite(num) || num <= 0) {
    return null
  }
  return num / 100
}

export function parseAppUserSubjects(subjects) {
  if (Array.isArray(subjects)) return subjects
  if (!subjects) return []
  try {
    const parsed = JSON.parse(subjects)
    return Array.isArray(parsed) ? parsed : []
  } catch {
    return String(subjects).split(',').map((item) => item.trim()).filter(Boolean)
  }
}

export const APP_USER_DETAIL_FIELDS = [
  { key: 'userId', label: '用户 ID' },
  { key: 'userName', label: '用户名' },
  { key: 'nickname', label: '昵称' },
  { key: 'phone', label: '手机号' },
  { key: 'grade', label: '年级' },
  { key: 'gradeShort', label: '年级简称' },
  { key: 'textbookVersion', label: '教材版本' },
  { key: 'subjects', label: '学科', formatter: (value, row) => parseAppUserSubjects(value || row?.subjects).join('、') || '—' },
  { key: 'profileReady', label: '资料完善', formatter: (value) => (Number(value) === 1 ? '已完善' : '未完善') },
  { key: 'status', label: '状态', formatter: (value) => getAppUserStatusLabel(value) },
  { key: 'userType', label: '用户类型', formatter: (value) => getAppUserTypeLabel(value) },
  { key: 'isVip', label: '会员状态', formatter: (value) => (Number(value) === 1 ? '会员' : '非会员') },
  { key: 'vipExpireAt', label: '会员到期时间' },
  { key: 'walletAmount', label: '钱包余额', formatter: (value) => (value == null ? '—' : `¥${value}`) },
  { key: 'sex', label: '性别', formatter: (value) => getAppUserSexLabel(value) },
  { key: 'age', label: '年龄' },
  { key: 'platform', label: '来源平台' },
  { key: 'sysPhone', label: '手机类型', formatter: (value) => ({ 1: '安卓', 2: 'iOS' }[value] || '—') },
  { key: 'ocrUsedToday', label: '今日 OCR 次数' },
  { key: 'ocrQuotaDate', label: 'OCR 统计日期' },
  { key: 'aiUsedToday', label: '今日 AI 次数' },
  { key: 'aiQuotaDate', label: 'AI 统计日期' },
  { key: 'invitationCode', label: '邀请码' },
  { key: 'superiorInvitationCode', label: '上级邀请码' },
  { key: 'commissionRateLevel1', label: '一级佣金', formatter: (value) => formatUserCommissionRateLevel1(value) },
  { key: 'commissionRateLevel2', label: '二级佣金', formatter: (value) => formatUserCommissionRateLevel2(value) },
  { key: 'maOpenId', label: '小程序 OpenId' },
  { key: 'mpOpenId', label: '公众号 OpenId' },
  { key: 'appleId', label: 'Apple ID' },
  { key: 'cid', label: '推送 CID' },
  { key: 'createTime', label: '创建时间' },
  { key: 'updateTime', label: '更新时间' },
]
