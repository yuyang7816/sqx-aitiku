<script setup>
import FilterSelect from '@/components/common/FilterSelect.vue'
import FilterSummary from '@/components/common/FilterSummary.vue'
import { useFilterFormKey } from '@/composables/useFilterFormKey'
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Link, Refresh, Search } from '@element-plus/icons-vue'
import {
  API_DOC_CONVENTIONS,
  API_DOC_MARKDOWN_INDEX,
  API_SECTION_OPTIONS,
  APP_API_ENDPOINTS,
  HTTP_METHOD_OPTIONS,
} from '@/constants/api-doc'
import { getApiBaseUrl, getOpenApiDocsUrl, getSwaggerDocUrl } from '@/utils/tool'
import { FILTER_SELECT_EMPTY, isFilterEmpty } from '@/utils/filter-form'

const AUTH_FILTER_OPTIONS = [
  { label: '需登录', value: 'yes' },
  { label: '白名单', value: 'no' },
]

const activeTab = ref('catalog')
const iframeLoading = ref(true)
const iframeBlocked = ref(false)
const swaggerReachable = ref(null)
const { filterFormKey, bumpFilterFormKey } = useFilterFormKey()

const filters = reactive({
  keyword: '',
  module: FILTER_SELECT_EMPTY,
  method: FILTER_SELECT_EMPTY,
  section: FILTER_SELECT_EMPTY,
  auth: FILTER_SELECT_EMPTY,
})

const moduleOptions = computed(() => {
  const set = new Set(APP_API_ENDPOINTS.map((item) => item.module))
  return Array.from(set).sort()
})

const moduleSelectOptions = computed(() => moduleOptions.value.map((item) => ({ label: item, value: item })))
const methodSelectOptions = computed(() => HTTP_METHOD_OPTIONS.map((item) => ({ label: item, value: item })))
const sectionSelectOptions = computed(() => API_SECTION_OPTIONS.filter((item) => item.value))

const FILTER_FIELDS = computed(() => [
  { key: 'keyword', label: '关键词', type: 'text', placeholder: '路径 / 说明 / 模块' },
  { key: 'module', label: '模块', type: 'select', options: moduleSelectOptions.value, placeholder: '全部模块' },
  { key: 'method', label: '方法', type: 'select', options: methodSelectOptions.value, placeholder: '全部方法' },
  { key: 'section', label: '章节', type: 'select', options: sectionSelectOptions.value, placeholder: '全部章节' },
  { key: 'auth', label: '鉴权', type: 'select', options: AUTH_FILTER_OPTIONS, placeholder: '全部鉴权' },
])

const filteredEndpoints = computed(() =>
  APP_API_ENDPOINTS.filter((item) => {
    if (!isFilterEmpty(filters.module) && item.module !== filters.module) return false
    if (!isFilterEmpty(filters.method) && item.method !== filters.method) return false
    if (!isFilterEmpty(filters.section) && item.section !== filters.section) return false
    if (filters.auth === 'yes' && !item.auth) return false
    if (filters.auth === 'no' && item.auth) return false
    if (filters.keyword) {
      const key = filters.keyword.trim().toLowerCase()
      const haystack = `${item.path} ${item.summary} ${item.module}`.toLowerCase()
      if (!haystack.includes(key)) return false
    }
    return true
  }),
)

const stats = computed(() => ({
  total: APP_API_ENDPOINTS.length,
  auth: APP_API_ENDPOINTS.filter((item) => item.auth).length,
  public: APP_API_ENDPOINTS.filter((item) => !item.auth).length,
  filtered: filteredEndpoints.value.length,
}))

const swaggerDocUrl = computed(() => getSwaggerDocUrl())
const openApiUrl = computed(() => getOpenApiDocsUrl())
const apiBaseUrl = computed(() => getApiBaseUrl().replace(/\/$/, ''))

function methodTagType(method) {
  const map = { GET: 'success', POST: 'primary', PUT: 'warning', DELETE: 'danger' }
  return map[method] || 'info'
}

function resetFilters() {
  Object.assign(filters, {
    keyword: '',
    module: FILTER_SELECT_EMPTY,
    method: FILTER_SELECT_EMPTY,
    section: FILTER_SELECT_EMPTY,
    auth: FILTER_SELECT_EMPTY,
  })
  bumpFilterFormKey()
}

function openSwagger() {
  window.open(swaggerDocUrl.value, '_blank', 'noopener,noreferrer')
}

function openOpenApiJson() {
  window.open(openApiUrl.value, '_blank', 'noopener,noreferrer')
}

