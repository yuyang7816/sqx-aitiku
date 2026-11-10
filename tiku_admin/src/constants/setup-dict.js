/** 学习计划字典类型（供 GET /app/setup/options 读取） */
export const SETUP_DICT_TYPE = {
  GRADE_STAGE: 'app_setup_grade_stage',
  GRADE: 'app_setup_grade',
  SUBJECT: 'app_setup_subject',
  VERSION: 'app_setup_version',
}

/** 检测乱码/占位文本（如 ??、???） */
export function isGarbledText(text = '') {
  const normalized = String(text).trim()
  return !normalized || /^[?？]+$/.test(normalized)
}

export function isValidDisplayText(text = '') {
  return !isGarbledText(text)
}

/** 过滤字典/选项列表中的乱码行 */
export function filterGarbledDictRows(rows = []) {
  return (rows || []).filter((row) => (
    isValidDisplayText(row?.name)
    && isValidDisplayText(row?.code)
    && isValidDisplayText(row?.value)
  ))
}

/** 字典显示状态：1 显示，0 隐藏（C 端不展示） */
export const SETUP_DICT_STATUS = {
  VISIBLE: 1,
  HIDDEN: 0,
}

/** 学段选项（运营只需选中文名称，系统自动处理编码） */
export const STAGE_NAME_OPTIONS = [
  { label: '小学', code: 'primary' },
  { label: '初中', code: 'junior' },
  { label: '高中', code: 'senior' },
]

/** 年级 remark 存学段编码，供下拉展示 */
export const GRADE_STAGE_OPTIONS = STAGE_NAME_OPTIONS.map((item) => ({
  label: item.label,
  value: item.code,
}))

const SUBJECT_ICON_BY_NAME = {
  数学: 'file-text',
  语文: 'edit-pen',
  英语: 'email',
  物理: 'reload',
  化学: 'more-circle',
  生物: 'grid',
  历史: 'clock',
  地理: 'map',
  政治: 'star',
}

const KNOWN_SUBJECT_ICONS = new Set([
  'file-text', 'edit-pen', 'email', 'reload', 'more-circle', 'grid',
  'bookmark', 'star', 'photo', 'calendar', 'map', 'tags', 'clock',
])

const DEFAULT_STAGES_BY_SUBJECT = {
  数学: ['primary', 'junior', 'senior'],
  语文: ['primary', 'junior', 'senior'],
  英语: ['primary', 'junior', 'senior'],
  物理: ['junior', 'senior'],
  化学: ['junior', 'senior'],
  生物: ['junior', 'senior'],
  历史: ['junior', 'senior'],
  地理: ['junior', 'senior'],
  政治: ['junior', 'senior'],
}

const STAGE_CODE_SET = new Set(STAGE_NAME_OPTIONS.map((item) => item.code))

const GRADE_SHORT_TO_CODE_BY_STAGE = {
  primary: {
    一年级: 'primary_1',
    二年级: 'primary_2',
    三年级: 'primary_3',
    四年级: 'primary_4',
    五年级: 'primary_5',
    六年级: 'primary_6',
  },
  junior: {
    初一: 'junior_1',
    初二: 'junior_2',
    初三: 'junior_3',
  },
  senior: {
    高一: 'senior_1',
    高二: 'senior_2',
    高三: 'senior_3',
  },
}

const GRADE_CODE_TO_SHORT = {
  primary_1: '一年级',
  primary_2: '二年级',
  primary_3: '三年级',
  primary_4: '四年级',
  primary_5: '五年级',
  primary_6: '六年级',
  junior_1: '初一',
  junior_2: '初二',
  junior_3: '初三',
  senior_1: '高一',
  senior_2: '高二',
  senior_3: '高三',
}

const GRADE_FULL_NAME_TO_CODE = {
  小学一年级: 'primary_1',
  小学二年级: 'primary_2',
  小学三年级: 'primary_3',
  小学四年级: 'primary_4',
  小学五年级: 'primary_5',
  小学六年级: 'primary_6',
  初中一年级: 'junior_1',
  初中二年级: 'junior_2',
  初中三年级: 'junior_3',
  高中一年级: 'senior_1',
  高中二年级: 'senior_2',
  高中三年级: 'senior_3',
}

const ASCII_PARAM_PATTERN = /^[\w.-]+$/

const VERSION_CODE_BY_NAME = {
  人教版: 'renjiao',
  北师大版: 'beishida',
  苏教版: 'sujiao',
  沪教版: 'hujiao',
}

