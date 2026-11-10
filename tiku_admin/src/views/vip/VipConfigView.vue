<script setup>
import { onMounted, reactive, ref } from 'vue'
import { getVipConfigApi, saveVipConfigApi } from '@/api/modules/vip-config'
import { VIP_CONFIG_DEFS } from '@/constants/vip-config'
import { validateForm } from '@/utils/form'
import { showSuccess } from '@/utils/modal'
import { isAuth } from '@/utils/tool'
import { useSystemConfigEmbedded } from '@/composables/useSystemConfigEmbedded'

const embedded = useSystemConfigEmbedded()
const loading = ref(false)
const saving = ref(false)
const formRef = ref()
const configIds = reactive({})

const form = reactive({
  renewNoticeDays: Number(VIP_CONFIG_DEFS[0].defaultValue),
})

const rules = {
  renewNoticeDays: [
    { required: true, message: '请设置续费提醒提前天数', trigger: 'change' },
    {
      validator: (_rule, value, callback) => {
        const num = Number(value)
        if (Number.isNaN(num) || num < 1 || num > 30) {
          callback(new Error('提前天数范围为 1~30'))
          return
        }
        callback()
      },
      trigger: 'change',
    },
  ],
}

function applyData({ records }) {
  records.forEach((row) => {
    configIds[row.paramKey] = row.id
    if (row.paramKey === 'vip_renew_notice_days') {
      form.renewNoticeDays = Number(row.paramValue) || Number(VIP_CONFIG_DEFS[0].defaultValue)
    }
  })
}

function buildSaveRecords() {
  return VIP_CONFIG_DEFS.map((def) => ({
    id: configIds[def.key] || null,
    paramKey: def.key,
    paramValue: String(form.renewNoticeDays),
    status: '1',
    remark: def.remark,
  }))
}

async function loadData() {
  loading.value = true
  try {
    const data = await getVipConfigApi()
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
    const refreshed = await saveVipConfigApi(buildSaveRecords())
    applyData(refreshed)
    showSuccess('会员配置已保存，到期前将按设定天数向用户推送续费提醒')
  } finally {
    saving.value = false
  }
}

onMounted(loadData)
</script>

<template>
  <div v-loading="loading" class="vip-config-page">
    <el-row :gutter="20">
      <el-col :xs="24" :lg="14">
        <el-card shadow="never" class="page-card">
          <template #header>
            <div class="card-header">
              <strong v-if="!embedded">会员续费提醒配置</strong>
              <el-button
                v-if="isAuth('sys:config:update') || isAuth('sys:config:save')"
                type="primary"
                :loading="saving"
                @click="handleSave"
              >
                保存配置
              </el-button>
            </div>
          </template>

          <el-alert
            type="info"
            :closable="false"
            show-icon
            title="配置说明"
            description="设置会员到期前多少天向用户推送续费提醒。套餐与权益请在左侧「会员套餐」「会员权益」标签页维护。"
            class="tip-alert"
          />

          <el-form ref="formRef" :model="form" :rules="rules" label-width="160px" class="config-form">
            <el-form-item label="续费提醒提前天数" prop="renewNoticeDays">
              <el-input-number
                v-model="form.renewNoticeDays"
                :min="1"
                :max="30"
                :step="1"
                controls-position="right"
              />
              <p class="field-hint">会员到期前 N 天发送站内续费提醒，建议 3~7 天</p>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="10">
        <el-card shadow="never" class="page-card preview-card">
          <template #header><strong>配置效果预览</strong></template>
          <p class="preview-tip">预览仅展示提醒规则，实际推送以系统定时任务为准</p>
          <div class="renew-preview">
            <div class="renew-preview-title">续费提醒</div>
            <p class="renew-preview-desc">
              会员到期前 <strong>{{ form.renewNoticeDays }}</strong> 天，将向用户发送站内续费提醒消息。
            </p>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
.page-card { margin-bottom: 16px; }
.card-header { display: flex; align-items: center; justify-content: space-between; gap: 12px; }
.tip-alert { margin-bottom: 20px; }
.config-form { max-width: 560px; }
.field-hint { margin: 6px 0 0; font-size: 12px; color: var(--el-text-color-secondary); }
.preview-tip { margin: 0 0 12px; font-size: 13px; color: var(--el-text-color-secondary); }
.renew-preview {
  padding: 20px;
  border-radius: 12px;
  background: linear-gradient(160deg, #f59e0b 0%, #d97706 100%);
  color: #fff;
  min-height: 160px;
}
.renew-preview-title { font-size: 18px; font-weight: 700; margin-bottom: 12px; }
.renew-preview-desc { margin: 0; line-height: 1.8; font-size: 14px; }
.renew-preview-desc strong { font-size: 24px; }
</style>
