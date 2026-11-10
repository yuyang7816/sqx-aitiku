import { post, resolveBusinessResponse } from '@/api/http'
import { createPageApi } from '@/api/admin'

export const practiceEntryApi = createPageApi('admin/base/practiceEntry')

export function updatePracticeEntryStateApi(data) {
  return post('admin/base/practiceEntry/updateState', data).then(resolveBusinessResponse)
}
