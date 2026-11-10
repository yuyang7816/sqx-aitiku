import {
  defaultStagesForSubject,
  parseSubjectStageCodes,
  resolveGradeShortLabel,
  resolveGradeStageLabel,
  resolveSubjectStageCodes,
  GRADE_STAGE_OPTIONS,
} from '@/constants/setup-dict'

export const EMPTY_SETUP_OPTIONS = {
  gradeStages: [],
  grades: [],
  subjects: [],
  versions: [],
}

export function inferStageCodeFromGradeValue(gradeValue = '') {
  const text = String(gradeValue || '').trim()
  if (text.startsWith('小学')) return 'primary'
  if (text.startsWith('高中')) return 'senior'
  if (text.startsWith('初中')) return 'junior'
  return 'junior'
}

export function inferStageNameFromCode(stageCode = '') {
  if (stageCode === 'primary') return '小学'
  if (stageCode === 'senior') return '高中'
  return '初中'
}

export function buildGradeStagesFromGrades(grades = []) {
  const ordered = []
  const seen = new Set()
  grades.forEach((item) => {
    if (!item?.stageCode || seen.has(item.stageCode)) {
      return
    }
    seen.add(item.stageCode)
    ordered.push({
      label: item.stageName || inferStageNameFromCode(item.stageCode),
      value: item.stageCode,
    })
  })
  return ordered
}

function normalizeGradeStages(rows = []) {
  const fromDict = rows
    .map((row) => ({
      label: row.name || row.value || row.code,
      value: row.code || row.value || row.name,
    }))
    .filter((item) => item.label && item.value)
  return fromDict.length ? fromDict : [...GRADE_STAGE_OPTIONS]
}

function normalizeGrades(rows = [], gradeStages = []) {
  const stageNameMap = new Map(gradeStages.map((item) => [item.value, item.label]))
  return rows.map((row) => {
    const fullName = row.value || row.name
    const code = row.code || ''
    const shortLabel = resolveGradeShortLabel(code, fullName)
    const stageCode = String(row.remark || '').trim() || inferStageCodeFromGradeValue(fullName)
    return {
      label: shortLabel,
      value: fullName,
      code,
      shortLabel,
      stageCode,
      stageName: stageNameMap.get(stageCode) || resolveGradeStageLabel(stageCode),
    }
  })
}

function normalizeSubjects(rows = []) {
  return rows.map((row) => {
    const name = row.name || row.value
    const stageCodes = resolveSubjectStageCodes(name, row.remark)
    return {
      label: name,
      value: name,
      code: row.code,
      stageCodes,
    }
  })
}

function normalizeVersions(rows = []) {
  return rows.map((row) => ({
    label: row.name || row.value,
    value: row.value || row.name,
    code: row.code,
  }))
}

/** 将 sys_dict 原始行映射为带学段元数据的选项结构 */
export function normalizeSetupOptionsFromDict({
  gradeStageRows = [],
  gradeRows = [],
  subjectRows = [],
  versionRows = [],
} = {}) {
  const gradeStages = normalizeGradeStages(gradeStageRows)
  const grades = normalizeGrades(gradeRows, gradeStages)
  return {
    gradeStages: gradeStages.length ? gradeStages : buildGradeStagesFromGrades(grades),
    grades,
    subjects: normalizeSubjects(subjectRows),
    versions: normalizeVersions(versionRows),
  }
}

export function filterGradesByStage(grades = [], stageCode = '') {
  if (!stageCode) {
    return grades
  }
  return grades.filter((item) => item.stageCode === stageCode)
}

export function filterSubjectsByStage(subjects = [], stageCode = '') {
  if (!stageCode) {
    return subjects
  }
  return subjects.filter((item) => (
    (item.stageCodes || defaultStagesForSubject(item.label)).includes(stageCode)
  ))
}

export function resolveStageCodeFromGrade(grades = [], gradeValue = '') {
  const text = String(gradeValue || '').trim()
  if (!text) {
    return ''
  }
  const matched = grades.find((item) => item.value === text)
  if (matched?.stageCode) {
    return matched.stageCode
  }
  return inferStageCodeFromGradeValue(text)
}

export function resolveGradeShortFromOptions(grades = [], gradeValue = '') {
  const matched = grades.find((item) => item.value === gradeValue)
  return matched?.shortLabel || matched?.label || ''
}

/** 解析学科 remark（供测试或特殊场景） */
export { parseSubjectStageCodes, defaultStagesForSubject }
