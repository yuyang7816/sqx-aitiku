export const USER_BADGE_RULE_TYPE_OPTIONS = [
  { value: 'submit_count', label: '交卷次数' },
  { value: 'study_days', label: '累计学习天数' },
  { value: 'question_count', label: '累计做题数' },
  { value: 'perfect_score', label: '单次满分' },
]

export const USER_BADGE_STATE_OPTIONS = [
  { value: 1, label: '启用' },
  { value: 0, label: '禁用' },
]

export function getUserBadgeRuleTypeLabel(ruleType) {
  return USER_BADGE_RULE_TYPE_OPTIONS.find((item) => item.value === ruleType)?.label || ruleType || '—'
}

export function getUserBadgeStateLabel(state) {
  return state === 1 ? '启用' : '禁用'
}

export function getUserBadgeStateTagType(state) {
  return state === 1 ? 'success' : 'info'
}
