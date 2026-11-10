import {
  createDictApi,
  deleteDictApi,
  getDictInfoApi,
  getDictPage,
  updateDictApi,
} from '@/api/modules/dicts'
import { filterGarbledDictRows } from '@/constants/setup-dict'

const FETCH_LIMIT = 100

function filterByCode(list, code = '') {
  const codeKeyword = String(code || '').trim()
  if (!codeKeyword) return list
  return list.filter((item) => String(item.code || '').includes(codeKeyword))
}

function sortByOrder(list) {
  return [...list].sort((a, b) => (a.orderNum ?? 0) - (b.orderNum ?? 0))
}

/**
 * 分页查询指定类型的学习计划字典。
 * 后端已支持 type/name 筛选，无需一次拉取全量。
 */
export async function getSetupDictPage({ type, page = 1, limit = 10, name = '', code = '' } = {}) {
  const data = await getDictPage({
    page,
    limit,
    type,
    name: name || undefined,
    keyword: name || undefined,
  })
  const list = sortByOrder(filterByCode(data.list, code))
  const filtered = filterGarbledDictRows(list)
  return {
    list: filtered,
    total: code ? filtered.length : Math.max(0, data.total - (list.length - filtered.length)),
  }
}

export function getSetupDictInfoApi(id) {
  return getDictInfoApi(id)
}

export function createSetupDictApi(payload) {
  return createDictApi(payload)
}

export function updateSetupDictApi(payload) {
  return updateDictApi(payload)
}

export function deleteSetupDictApi(ids) {
  return deleteDictApi(ids)
}

/** 导入默认字典项（按 type + code 去重，仅新增缺失项） */
export async function importDefaultSetupDictItems(type, items) {
  const data = await getDictPage({ page: 1, limit: FETCH_LIMIT, type })
  const existingCodes = new Set((data.list || []).map((item) => item.code))
  const pending = items.filter((item) => !existingCodes.has(item.code))
  for (const item of pending) {
    await createDictApi({ ...item, type, status: 1 })
  }
  return pending.length
}

/** 拉取指定类型的字典选项（如年级学段下拉） */
export async function getSetupDictOptions(type, { limit = FETCH_LIMIT } = {}) {
  const data = await getDictPage({ page: 1, limit, type })
  return sortByOrder(filterGarbledDictRows(data.list || []))
}
