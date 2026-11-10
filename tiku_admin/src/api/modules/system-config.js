import { get, post, resolveBusinessResponse } from '@/api/http'

export function getSystemConfigListApi(params = {}) {
  return get('/sys/config/list', params)
    .then(resolveBusinessResponse)
    .then((data) => (Array.isArray(data) ? data : []))
}

export function getSystemConfigInfoApi(id) {
  return get(`/sys/config/info/${id}`).then(resolveBusinessResponse)
}

export function saveSystemConfigApi(data) {
  return post('/sys/config/save', data).then(resolveBusinessResponse)
}

export function updateSystemConfigApi(data) {
  return post('/sys/config/update', data).then(resolveBusinessResponse)
}

export function deleteSystemConfigApi(ids) {
  return post('/sys/config/delete', ids).then(resolveBusinessResponse)
}

export function refreshSystemConfigApi() {
  return get('/sys/config/refresh').then(resolveBusinessResponse)
}

export function uploadFileApi(file) {
  const formData = new FormData()
  formData.append('file', file)
  return post('file/upload', formData).then(resolveBusinessResponse)
}
