<script setup>
import { computed, defineAsyncComponent, provide, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { refreshSystemConfigApi } from '@/api/modules/system-config'
import {
  SYSTEM_CONFIG_TABS,
  resolveSystemConfigTabKey,
} from '@/constants/system-config-tabs'
import { showSuccess } from '@/utils/modal'
import { isAuth } from '@/utils/tool'

const TAB_COMPONENTS = {
  basic: defineAsyncComponent(() => import('@/views/system/BasicConfigView.vue')),
  auth: defineAsyncComponent(() => import('@/views/system/AuthConfigView.vue')),
  pay: defineAsyncComponent(() => import('@/views/system/PayConfigView.vue')),
  smsChannel: defineAsyncComponent(() => import('@/views/system/SmsChannelConfigView.vue')),
  file: defineAsyncComponent(() => import('@/views/system/FileConfigView.vue')),
  customer: defineAsyncComponent(() => import('@/views/customer/CustomerConfigView.vue')),
  agreement: defineAsyncComponent(() => import('@/views/agreement/AgreementConfigView.vue')),
  sms: defineAsyncComponent(() => import('@/views/business/SmsTemplateView.vue')),
  ocr: defineAsyncComponent(() => import('@/views/ocr/OcrConfigView.vue')),
  ai: defineAsyncComponent(() => import('@/views/ai/AiConfigView.vue')),
  promo: defineAsyncComponent(() => import('@/views/promo/PromoConfigView.vue')),
  withdraw: defineAsyncComponent(() => import('@/views/promo/WithdrawConfigView.vue')),
  dict: defineAsyncComponent(() => import('@/views/system/DictManagerView.vue')),
}

const route = useRoute()
const router = useRouter()
provide('systemConfigEmbedded', true)
const activeTab = ref(resolveSystemConfigTabKey(route.query.tab))

const visibleTabs = computed(() => SYSTEM_CONFIG_TABS.filter((tab) => {
  if (!tab.permission) return true
  return isAuth(tab.permission)
}))

const activeComponent = computed(() => TAB_COMPONENTS[activeTab.value] || TAB_COMPONENTS.basic)

watch(
  () => route.query.tab,
  (tab) => {
    const next = resolveSystemConfigTabKey(tab)
    if (next !== activeTab.value) {
      activeTab.value = next
    }
  },
)

watch(visibleTabs, (tabs) => {
  if (!tabs.some((item) => item.key === activeTab.value)) {
    activeTab.value = tabs[0]?.key || 'basic'
  }
}, { immediate: true })

function handleTabChange(name) {
  activeTab.value = name
  router.replace({ path: '/system/configs', query: { tab: name } })
}

async function handleRefresh() {
  await refreshSystemConfigApi()
  showSuccess('配置缓存已刷新')
}
</script>

<template>
  <el-card shadow="never" class="page-card system-config-page">
    <template #header>
      <div class="card-header">
        <strong>系统配置</strong>
        <el-button v-if="isAuth('sys:config:update')" type="primary" @click="handleRefresh">
          刷新配置缓存
        </el-button>
      </div>
    </template>

    <el-tabs
      v-model="activeTab"
      class="system-config-page__tabs"
      @tab-change="handleTabChange"
    >
      <el-tab-pane
        v-for="tab in visibleTabs"
        :key="tab.key"
        :label="tab.label"
        :name="tab.key"
        lazy
      >
        <div class="system-config-page__panel">
          <component :is="activeComponent" v-if="activeTab === tab.key" />
        </div>
      </el-tab-pane>
    </el-tabs>
  </el-card>
</template>

<style scoped>
.system-config-page__tabs :deep(.el-tabs__header) {
  margin-bottom: 16px;
}

.system-config-page__panel {
  padding-top: 4px;
}

.system-config-page__panel :deep(.page-card) {
  border: none;
  box-shadow: none;
}

.system-config-page__panel :deep(.page-card > .el-card__header) {
  display: none;
}

.system-config-page__panel :deep(.page-card > .el-card__body) {
  padding: 0;
}
</style>
