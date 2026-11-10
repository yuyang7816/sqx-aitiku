/** 刷题中心四种练习入口（与 uniapp practice-config 一致） */
export const QUESTION_PRACTICE_ENTRY_OPTIONS = [
  { label: '章节练习', value: 'chapter', hint: '绑定到具体章节/小节，出现在刷题中心「热门章节」' },
  { label: '知识点练习', value: 'knowledge', hint: '绑定到知识点，出现在「知识点练习」列表' },
  { label: '专项训练', value: 'special', hint: '加入专项题集，出现在刷题中心「专项训练」' },
  { label: '真题模拟', value: 'exam', hint: '加入真题题集，出现在刷题中心「真题模拟」' },
]

/** 题型选项 */
export const QUESTION_TYPE_OPTIONS = [
  { label: '单选题', value: '单选题' },
  { label: '多选题', value: '多选题' },
  { label: '判断题', value: '判断题' },
]

/** 难度选项 */
export const QUESTION_DIFFICULTY_OPTIONS = [
  { label: '简单', value: '简单' },
  { label: '中等', value: '中等' },
  { label: '困难', value: '困难' },
]

/** 题目状态 */
export const QUESTION_STATE_OPTIONS = [
  { label: '启用', value: 1 },
  { label: '禁用', value: 2 },
]

export const QUESTION_DEFAULT_OPTION = { label: '', value: '' }

export const QUESTION_DEFAULT_FORM = {
  practiceEntry: 'chapter',
  subject: '',
  grade: '',
  version: '',
  chapterCode: '',
  sectionCode: '',
  pointCode: '',
  paperCode: '',
  title: '',
  questionType: '单选题',
  difficulty: '简单',
  rightAnswer: '',
  analysis: '',
  options: [
    { label: 'A', value: '' },
    { label: 'B', value: '' },
    { label: 'C', value: '' },
    { label: 'D', value: '' },
  ],
  tags: [],
  sortOrder: 1,
  state: 1,
  videoUrl: '',
  videoPosterUrl: '',
  videoDuration: '',
  videoTeacher: '',
  videoSummary: '',
}

export function getQuestionPracticeEntryLabel(entry) {
  return QUESTION_PRACTICE_ENTRY_OPTIONS.find((item) => item.value === entry)?.label || '—'
}

export function getQuestionStateLabel(state) {
  return QUESTION_STATE_OPTIONS.find((item) => item.value === state)?.label || '-'
}

export function getQuestionTypeLabel(type) {
  return QUESTION_TYPE_OPTIONS.find((item) => item.value === type)?.label || type || '-'
}

export function getQuestionDifficultyLabel(difficulty) {
  return QUESTION_DIFFICULTY_OPTIONS.find((item) => item.value === difficulty)?.label || difficulty || '-'
}

export function isPaperPracticeEntry(entry) {
  return entry === 'special' || entry === 'exam'
}
