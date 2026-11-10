import { post, resolveBusinessResponse } from '@/api/http'
import { createPageApi } from '@/api/admin'

export const homeShortcutApi = createPageApi('admin/base/homeShortcut')

export function updateHomeShortcutStateApi(data) {
  return post('admin/base/homeShortcut/updateState', data).then(resolveBusinessResponse)
}
