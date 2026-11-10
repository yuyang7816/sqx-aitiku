<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { getAgreementConfigApi, saveAgreementConfigApi } from '@/api/modules/agreement-config'
import RichTextEditor from '@/components/RichTextEditor.vue'
import {
  ARTICLE_CONFIG_DEFS,
  ARTICLE_TYPES,
} from '@/constants/agreement-config'
import { validateForm } from '@/utils/form'
import { showSuccess } from '@/utils/modal'
import { isAuth } from '@/utils/tool'
import { useSystemConfigEmbedded } from '@/composables/useSystemConfigEmbedded'

const embedded = useSystemConfigEmbedded()

const loading = ref(false)
const saving = ref(false)
const activeTab = ref(ARTICLE_TYPES[0].type)
const formRef = ref()
const configIds = reactive({})

const form = reactive(
  Object.fromEntries(
    ARTICLE_TYPES.flatMap((item) => [
      [`${item.type}_title`, item.defaultTitle],
      [`${item.type}_sub`, item.defaultSub],
      [`${item.type}_content`, item.defaultContent],
    ]),
  ),
)

const rules = Object.fromEntries(
  ARTICLE_TYPES.flatMap((item) => [
    [
      `${item.type}_title`,
      [
        { required: true, message: `请输入${item.label}标题`, trigger: 'blur' },
        { max: 32, message: '标题不超过 32 字', trigger: 'blur' },
      ],
    ],
    [
      `${item.type}_sub`,
      [
        { required: true, message: `请输入${item.label}副标题`, trigger: 'blur' },
        { max: 120, message: '副标题不超过 120 字', trigger: 'blur' },
      ],
    ],
    [
      `${item.type}_content`,
      [
        {
          validator: (_rule, value, callback) => {
            const text = String(value || '')
              .replace(/<[^>]+>/g, '')
              .trim()
            if (!text) {
              callback(new Error(`请填写${item.label}正文`))
              return
            }
            callback()
          },
          trigger: 'blur',
        },
      ],
    ],
  ]),
)

const activeArticle = computed(() => ARTICLE_TYPES.find((item) => item.type === activeTab.value))
const canSave = computed(() => isAuth('sys:config:update') || isAuth('sys:config:save'))

function applyData({ records }) {
  records.forEach((row) => {
    configIds[row.paramKey] = row.id
    const def = ARTICLE_CONFIG_DEFS.find((item) => item.key === row.paramKey)
    if (!def) return
    form[`${def.articleType}_${def.field}`] = row.paramValue ?? def.defaultValue
  })
}

function buildSaveRecords() {
  return ARTICLE_CONFIG_DEFS.map((def) => ({
    id: configIds[def.key] || null,
    paramKey: def.key,
    paramValue: form[`${def.articleType}_${def.field}`] ?? def.defaultValue,
    status: '1',
    remark: def.remark,
  }))
}

async function loadData() {
  loading.value = true
  try {
    const data = await getAgreementConfigApi()
    applyData(data)
  } finally {
    loading.value = false
  }
}

async function handleSave() {
  const valid = await validateForm(formRef)
  if (!valid) return
  saving.value = true
  try {
    const refreshed = await saveAgreementConfigApi(buildSaveRecords())
    applyData(refreshed)
    showSuccess('协议内容已保存')
  } finally {
    saving.value = false
  }
}

onMounted(loadData)
</script>

<template>
  <div v-loading="loading" class="agreement-config-page">
    <div v-if="canSave" class="config-toolbar">
      <strong v-if="!embedded" class="config-toolbar__title">协议 CMS</strong>
      <el-button type="primary" :loading="saving" @click="handleSave">保存配置</el-button>
    </div>

    <el-tabs v-model="activeTab" class="article-tabs">
      <el-tab-pane
        v-for="item in ARTICLE_TYPES"
        :key="item.type"
        :label="item.label"
        :name="item.type"
      />
    </el-tabs>

    <el-form ref="formRef" :model="form" :rules="rules" class="config-list-form">
      <table class="config-table">
        <colgroup>
          <col class="config-table__label-col" />
          <col class="config-table__value-col" />
        </colgroup>
        <tbody>
          <tr class="config-table__row">
            <td class="config-table__label">{{ activeArticle?.label }}标题</td>
            <td class="config-table__value">
              <el-form-item :prop="`${activeTab}_title`" label-width="0" class="config-table__field">
                <el-input
                  v-model="form[`${activeTab}_title`]"
                  maxlength="32"
                  show-word-limit
                  style="width: 360px"
                />
              </el-form-item>
            </td>
          </tr>
          <tr class="config-table__row">
            <td class="config-table__label">{{ activeArticle?.label }}副标题</td>
            <td class="config-table__value">
              <el-form-item :prop="`${activeTab}_sub`" label-width="0" class="config-table__field">
                <el-input
                  v-model="form[`${activeTab}_sub`]"
                  maxlength="120"
                  show-word-limit
                  type="textarea"
                  :rows="2"
                  style="width: 100%"
                />
              </el-form-item>
            </td>
          </tr>
          <tr class="config-table__row">
            <td class="config-table__label">{{ activeArticle?.label }}正文</td>
            <td class="config-table__value">
              <el-form-item :prop="`${activeTab}_content`" label-width="0" class="config-table__field">
                <RichTextEditor
                  v-model="form[`${activeTab}_content`]"
                  min-height="320px"
                />
              </el-form-item>
            </td>
          </tr>
        </tbody>
      </table>
    </el-form>
  </div>
</template>

<style scoped lang="scss">
.agreement-config-page {
  .config-toolbar {
    display: flex;
    align-items: center;
    justify-content: flex-end;
    gap: 12px;
    margin-bottom: 16px;
  }

  .config-toolbar__title {
    margin-right: auto;
    font-size: 15px;
  }

  .article-tabs {
    margin-bottom: 16px;
  }

  .config-table {
    width: 100%;
    border-collapse: collapse;
    table-layout: fixed;
    border: 1px solid var(--el-border-color-lighter);
    background: var(--el-bg-color);
  }

  .config-table__label-col {
    width: 200px;
  }

  .config-table__row + .config-table__row {
    border-top: 1px solid var(--el-border-color-lighter);
  }

  .config-table__label {
    padding: 12px 16px;
    font-size: 14px;
    color: var(--el-text-color-regular);
    background: var(--el-fill-color-light);
    vertical-align: top;
  }

  .config-table__value {
    padding: 8px 16px;
    vertical-align: top;
  }

  .config-table__field {
    margin-bottom: 0;
  }

  .config-table__field :deep(.el-form-item__error) {
    position: static;
    padding-top: 4px;
  }
}
</style>