function copyText(text) {
  navigator.clipboard?.writeText(text)
  ElMessage.success('已复制到剪贴板')
}

function handleIframeLoad() {
  iframeLoading.value = false
}

function handleIframeError() {
  iframeLoading.value = false
  iframeBlocked.value = true
}

function reloadIframe() {
  iframeBlocked.value = false
  iframeLoading.value = true
  const iframe = document.getElementById('swagger-iframe')
  if (iframe) {
    iframe.src = swaggerDocUrl.value
  }
}

async function probeSwagger() {
  try {
    const res = await fetch(openApiUrl.value, { method: 'GET', mode: 'cors' })
    swaggerReachable.value = res.ok
  } catch {
    swaggerReachable.value = false
  }
}

onMounted(probeSwagger)
</script>

<template>
  <div class="api-doc-page">
    <el-alert
      type="info"
      :closable="false"
      show-icon
      class="intro-alert"
      title="用户端 API 文档（uniapp 对接）"
      :description="`Base URL：${apiBaseUrl} · 响应体 ${API_DOC_CONVENTIONS.responseShape} · 成功 code=${API_DOC_CONVENTIONS.successCode} · 需登录接口请求头 ${API_DOC_CONVENTIONS.authHeader}`"
    />

    <el-row :gutter="16" class="stat-row">
      <el-col :xs="12" :sm="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-value">{{ stats.total }}</div>
          <div class="stat-label">/app/** 接口条目</div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-value">{{ stats.auth }}</div>
          <div class="stat-label">需登录</div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-value">{{ stats.public }}</div>
          <div class="stat-label">白名单（无需 token）</div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-value stat-link">
            <el-tag :type="swaggerReachable === true ? 'success' : swaggerReachable === false ? 'danger' : 'info'" size="small">
              {{ swaggerReachable === true ? '在线' : swaggerReachable === false ? '离线' : '检测中' }}
            </el-tag>
          </div>
          <div class="stat-label">Knife4j 文档服务</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="main-card">
      <template #header>
        <div class="card-header">
          <strong>API 文档中心</strong>
          <div class="header-actions">
            <el-button :icon="Link" @click="openSwagger">新窗口打开 Swagger</el-button>
            <el-button @click="openOpenApiJson">OpenAPI JSON</el-button>
          </div>
        </div>
      </template>

      <el-tabs v-model="activeTab">
        <el-tab-pane label="接口目录" name="catalog">
          <el-form :key="filterFormKey" :inline="true" class="filter-form" @submit.prevent>
            <el-form-item label="关键词">
              <el-input
                v-model="filters.keyword"
                clearable
                placeholder="路径 / 说明 / 模块"
                style="width: 200px"
              />
            </el-form-item>
            <el-form-item label="模块">
              <FilterSelect v-model="filters.module" clearable placeholder="全部模块" style="width: 180px">
                <el-option v-for="item in moduleOptions" :key="item" :label="item" :value="item" />
              </FilterSelect>
            </el-form-item>
            <el-form-item label="方法">
              <FilterSelect v-model="filters.method" clearable placeholder="全部方法" style="width: 100px">
                <el-option v-for="item in HTTP_METHOD_OPTIONS" :key="item" :label="item" :value="item" />
              </FilterSelect>
            </el-form-item>
            <el-form-item label="章节">
              <FilterSelect v-model="filters.section" clearable placeholder="全部章节" style="width: 140px">
                <el-option
                  v-for="item in API_SECTION_OPTIONS.filter((o) => o.value)"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </FilterSelect>
            </el-form-item>
            <el-form-item label="鉴权">
              <FilterSelect v-model="filters.auth" clearable placeholder="全部鉴权" style="width: 110px">
                <el-option label="需登录" value="yes" />
                <el-option label="白名单" value="no" />
              </FilterSelect>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :icon="Search">筛选 {{ stats.filtered }} 条</el-button>
              <el-button @click="resetFilters">重置</el-button>
            </el-form-item>
          </el-form>
          <FilterSummary :filters="filters" :fields="FILTER_FIELDS" />

          <el-table :data="filteredEndpoints" border stripe size="small" empty-text="无匹配接口，请调整筛选项">
            <el-table-column prop="section" label="章节" width="88" align="center">
              <template #default="{ row }">
                <el-tag size="small" :type="row.section === '21.2' ? 'warning' : row.section === '21.1' ? 'success' : 'info'">
                  {{ row.section }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="module" label="Swagger 模块" min-width="160" show-overflow-tooltip />
            <el-table-column prop="method" label="方法" width="88" align="center">
              <template #default="{ row }">
                <el-tag size="small" :type="methodTagType(row.method)">{{ row.method }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="path" label="路径" min-width="220">
              <template #default="{ row }">
                <code class="path-code">{{ row.path }}</code>
              </template>
            </el-table-column>
            <el-table-column prop="summary" label="说明" min-width="200" show-overflow-tooltip />
            <el-table-column prop="auth" label="鉴权" width="88" align="center">
              <template #default="{ row }">
                <el-tag size="small" :type="row.auth ? 'danger' : 'success'">
                  {{ row.auth ? '需登录' : '白名单' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="docFile" label="对照文档" width="160" show-overflow-tooltip>
              <template #default="{ row }">
                <span v-if="row.docFile" class="doc-ref">tiku_server/docs/{{ row.docFile }}</span>
                <span v-else class="text-muted">—</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100" fixed="right" align="center">
              <template #default="{ row }">
                <el-button link type="primary" @click="copyText(`${row.method} ${apiBaseUrl}${row.path}`)">
                  复制
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="Swagger 在线文档" name="swagger">
          <div class="swagger-toolbar">
            <span class="swagger-hint">
              Knife4j 分组「{{ API_DOC_CONVENTIONS.swaggerGroup }}」，含全部 /app/** 入参出参与在线调试
            </span>
            <el-button :icon="Refresh" @click="reloadIframe">刷新</el-button>
          </div>

          <el-alert
            v-if="iframeBlocked || swaggerReachable === false"
            type="warning"
            :closable="false"
            show-icon
            class="swagger-fallback"
            title="无法内嵌 Swagger 或文档服务未启动"
            description="请确认 tiku_server 已启动（端口 9165），点击下方按钮在新窗口打开 Knife4j。"
          >
            <el-button type="primary" :icon="Link" @click="openSwagger">打开 {{ swaggerDocUrl }}</el-button>
          </el-alert>

          <div v-loading="iframeLoading" class="swagger-frame-wrap">
            <iframe
              id="swagger-iframe"
              :src="swaggerDocUrl"
              title="Knife4j Swagger 文档"
              class="swagger-iframe"
              @load="handleIframeLoad"
              @error="handleIframeError"
            />
          </div>
        </el-tab-pane>

        <el-tab-pane label="Markdown 对照" name="markdown">
          <p class="markdown-tip">
            以下 Markdown 位于服务端仓库 <code>tiku_server/docs/</code>，与 Swagger 互为补充；产品/前端可结合本页「接口目录」与 Knife4j 查阅。
          </p>
          <el-table :data="API_DOC_MARKDOWN_INDEX" border size="small">
            <el-table-column prop="section" label="章节" width="88" align="center" />
            <el-table-column prop="title" label="文档标题" min-width="200" />
            <el-table-column prop="file" label="文件路径" min-width="240">
              <template #default="{ row }">
                <code>tiku_server/docs/{{ row.file }}</code>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<style scoped lang="scss">
.api-doc-page {
  .intro-alert {
    margin-bottom: 16px;
  }

  .stat-row {
    margin-bottom: 16px;
  }

  .stat-card {
    text-align: center;

    .stat-value {
      font-size: 24px;
      font-weight: 600;
      color: var(--el-color-primary);
      line-height: 1.2;
    }

    .stat-label {
      margin-top: 6px;
      font-size: 12px;
      color: var(--el-text-color-secondary);
    }
  }

  .main-card {
    .card-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      flex-wrap: wrap;
      gap: 12px;
    }

    .header-actions {
      display: flex;
      gap: 8px;
      flex-wrap: wrap;
    }
  }

  .filter-form {
    margin-bottom: 12px;
  }

  .path-code {
    font-size: 12px;
    color: var(--el-color-primary);
  }

  .doc-ref {
    font-size: 12px;
    color: var(--el-text-color-secondary);
  }

  .text-muted {
    color: var(--el-text-color-placeholder);
  }

  .swagger-toolbar {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 12px;
    flex-wrap: wrap;
    gap: 8px;
  }

  .swagger-hint {
    font-size: 13px;
    color: var(--el-text-color-regular);
  }

  .swagger-fallback {
    margin-bottom: 12px;
  }

  .swagger-frame-wrap {
    min-height: 720px;
    border: 1px solid var(--el-border-color-light);
    border-radius: 6px;
    overflow: hidden;
  }

  .swagger-iframe {
    width: 100%;
    height: 720px;
    border: none;
    display: block;
  }

  .markdown-tip {
    margin: 0 0 12px;
    font-size: 13px;
    color: var(--el-text-color-regular);
    line-height: 1.6;
  }
}
</style>
