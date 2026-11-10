import { post, resolveBusinessResponse } from '@/api/http'
import { createPageApi } from '@/api/admin'
import { getDictPage } from '@/api/modules/dicts'
import { SETUP_DICT_TYPES } from '@/constants/chapter-tree'
import { SETUP_DICT_TYPE } from '@/constants/setup-dict'
import { normalizeSetupOptionsFromDict } from '@/utils/setup-options'

export const questionChapterApi = createPageApi('admin/base/question/chapter')
export const questionSectionApi = createPageApi('admin/base/question/section')

export function updateQuestionChapterStateApi(data) {
  return post('admin/base/question/chapter/updateState', data).then(resolveBusinessResponse)
}

export function updateQuestionSectionStateApi(data) {
  return post('admin/base/question/section/updateState', data).then(resolveBusinessResponse)
}

const DICT_FETCH_LIMIT = 100

async function loadDictRows(type) {
  const data = await getDictPage({ page: 1, limit: DICT_FETCH_LIMIT, type })
  return (data.list || [])
    .sort((a, b) => (a.orderNum ?? 0) - (b.orderNum ?? 0))
}

/** 加载学段/年级/学科/版本下拉（来自年级学科字典，含学段元数据） */
export async function loadChapterSetupOptions() {
  const [gradeStageRows, gradeRows, subjectRows, versionRows] = await Promise.all([
    loadDictRows(SETUP_DICT_TYPE.GRADE_STAGE),
    loadDictRows(SETUP_DICT_TYPE.GRADE),
    loadDictRows(SETUP_DICT_TYPE.SUBJECT),
    loadDictRows(SETUP_DICT_TYPE.VERSION),
  ])
  return normalizeSetupOptionsFromDict({ gradeStageRows, gradeRows, subjectRows, versionRows })
}

/** 按筛选条件拉取章列表（供节表单选择所属章） */
export async function searchChapterOptions({ grade, subject, version, keyword = '', valueKey = 'chapterCode' } = {}) {
  const trimmed = String(keyword || '').trim()
  const data = await questionChapterApi.page({
    page: 1,
    limit: 200,
    grade: grade || undefined,
    subject: subject || undefined,
    version: version || undefined,
    title: trimmed || undefined,
  })
  return (data.list || []).map((item) => ({
    label: [item.gradeShort || item.grade, item.subject, item.title].filter(Boolean).join(' · '),
    value: valueKey === 'id' ? item.id : item.chapterCode,
    id: item.id,
    chapterCode: item.chapterCode,
    title: item.title,
    grade: item.grade,
    subject: item.subject,
    version: item.version,
  }))
}

export { SETUP_DICT_TYPES }
