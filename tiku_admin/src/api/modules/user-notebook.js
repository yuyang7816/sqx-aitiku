import { post, resolveBusinessResponse } from '@/api/http'
import { resolvePageData } from '@/api/admin'

/** 有错题的用户汇总分页（每用户一行） */
export function getUserNotebookUserPageApi(data) {
  return post('admin/userNotebook/userPage', data).then(resolveBusinessResponse).then(resolvePageData)
}

/** 指定用户的错题分页列表 */
export function getUserNotebookPageApi(data) {
  return post('admin/userNotebook/page', data).then(resolveBusinessResponse).then(resolvePageData)
}