function resolveGradeCode(stageCode = '', shortLabel = '', fullName = '') {
  const stage = String(stageCode || '').trim()
  const short = String(shortLabel || '').trim()
  if (stage && short && GRADE_SHORT_TO_CODE_BY_STAGE[stage]?.[short]) {
    return GRADE_SHORT_TO_CODE_BY_STAGE[stage][short]
  }
  const full = String(fullName || '').trim()
  if (full && GRADE_FULL_NAME_TO_CODE[full]) {
    return GRADE_FULL_NAME_TO_CODE[full]
  }
  if (short && ASCII_PARAM_PATTERN.test(short)) {
    return short
  }
  return short.replace(/[^\w.-]/g, '').toLowerCase() || 'grade'
}

/** 年级字典 code（ASCII）→ 运营侧年级简称（如 junior_1 → 初一） */
export function resolveGradeShortLabel(code = '', fullName = '') {
  const token = String(code || '').trim()
  if (GRADE_CODE_TO_SHORT[token]) {
    return GRADE_CODE_TO_SHORT[token]
  }
  if (token && !ASCII_PARAM_PATTERN.test(token)) {
    return token
  }
  const full = String(fullName || '').trim()
  if (full && GRADE_FULL_NAME_TO_CODE[full]) {
    return GRADE_CODE_TO_SHORT[GRADE_FULL_NAME_TO_CODE[full]] || token
  }
  return token
}


const GRADE_FULL_NAME_BY_STAGE = {
  primary: {
    一年级: '小学一年级',
    二年级: '小学二年级',
    三年级: '小学三年级',
    四年级: '小学四年级',
    五年级: '小学五年级',
    六年级: '小学六年级',
  },
  junior: {
    初一: '初中一年级',
    初二: '初中二年级',
    初三: '初中三年级',
  },
  senior: {
    高一: '高中一年级',
    高二: '高中二年级',
    高三: '高中三年级',
  },
}

/** Tab 配置 */
export const SETUP_DICT_TABS = [
  {
    key: 'gradeStage',
    label: '学段',
    type: SETUP_DICT_TYPE.GRADE_STAGE,
    searchLabel: '学段名称',
    searchPlaceholder: '如：初中',
    createLabel: '新增学段',
    emptyText: '暂无数据',
    defaultForm: { displayName: '', orderNum: 0, status: SETUP_DICT_STATUS.VISIBLE },
  },
  {
    key: 'grade',
    label: '年级',
    type: SETUP_DICT_TYPE.GRADE,
    searchLabel: '年级',
    searchPlaceholder: '如：初一',
    createLabel: '新增年级',
    emptyText: '暂无数据',
    defaultForm: { stageCode: 'junior', shortLabel: '', fullName: '', orderNum: 0, status: SETUP_DICT_STATUS.VISIBLE },
  },
  {
    key: 'subject',
    label: '学科',
    type: SETUP_DICT_TYPE.SUBJECT,
    searchLabel: '学科名称',
    searchPlaceholder: '如：数学',
    createLabel: '新增学科',
    emptyText: '暂无数据',
    defaultForm: { displayName: '', stageCodes: ['primary', 'junior', 'senior'], orderNum: 0, status: SETUP_DICT_STATUS.VISIBLE },
  },
  {
    key: 'version',
    label: '教材版本',
    type: SETUP_DICT_TYPE.VERSION,
    searchLabel: '版本名称',
    searchPlaceholder: '如：人教版',
    createLabel: '新增版本',
    emptyText: '暂无数据',
    defaultForm: { displayName: '', orderNum: 0, status: SETUP_DICT_STATUS.VISIBLE },
  },
]

