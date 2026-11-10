import { SETUP_DICT_TYPE } from '@/constants/setup-dict'

/** 章/节状态 */
export const CHAPTER_STATE_OPTIONS = [
  { label: '启用', value: 1 },
  { label: '禁用', value: 2 },
]

export const CHAPTER_TREE_TABS = [
  { key: 'chapter', label: '章' },
  { key: 'section', label: '节' },
]

export const SETUP_DICT_TYPES = {
  grade: SETUP_DICT_TYPE.GRADE,
  subject: SETUP_DICT_TYPE.SUBJECT,
  version: SETUP_DICT_TYPE.VERSION,
}

export const CHAPTER_DEFAULT_FORM = {
  title: '',
  grade: '',
  gradeShort: '',
  subject: '',
  version: '',
  questionCount: 0,
  sortOrder: 1,
  state: 1,
}

export const SECTION_DEFAULT_FORM = {
  chapterId: null,
  title: '',
  questionCount: 0,
  sortOrder: 1,
  state: 1,
}

export function getChapterStateLabel(state) {
  return CHAPTER_STATE_OPTIONS.find((item) => item.value === state)?.label || '-'
}
