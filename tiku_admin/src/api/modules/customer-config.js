import {
  getSystemConfigListApi,
  refreshSystemConfigApi,
  saveSystemConfigApi,
  updateSystemConfigApi,
} from '@/api/modules/system-config'
import {
  CUSTOMER_CONFIG_DEFS,
  CUSTOMER_PARAM_TYPE,
  CUSTOMER_PARAM_TYPE_DESC,
  KEFU_ENABLED_KEY,
} from '@/constants/customer-config'

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
    paramType: CUSTOMER_PARAM_TYPE,
    paramTypeDesc: CUSTOMER_PARAM_TYPE_DESC,
  }
}

/**
 * 读取客服配置（按 param_key 合并默认值）
 */
export async function getCustomerConfigApi() {
  const groups = await getSystemConfigListApi()
  const allRows = flattenConfigList(groups)
  const keyMap = new Map(allRows.map((row) => [row.paramKey, row]))

  const records = CUSTOMER_CONFIG_DEFS.map((def) => {
    const existing = keyMap.get(def.key)
    if (existing) {
      return { ...existing, paramTypeDesc: existing.paramTypeDesc || CUSTOMER_PARAM_TYPE_DESC }
    }
    return buildDefaultRecord(def)
  })

  const kefuEnabledRow = keyMap.get(KEFU_ENABLED_KEY)
  const kefuEnabled = kefuEnabledRow ? kefuEnabledRow.paramValue !== '0' : true

  return { records, kefuEnabled }
}

/**
 * 保存客服配置（存在则更新，不存在则新增），保存后返回最新配置
 */
export async function saveCustomerConfigApi(records) {
  for (const record of records) {
    const payload = {
      id: record.id,
      paramKey: record.paramKey,
      paramValue: String(record.paramValue ?? ''),
      status: record.status || '1',
      remark: record.remark || '',
      paramType: CUSTOMER_PARAM_TYPE,
      paramTypeDesc: CUSTOMER_PARAM_TYPE_DESC,
    }
    if (record.id) {
      await updateSystemConfigApi(payload)
    } else {
      await saveSystemConfigApi(payload)
    }
  }
  await refreshSystemConfigApi()
  return getCustomerConfigApi()
}
