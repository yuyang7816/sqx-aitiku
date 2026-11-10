/** 题集类型 */
export const PAPER_TYPE_OPTIONS = [
  { label: '专项训练', value: 'special' },
  { label: '真题模拟', value: 'exam' },
]

/** 题集上下架状态 */
export const PAPER_STATE_OPTIONS = [
  { label: '上架', value: 1 },
  { label: '下架', value: 2 },
]

export const PAPER_DEFAULT_FORM = {
  title: '',
  paperType: 'special',
  description: '',
  grade: '',
  gradeShort: '',
  subject: '',
  sortOrder: 1,
  state: 1,
  items: [],
}

export function getPaperTypeLabel(type) {
  return PAPER_TYPE_OPTIONS.find((item) => item.value === type)?.label || type || '-'
}

export function getPaperStateLabel(state) {
  return PAPER_STATE_OPTIONS.find((item) => item.value === state)?.label || '-'
}