/** 与 C 端 plan.vue 对齐的默认字典项 */
export const DEFAULT_SETUP_DICT_ITEMS = {
  [SETUP_DICT_TYPE.GRADE_STAGE]: [
    { name: '小学', code: 'primary', value: '小学', orderNum: 1, remark: '默认学段' },
    { name: '初中', code: 'junior', value: '初中', orderNum: 2, remark: '默认学段' },
    { name: '高中', code: 'senior', value: '高中', orderNum: 3, remark: '默认学段' },
  ],
  [SETUP_DICT_TYPE.GRADE]: [
    { name: '小学一年级', code: 'primary_1', value: '小学一年级', orderNum: 1, remark: 'primary' },
    { name: '小学二年级', code: 'primary_2', value: '小学二年级', orderNum: 2, remark: 'primary' },
    { name: '小学三年级', code: 'primary_3', value: '小学三年级', orderNum: 3, remark: 'primary' },
    { name: '小学四年级', code: 'primary_4', value: '小学四年级', orderNum: 4, remark: 'primary' },
    { name: '小学五年级', code: 'primary_5', value: '小学五年级', orderNum: 5, remark: 'primary' },
    { name: '小学六年级', code: 'primary_6', value: '小学六年级', orderNum: 6, remark: 'primary' },
    { name: '初中一年级', code: 'junior_1', value: '初中一年级', orderNum: 7, remark: 'junior' },
    { name: '初中二年级', code: 'junior_2', value: '初中二年级', orderNum: 8, remark: 'junior' },
    { name: '初中三年级', code: 'junior_3', value: '初中三年级', orderNum: 9, remark: 'junior' },
    { name: '高中一年级', code: 'senior_1', value: '高中一年级', orderNum: 10, remark: 'senior' },
    { name: '高中二年级', code: 'senior_2', value: '高中二年级', orderNum: 11, remark: 'senior' },
    { name: '高中三年级', code: 'senior_3', value: '高中三年级', orderNum: 12, remark: 'senior' },
  ],
  [SETUP_DICT_TYPE.SUBJECT]: [
    { name: '数学', code: 'math', value: '数学', orderNum: 1, remark: 'primary,junior,senior' },
    { name: '语文', code: 'chinese', value: '语文', orderNum: 2, remark: 'primary,junior,senior' },
    { name: '英语', code: 'english', value: '英语', orderNum: 3, remark: 'primary,junior,senior' },
    { name: '物理', code: 'physics', value: '物理', orderNum: 4, remark: 'junior,senior' },
    { name: '化学', code: 'chemistry', value: '化学', orderNum: 5, remark: 'junior,senior' },
    { name: '生物', code: 'biology', value: '生物', orderNum: 6, remark: 'junior,senior' },
  ],
  [SETUP_DICT_TYPE.VERSION]: [
    { name: '人教版', code: 'renjiao', value: '人教版', orderNum: 1, remark: '默认教材版本' },
    { name: '北师大版', code: 'beishida', value: '北师大版', orderNum: 2, remark: '默认教材版本' },
    { name: '苏教版', code: 'sujiao', value: '苏教版', orderNum: 3, remark: '默认教材版本' },
  ],
}

export function getSetupDictTab(key) {
  return SETUP_DICT_TABS.find((tab) => tab.key === key) || SETUP_DICT_TABS[0]
}

export function resolveGradeStageLabel(stageCode = '') {
  return GRADE_STAGE_OPTIONS.find((item) => item.value === stageCode)?.label || stageCode || '-'
}

/** 从学段字典行构建年级 Tab 的下拉选项 */
export function buildGradeStageSelectOptions(rows = []) {
  const fromDict = (rows || [])
    .filter((row) => row.status !== SETUP_DICT_STATUS.HIDDEN)
    .map((row) => ({
      label: row.name || row.value || row.code,
      value: row.code || row.value || row.name,
    }))
    .filter((item) => item.label && item.value)
  if (fromDict.length) return fromDict
  return GRADE_STAGE_OPTIONS
}

function resolveStageCodeByName(stageName = '') {
  return STAGE_NAME_OPTIONS.find((item) => item.label === String(stageName).trim())?.code || ''
}

function resolveSubjectIcon(name = '') {
  return SUBJECT_ICON_BY_NAME[String(name).trim()] || 'file-text'
}

function isKnownSubjectIcon(remark = '') {
  return KNOWN_SUBJECT_ICONS.has(String(remark || '').trim())
}

/** 解析学科 remark 中的学段编码（remark 存图标时不含学段，走默认映射） */
export function parseSubjectStageCodes(remark = '') {
  const text = String(remark || '').trim()
  if (!text || isKnownSubjectIcon(text)) {
    return []
  }
  const parsed = text.split(',')
    .map((item) => item.trim())
    .filter((item) => STAGE_CODE_SET.has(item))
  return [...new Set(parsed)]
}

/** 学科默认适用学段（与后端 SetupSubjectStageDefaults 对齐） */
export function defaultStagesForSubject(name = '') {
  const subjectName = String(name || '').trim()
  return DEFAULT_STAGES_BY_SUBJECT[subjectName] || ['junior', 'senior']
}

export function resolveSubjectStageCodes(name = '', remark = '') {
  const parsed = parseSubjectStageCodes(remark)
  return parsed.length ? parsed : defaultStagesForSubject(name)
}

export function formatSubjectStageLabels(stageCodes = []) {
  return (stageCodes || [])
    .map((code) => resolveGradeStageLabel(code))
    .filter(Boolean)
    .join('、') || '-'
}

function normalizeStageCodes(stageCodes = []) {
  return [...new Set((stageCodes || []).map((item) => String(item || '').trim()).filter((item) => STAGE_CODE_SET.has(item)))]
}

