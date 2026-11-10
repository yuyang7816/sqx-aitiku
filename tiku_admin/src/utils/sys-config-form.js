import {
  getSystemConfigListApi,
  refreshSystemConfigApi,
  saveSystemConfigApi,
  updateSystemConfigApi,
} from '@/api/modules/system-config'
import { percentToStored, storedToPercent } from '@/constants/promo-config'

export function flattenConfigList(groups) {
  return (groups || []).flatMap((group) => group.sysConfigEntityList || group.list || [])
}

export function buildDefaultRecord(def, paramType, paramTypeDesc) {
  return {
    id: null,
    paramKey: def.key,
    paramValue: def.defaultValue ?? '',
    status: '1',
    remark: def.remark || def.hint || '',
    paramType,
    paramTypeDesc,
  }
}

export async function loadSysConfigByDefs(defs, paramType, paramTypeDesc) {
  const groups = await getSystemConfigListApi()
  const keyMap = new Map(flattenConfigList(groups).map((row) => [row.paramKey, row]))

  return defs.map((def) => {
    const existing = keyMap.get(def.key)
      || (def.fallbackKey ? keyMap.get(def.fallbackKey) : null)
    if (existing) {
      return { ...existing, paramTypeDesc: existing.paramTypeDesc || paramTypeDesc }
    }
    return buildDefaultRecord(def, paramType, paramTypeDesc)
  })
}

export async function saveSysConfigRecords(records, paramType, paramTypeDesc) {
  for (const record of records) {
    const payload = {
      id: record.id,
      paramKey: record.paramKey,
      paramValue: String(record.paramValue ?? ''),
      status: record.status || '1',
      remark: record.remark || '',
      paramType: record.paramType || paramType,
      paramTypeDesc: record.paramTypeDesc || paramTypeDesc,
    }
    if (record.id) {
      await updateSystemConfigApi(payload)
    } else {
      await saveSystemConfigApi(payload)
    }
  }
  await refreshSystemConfigApi()
}

export function recordsToForm(records, defs) {
  const keyMap = new Map(records.map((row) => [row.paramKey, row]))
  const form = {}
  defs.forEach((def) => {
    const raw = keyMap.get(def.key)?.paramValue ?? def.defaultValue ?? ''
    if (def.type === 'number') {
      if (def.display === 'percent' || def.display === 'amount') {
        form[def.key] = storedToPercent(raw, def.display)
        return
      }
      form[def.key] = raw === '' ? undefined : Number(raw)
      return
    }
    if (def.type === 'switch') {
      form[def.key] = String(raw) === '1'
      return
    }
    form[def.key] = raw
  })
  return form
}

export function formToRecords(form, records, defs) {
  const keyMap = new Map(records.map((row) => [row.paramKey, row]))
  return defs.map((def) => {
    const existing = keyMap.get(def.key) || buildDefaultRecord(def, def.paramType, def.paramTypeDesc)
    let value = form[def.key]
    if (def.type === 'switch') {
      value = value ? '1' : '0'
    } else if (def.type === 'number' && (def.display === 'percent' || def.display === 'amount')) {
      value = percentToStored(value, def.display)
    } else if (value === undefined || value === null) {
      value = ''
    } else {
      value = String(value)
    }
    return {
      ...existing,
      paramKey: def.key,
      paramValue: value,
      remark: def.remark || def.hint || existing.remark || '',
      paramType: def.paramType || existing.paramType,
      paramTypeDesc: def.paramTypeDesc || existing.paramTypeDesc,
    }
  })
}

export function buildFieldRules(defs, form = {}) {
  const rules = {}
  defs.forEach((def) => {
    if (typeof def.showWhen === 'function' && !def.showWhen(form)) {
      return
    }
    const fieldRules = []
    if (def.required) {
      fieldRules.push({ required: true, message: `请填写${def.label}`, trigger: def.type === 'select' ? 'change' : 'blur' })
    }
    if (typeof def.requiredWhen === 'function' && def.requiredWhen(form)) {
      fieldRules.push({ required: true, message: `请填写${def.label}`, trigger: 'blur' })
    }
    if (def.type === 'number' && (def.min !== undefined || def.max !== undefined)) {
      fieldRules.push({
        validator: (_rule, value, callback) => {
          const num = Number(value)
          if (Number.isNaN(num)
            || (def.min !== undefined && num < def.min)
            || (def.max !== undefined && num > def.max)) {
            callback(new Error(`${def.label}范围为 ${def.min}~${def.max}${def.unit || ''}`))
            return
          }
          callback()
        },
        trigger: 'change',
      })
    }
    if (typeof def.getRules === 'function') {
      fieldRules.push(...def.getRules(form))
    }
    if (def.maxLength) {
      fieldRules.push({ max: def.maxLength, message: `${def.label}不超过 ${def.maxLength} 字`, trigger: 'blur' })
    }
    if (fieldRules.length) {
      rules[def.key] = fieldRules
    }
  })
  return rules
}

export function isDefVisible(def, form) {
  if (typeof def.showWhen === 'function') {
    return def.showWhen(form)
  }
  return true
}

/** 保存时将主键值同步到关联键，避免多处重复填写 */
export async function syncMirrorConfigKeys(form, mirrorKeys, paramType, paramTypeDesc) {
  const groups = await getSystemConfigListApi()
  const keyMap = new Map(flattenConfigList(groups).map((row) => [row.paramKey, row]))
  const tasks = []

  Object.entries(mirrorKeys).forEach(([sourceKey, targetKeys]) => {
    const sourceValue = form[sourceKey]
    if (sourceValue === undefined || sourceValue === null) return
    const paramValue = typeof sourceValue === 'boolean'
      ? (sourceValue ? '1' : '0')
      : String(sourceValue)

    targetKeys.forEach((targetKey) => {
      const existing = keyMap.get(targetKey)
      const payload = {
        id: existing?.id || null,
        paramKey: targetKey,
        paramValue,
        status: existing?.status || '1',
        remark: existing?.remark || '',
        paramType: existing?.paramType || paramType,
        paramTypeDesc: existing?.paramTypeDesc || paramTypeDesc,
      }
      tasks.push(existing?.id ? updateSystemConfigApi(payload) : saveSystemConfigApi(payload))
    })
  })

  if (tasks.length) {
    await Promise.all(tasks)
    await refreshSystemConfigApi()
  }
}
