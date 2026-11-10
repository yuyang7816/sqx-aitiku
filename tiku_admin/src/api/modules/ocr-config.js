import {
  getSystemConfigListApi,
  refreshSystemConfigApi,
  saveSystemConfigApi,
  updateSystemConfigApi,
} from '@/api/modules/system-config'
import {
  OCR_CONFIG_DEFS,
  OCR_PARAM_TYPE,
  OCR_PARAM_TYPE_DESC,
} from '@/constants/ocr-config'

function flattenConfigList(groups) {
  return (groups || []).flatMap((group) => group.sysConfigEntityList || group.list || [])
}

function buildDefaultRecord(def) {
  return {
    id: null,
    paramKey: def.key,
    paramValue: def.defaultValue,
    status: '1',
    remark: def.remark,
    paramType: OCR_PARAM_TYPE,
    paramTypeDesc: OCR_PARAM_TYPE_DESC,
  }
}

/**
 * 读取 OCR 配置（按 param_key 合并默认值）
 */
export async function getOcrConfigApi() {
  const groups = await getSystemConfigListApi()
  const allRows = flattenConfigList(groups)
  const keyMap = new Map(allRows.map((row) => [row.paramKey, row]))

  const records = OCR_CONFIG_DEFS.map((def) => {
    const existing = keyMap.get(def.key)
    if (existing) {
      return { ...existing, paramTypeDesc: existing.paramTypeDesc || OCR_PARAM_TYPE_DESC }
    }
    return buildDefaultRecord(def)
  })

  return { records }
}

/**
 * 保存 OCR 配置（存在则更新，不存在则新增），保存后返回最新配置
 */
export async function saveOcrConfigApi(records) {
  for (const record of records) {
    const payload = {
      id: record.id,
      paramKey: record.paramKey,
      paramValue: String(record.paramValue ?? ''),
      status: record.status || '1',
      remark: record.remark || '',
      paramType: OCR_PARAM_TYPE,
      paramTypeDesc: OCR_PARAM_TYPE_DESC,
    }
    if (record.id) {
      await updateSystemConfigApi(payload)
    } else {
      await saveSystemConfigApi(payload)
    }
  }
  await refreshSystemConfigApi()
  return getOcrConfigApi()
}
