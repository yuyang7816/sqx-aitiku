/** 列表筛选表单通用工具 */

export const FILTER_SELECT_EMPTY = undefined

export function isFilterEmpty(value) {
  return value === undefined || value === null || value === ''
}

export function resolveFilterOptionLabel(value, options = []) {
  const matched = (options || []).find((item) => item.value === value)
  return matched?.label ?? String(value ?? '')
}

export function buildFilterSummaryItems(filters = {}, fields = []) {
  const items = []
  for (const field of fields) {
    const value = filters[field.key]
    if (isFilterEmpty(value)) continue
    let display = value
    if (field.type === 'select') {
      display = resolveFilterOptionLabel(value, field.options)
    } else if (Array.isArray(value)) {
      display = value.join(' ~ ')
    }
    items.push({
      key: field.key,
      label: field.label,
      value: display,
    })
  }
  return items
}

export function normalizeSelectFilterQuery(value) {
  return isFilterEmpty(value) ? undefined : value
}

export function normalizeNumberFilterQuery(value) {
  return isFilterEmpty(value) ? undefined : Number(value)
}
