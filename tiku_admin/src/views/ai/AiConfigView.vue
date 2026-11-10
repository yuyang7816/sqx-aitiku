<script setup>
import SysConfigSectionView from '@/components/common/SysConfigSectionView.vue'
import {
  AI_CONFIG_SECTIONS,
  AI_PARAM_TYPE,
  AI_PARAM_TYPE_DESC,
} from '@/constants/ai-config'
</script>

<template>
  <SysConfigSectionView
    title="AI 配置"
    :sections="AI_CONFIG_SECTIONS"
    :param-type="AI_PARAM_TYPE"
    :param-type-desc="AI_PARAM_TYPE_DESC"
    success-message="AI 配置已保存"
  />
</template>
