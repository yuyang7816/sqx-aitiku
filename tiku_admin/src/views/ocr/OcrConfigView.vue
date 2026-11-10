<script setup>
import SysConfigSectionView from '@/components/common/SysConfigSectionView.vue'
import {
  OCR_CONFIG_SECTIONS,
  OCR_PARAM_TYPE,
  OCR_PARAM_TYPE_DESC,
} from '@/constants/ocr-config'
</script>

<template>
  <SysConfigSectionView
    title="OCR 配置"
    :sections="OCR_CONFIG_SECTIONS"
    :param-type="OCR_PARAM_TYPE"
    :param-type-desc="OCR_PARAM_TYPE_DESC"
    success-message="OCR 配置已保存"
  />
</template>
