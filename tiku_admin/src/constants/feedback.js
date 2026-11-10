/** 用户反馈类型（与 C 端 /app/feedback/submit category 一致） */
export const FEEDBACK_CATEGORY_OPTIONS = [
  { label: '功能问题', value: 'feature' },
  { label: '账号与会员', value: 'account' },
  { label: '搜题与答疑', value: 'search' },
  { label: '其他建议', value: 'other' },
]

/** 用户反馈状态 */
export const FEEDBACK_STATUS_OPTIONS = [
  { label: '待处理', value: 'pending' },
  { label: '处理中', value: 'processing' },
  { label: '已回复', value: 'replied' },
]

const CATEGORY_LABEL_MAP = Object.fromEntries(
  FEEDBACK_CATEGORY_OPTIONS.map((item) => [item.value, item.label]),
)

const STATUS_LABEL_MAP = Object.fromEntries(
  FEEDBACK_STATUS_OPTIONS.map((item) => [item.value, item.label]),
)

const STATUS_TAG_MAP = {
  pending: 'warning',
  processing: 'info',
  replied: 'success',
}

export function getFeedbackCategoryLabel(value = '') {
  return CATEGORY_LABEL_MAP[String(value || '').trim()] || value || '-'
}

export function getFeedbackStatusLabel(value = '') {
  return STATUS_LABEL_MAP[String(value || '').trim()] || value || '-'
}

export function getFeedbackStatusTagType(value = '') {
  return STATUS_TAG_MAP[String(value || '').trim()] || 'info'
}
