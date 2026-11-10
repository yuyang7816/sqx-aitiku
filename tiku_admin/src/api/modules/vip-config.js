import {
  getSystemConfigListApi,
  refreshSystemConfigApi,
  saveSystemConfigApi,
  updateSystemConfigApi,
} from '@/api/modules/system-config'
import {
  VIP_CONFIG_DEFS,
  VIP_PARAM_TYPE,
  VIP_PARAM_TYPE_DESC,
} from '@/constants/vip-config'

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
    paramType: VIP_PARAM_TYPE,
    paramTypeDesc: VIP_PARAM_TYPE_DESC,
  }
}

export async function getVipConfigApi() {
  const groups = await getSystemConfigListApi()
  const allRows = flattenConfigList(groups)
  const keyMap = new Map(allRows.map((row) => [row.paramKey, row]))

  const records = VIP_CONFIG_DEFS.map((def) => {
    const existing = keyMap.get(def.key)
    if (existing) {
      return { ...existing, paramTypeDesc: existing.paramTypeDesc || VIP_PARAM_TYPE_DESC }
    }
    return buildDefaultRecord(def)
  })

  return { records }
}

export async function saveVipConfigApi(records) {
  for (const record of records) {
    const payload = {
      id: record.id,
      paramKey: record.paramKey,
      paramValue: String(record.paramValue ?? ''),
      status: record.status || '1',
      remark: record.remark || '',
      paramType: VIP_PARAM_TYPE,
      paramTypeDesc: VIP_PARAM_TYPE_DESC,
    }
    if (record.id) {
      await updateSystemConfigApi(payload)
    } else {
      await saveSystemConfigApi(payload)
    }
  }
  await refreshSystemConfigApi()
  return getVipConfigApi()
}
