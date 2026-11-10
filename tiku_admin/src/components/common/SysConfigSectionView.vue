<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import ImageUpload from '@/components/common/ImageUpload.vue'
import FileUrlInput from '@/components/common/FileUrlInput.vue'
import {
  buildFieldRules,
  formToRecords,
  isDefVisible,
  loadSysConfigByDefs,
  recordsToForm,
  saveSysConfigRecords,
  syncMirrorConfigKeys,
} from '@/utils/sys-config-form'
import { validateForm } from '@/utils/form'
import { showError, showSuccess } from '@/utils/modal'
import { isAuth } from '@/utils/tool'
import { useSystemConfigEmbedded } from '@/composables/useSystemConfigEmbedded'

const props = defineProps({
  title: { type: String, default: '' },
  sections: { type: Array, required: true },
  paramType: { type: String, required: true },
  paramTypeDesc: { type: String, required: true },
  successMessage: { type: String, default: '配置已保存' },
  mirrorKeys: { type: Object, default: () => ({}) },
})

const embedded = useSystemConfigEmbedded()
const loading = ref(false)
const saving = ref(false)
const formRef = ref()
const records = ref([])
const form = reactive({})

const allDefs = computed(() => props.sections.flatMap((section) => section.items.map((item) => ({
  ...item,
  paramType: props.paramType,
  paramTypeDesc: props.paramTypeDesc,
  sectionId: section.id,
  sectionTitle: section.title,
}))))

const rules = computed(() => buildFieldRules(allDefs.value, form))
const canSave = computed(() => isAuth('sys:config:update') || isAuth('sys:config:save'))

function resolveInputType(def) {
  if (def.inputType) return def.inputType
  if (def.type === 'password') return 'password'
  if (def.type === 'textarea') return 'textarea'
  if (def.type === 'file') return 'file'
  return 'text'
}

function resolveControlWidth(def) {
  if (def.type === 'textarea' || def.type === 'image' || def.type === 'file') return '100%'
  if (def.type === 'switch') return 'auto'
  return '360px'
}

async function loadData() {
  loading.value = true
  try {
    records.value = await loadSysConfigByDefs(allDefs.value, props.paramType, props.paramTypeDesc)
    Object.assign(form, recordsToForm(records.value, allDefs.value))
  } catch (error) {
    showError(error?.msg || error?.message || '加载配置失败')
  } finally {
    loading.value = false
  }
}

async function handleSave() {
  const valid = await validateForm(formRef)
  if (!valid) return
  saving.value = true
  try {
    const payload = formToRecords(form, records.value, allDefs.value)
    await saveSysConfigRecords(payload, props.paramType, props.paramTypeDesc)
    if (Object.keys(props.mirrorKeys).length) {
      await syncMirrorConfigKeys(form, props.mirrorKeys, props.paramType, props.paramTypeDesc)
    }
    records.value = await loadSysConfigByDefs(allDefs.value, props.paramType, props.paramTypeDesc)
    Object.assign(form, recordsToForm(records.value, allDefs.value))
    showSuccess(props.successMessage)
  } catch (error) {
    showError(error?.msg || error?.message || '保存失败')
  } finally {
    saving.value = false
  }
}

onMounted(loadData)
</script>

<template>
  <div v-loading="loading" class="sys-config-section-page">
    <div v-if="canSave" class="config-toolbar">
      <strong v-if="!embedded && title" class="config-toolbar__title">{{ title }}</strong>
      <el-button type="primary" :loading="saving" @click="handleSave">保存配置</el-button>
    </div>

    <el-form ref="formRef" :model="form" :rules="rules" class="config-list-form">
      <section
        v-for="section in sections"
        :key="section.id"
        class="config-section"
      >
        <div class="config-section__title">{{ section.title }}</div>
        <table class="config-table">
          <colgroup>
            <col class="config-table__label-col" />
            <col class="config-table__value-col" />
          </colgroup>
          <tbody>
            <template v-for="def in section.items" :key="def.key">
              <tr v-if="isDefVisible(def, form)" class="config-table__row">
                <td class="config-table__label">{{ def.label }}</td>
                <td class="config-table__value">
                  <el-form-item :prop="def.key" label-width="0" class="config-table__field">
                    <el-select
                      v-if="def.type === 'select'"
                      v-model="form[def.key]"
                      :placeholder="def.placeholder || `请选择${def.label}`"
                      :style="{ width: resolveControlWidth(def) }"
                    >
                      <el-option
                        v-for="option in def.options || []"
                        :key="option.value"
                        :label="option.label"
                        :value="option.value"
                      />
                    </el-select>

                    <div v-else-if="def.type === 'number'" class="config-number-row">
                      <el-input-number
                        v-model="form[def.key]"
                        :min="def.min"
                        :max="def.max"
                        :step="def.step ?? 1"
                        :precision="def.precision"
                        controls-position="right"
                      />
                      <span v-if="def.unit" class="config-unit">{{ def.unit }}</span>
                    </div>

                    <el-switch
                      v-else-if="def.type === 'switch'"
                      v-model="form[def.key]"
                    />

                    <ImageUpload
                      v-else-if="def.type === 'image'"
                      v-model="form[def.key]"
                      :limit="1"
                    />

                    <FileUrlInput
                      v-else-if="def.type === 'file'"
                      v-model="form[def.key]"
                      :accept="def.accept || 'video/*,.mp4,.mov,.m4v'"
                      :placeholder="def.placeholder || `请填写${def.label}`"
                    />

                    <el-input
                      v-else
                      v-model="form[def.key]"
                      :type="resolveInputType(def)"
                      :rows="def.rows || 4"
                      :maxlength="def.maxLength"
                      :show-word-limit="Boolean(def.maxLength)"
                      :show-password="def.type === 'password'"
                      :placeholder="def.placeholder || `请填写${def.label}`"
                      :style="{ width: resolveControlWidth(def) }"
                      clearable
                    />
                    <p v-if="def.remark" class="config-field-hint">{{ def.remark }}</p>
                  </el-form-item>
                </td>
              </tr>
            </template>
          </tbody>
        </table>
      </section>
    </el-form>
  </div>
</template>

<style scoped lang="scss">
.sys-config-section-page {
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

  .config-list-form {
    display: flex;
    flex-direction: column;
    gap: 20px;
  }

  .config-section__title {
    margin-bottom: 8px;
    font-size: 14px;
    font-weight: 600;
    color: var(--el-text-color-primary);
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

  .config-number-row {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .config-unit {
    color: var(--el-text-color-secondary);
    font-size: 13px;
  }

  .config-field-hint {
    margin: 6px 0 0;
    font-size: 12px;
    line-height: 1.5;
    color: var(--el-text-color-secondary);
  }
}
</style>
