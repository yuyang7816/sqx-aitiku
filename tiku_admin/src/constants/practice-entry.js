/** 练习入口启用状态 */
export const PRACTICE_ENTRY_STATE_OPTIONS = [
  { label: '已启用', value: 1 },
  { label: '已禁用', value: 2 },
]

/** 入口类型：与 C 端 entryType 一致 */
export const PRACTICE_ENTRY_TYPE_OPTIONS = [
  { label: '章节练习', value: 'chapter' },
  { label: '知识点练习', value: 'knowledge' },
  { label: '专项训练', value: 'special' },
  { label: '真题模拟', value: 'exam' },
  { label: '自定义链接', value: 'custom' },
]

export function getPracticeEntryStateLabel(value) {
  return PRACTICE_ENTRY_STATE_OPTIONS.find((item) => item.value === value)?.label || '-'
}

export function getPracticeEntryTypeLabel(value) {
  const key = String(value || '').trim()
  if (!key) return '自定义'
  return PRACTICE_ENTRY_TYPE_OPTIONS.find((item) => item.value === key)?.label || key
}
