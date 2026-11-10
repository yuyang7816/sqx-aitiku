import { post, resolveBusinessResponse } from '@/api/http'
import { createPageApi, resolvePageData } from '@/api/admin'
import { getDictPage } from '@/api/modules/dicts'
import { SETUP_DICT_TYPE } from '@/constants/setup-dict'
import { normalizeSetupOptionsFromDict } from '@/utils/setup-options'

export const questionPaperApi = createPageApi('admin/base/question/paper')

export function updateQuestionPaperStateApi(data) {
  return post('admin/base/question/paper/updateState', data).then(resolveBusinessResponse)
}

export function searchPaperQuestionsApi(data) {
  return post('admin/base/question/paper/searchQuestions', data)
    .then(resolveBusinessResponse)
    .then(resolvePageData)
}

export function pagePaperItemsApi(data) {
  return post('admin/base/question/paper/pageItems', data)
    .then(resolveBusinessResponse)
    .then(resolvePageData)
}

export function bindPaperQuestionsApi(data) {
  return post('admin/base/question/paper/bindQuestions', data).then(resolveBusinessResponse)
}

export function unbindPaperQuestionApi(data) {
  return post('admin/base/question/paper/unbindQuestion', data).then(resolveBusinessResponse)
}

const DICT_FETCH_LIMIT = 100

async function loadDictRows(type) {
  const data = await getDictPage({ page: 1, limit: DICT_FETCH_LIMIT, type })
  return (data.list || [])
    .sort((a, b) => (a.orderNum ?? 0) - (b.orderNum ?? 0))
}

/** 加载学段/年级/学科下拉（来自年级学科字典，含学段元数据） */
export async function loadPaperSetupOptions() {
  const [gradeStageRows, gradeRows, subjectRows] = await Promise.all([
    loadDictRows(SETUP_DICT_TYPE.GRADE_STAGE),
    loadDictRows(SETUP_DICT_TYPE.GRADE),
    loadDictRows(SETUP_DICT_TYPE.SUBJECT),
  ])
  return normalizeSetupOptionsFromDict({ gradeStageRows, gradeRows, subjectRows })
}
