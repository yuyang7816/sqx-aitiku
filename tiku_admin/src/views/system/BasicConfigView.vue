<script setup>
import SysConfigSectionView from '@/components/common/SysConfigSectionView.vue'
import {
  BASIC_CONFIG_SECTIONS,
  BASIC_PARAM_TYPE,
  BASIC_PARAM_TYPE_DESC,
} from '@/constants/basic-config'
</script>

<template>
  <SysConfigSectionView
    title="基础配置"
    :sections="BASIC_CONFIG_SECTIONS"
    :param-type="BASIC_PARAM_TYPE"
    :param-type-desc="BASIC_PARAM_TYPE_DESC"
    success-message="基础配置已保存"
  />
</template>
