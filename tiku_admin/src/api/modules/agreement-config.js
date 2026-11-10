import {
  getSystemConfigListApi,
  refreshSystemConfigApi,
  saveSystemConfigApi,
  updateSystemConfigApi,
} from '@/api/modules/system-config'
import {
  ARTICLE_CONFIG_DEFS,
  ARTICLE_PARAM_TYPE,
  ARTICLE_PARAM_TYPE_DESC,
  ARTICLE_TYPES,
} from '@/constants/agreement-config'

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
    paramType: ARTICLE_PARAM_TYPE,
    paramTypeDesc: ARTICLE_PARAM_TYPE_DESC,
  }
}

/**
 * 读取协议与政策配置（按 param_key 合并默认值）
 */
export async function getAgreementConfigApi() {
  const groups = await getSystemConfigListApi()
  const allRows = flattenConfigList(groups)
  const keyMap = new Map(allRows.map((row) => [row.paramKey, row]))

  const records = ARTICLE_CONFIG_DEFS.map((def) => {
    const existing = keyMap.get(def.key)
    if (existing) {
      return { ...existing, paramTypeDesc: existing.paramTypeDesc || ARTICLE_PARAM_TYPE_DESC }
    }
    return buildDefaultRecord(def)
  })

  const articles = ARTICLE_TYPES.map((typeDef) => {
    const pick = (field) =>
      records.find((row) => row.paramKey === typeDef[`${field}Key`])?.paramValue ??
      typeDef[`default${field.charAt(0).toUpperCase()}${field.slice(1)}`]
    return {
      type: typeDef.type,
      label: typeDef.label,
      title: pick('title'),
      sub: pick('sub'),
      content: pick('content'),
    }
  })

  return { records, articles }
}

/**
 * 保存协议与政策配置，保存后刷新缓存并返回最新数据
 * @param {Array<{ id: number|null, paramKey: string, paramValue: string, status?: string, remark?: string }>} records
 */
export async function saveAgreementConfigApi(records) {
  for (const record of records) {
    const def = ARTICLE_CONFIG_DEFS.find((item) => item.key === record.paramKey)
    const payload = {
      id: record.id,
      paramKey: record.paramKey,
      paramValue: String(record.paramValue ?? ''),
      status: record.status || '1',
      remark: record.remark || def?.remark || '',
      paramType: ARTICLE_PARAM_TYPE,
      paramTypeDesc: ARTICLE_PARAM_TYPE_DESC,
    }
    if (record.id) {
      await updateSystemConfigApi(payload)
    } else {
      await saveSystemConfigApi(payload)
    }
  }
  await refreshSystemConfigApi()
  return getAgreementConfigApi()
}