function encodeSubjectRemark(name = '', stageCodes = []) {
  const normalized = normalizeStageCodes(stageCodes)
  const defaults = defaultStagesForSubject(name)
  const isDefault = normalized.length === defaults.length
    && normalized.every((code) => defaults.includes(code))
  if (isDefault) {
    return resolveSubjectIcon(name)
  }
  return normalized.join(',')
}

function resolveSubjectCode(name = '') {
  const text = String(name || '').trim()
  const map = {
    数学: 'math',
    语文: 'chinese',
    英语: 'english',
    物理: 'physics',
    化学: 'chemistry',
    生物: 'biology',
    历史: 'history',
    地理: 'geography',
    政治: 'politics',
  }
  return map[text] || text.replace(/[^\w.-]/g, '').toLowerCase() || 'subject'
}

function resolveVersionCode(name = '') {
  const text = String(name).trim()
  if (VERSION_CODE_BY_NAME[text]) return VERSION_CODE_BY_NAME[text]
  return text.replace(/版$/, '').toLowerCase() || 'version'
}

/** 根据学段 + 年级简称，自动推荐完整名称 */
export function suggestGradeFullName(stageCode = '', shortLabel = '') {
  const shortText = String(shortLabel || '').trim()
  if (!shortText) return ''
  const preset = GRADE_FULL_NAME_BY_STAGE[stageCode]?.[shortText]
  if (preset) return preset
  const stageName = resolveGradeStageLabel(stageCode)
  if (!stageName || stageName === '-') return shortText
  if (shortText.includes(stageName)) return shortText
  return `${stageName}${shortText}`
}

/** 列表行 → 表单（隐藏后端字段） */
export function rowToSetupDictForm(tabKey, row = {}) {
  const tab = getSetupDictTab(tabKey)
  const base = { ...tab.defaultForm }
  if (tab.key === 'gradeStage') {
    return {
      ...base,
      displayName: row.name || '',
      orderNum: row.orderNum ?? 0,
      status: row.status ?? SETUP_DICT_STATUS.VISIBLE,
    }
  }
  if (tab.key === 'grade') {
    return {
      ...base,
      stageCode: row.remark || 'junior',
      shortLabel: resolveGradeShortLabel(row.code, row.name || row.value),
      fullName: row.name || row.value || '',
      orderNum: row.orderNum ?? 0,
      status: row.status ?? SETUP_DICT_STATUS.VISIBLE,
    }
  }
  if (tab.key === 'subject') {
    const displayName = row.name || row.value || ''
    return {
      ...base,
      displayName,
      stageCodes: resolveSubjectStageCodes(displayName, row.remark),
      orderNum: row.orderNum ?? 0,
      status: row.status ?? SETUP_DICT_STATUS.VISIBLE,
    }
  }
  return {
    ...base,
    displayName: row.name || row.value || '',
    orderNum: row.orderNum ?? 0,
    status: row.status ?? SETUP_DICT_STATUS.VISIBLE,
  }
}

/** 表单 → 保存 payload（自动生成 code / value 等技术字段） */
export function formToSetupDictPayload(tabKey, form = {}) {
  const tab = getSetupDictTab(tabKey)
  const orderNum = Number(form.orderNum ?? 0)
  const status = Number(form.status ?? SETUP_DICT_STATUS.VISIBLE)

  if (tab.key === 'gradeStage') {
    const displayName = String(form.displayName || '').trim()
    const stageCode = resolveStageCodeByName(displayName)
    return {
      name: displayName,
      code: stageCode || displayName,
      value: displayName,
      orderNum,
      remark: '',
      status,
    }
  }

  if (tab.key === 'grade') {
    const shortLabel = String(form.shortLabel || '').trim()
    const fullName = String(form.fullName || suggestGradeFullName(form.stageCode, shortLabel)).trim()
    return {
      name: fullName,
      code: resolveGradeCode(form.stageCode, shortLabel, fullName),
      value: fullName,
      orderNum,
      remark: String(form.stageCode || '').trim(),
      status,
    }
  }

  if (tab.key === 'subject') {
    const displayName = String(form.displayName || '').trim()
    return {
      name: displayName,
      code: resolveSubjectCode(displayName),
      value: displayName,
      orderNum,
      remark: encodeSubjectRemark(displayName, form.stageCodes),
      status,
    }
  }

  const displayName = String(form.displayName || '').trim()
  return {
    name: displayName,
    code: resolveVersionCode(displayName),
    value: displayName,
    orderNum,
    remark: '',
    status,
  }
}

