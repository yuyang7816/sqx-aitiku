import { post, resolveBusinessResponse } from '@/api/http'
import { createPageApi } from '@/api/admin'
import { questionSectionApi } from '@/api/modules/chapter-tree'

export const questionApi = createPageApi('admin/base/question')

export function updateQuestionStateApi(data) {
  return post('admin/base/question/updateState', data).then(resolveBusinessResponse)
}

export function importQuestionsApi(data) {
  return post('admin/base/question/import', data).then(resolveBusinessResponse)
}

export function searchKnowledgePointOptions({ grade, subject, keyword = '' } = {}) {
  return post('admin/base/question/knowledgeOptions', {
    grade: grade || undefined,
    subject: subject || undefined,
    keyword: keyword || undefined,
  }).then(resolveBusinessResponse)
}

export function searchPaperOptions({ grade, subject, paperType, keyword = '' } = {}) {
  return post('admin/base/question/paperOptions', {
    grade: grade || undefined,
    subject: subject || undefined,
    paperType: paperType || undefined,
    keyword: keyword || undefined,
  }).then(resolveBusinessResponse)
}

/** 按章拉取节下拉 */
export async function searchSectionOptions({ chapterCode, keyword = '' } = {}) {
  if (!chapterCode) {
    return []
  }
  const trimmed = String(keyword || '').trim()
  const data = await questionSectionApi.page({
    page: 1,
    limit: 200,
    chapterCode,
    title: trimmed || undefined,
  })
  return (data.list || []).map((item) => ({
    label: item.title,
    value: item.sectionCode,
    title: item.title,
  }))
}

export { loadChapterSetupOptions, searchChapterOptions } from '@/api/modules/chapter-tree'
