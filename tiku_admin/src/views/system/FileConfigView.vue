<script setup>
import SysConfigSectionView from '@/components/common/SysConfigSectionView.vue'
import {
  FILE_CONFIG_SECTIONS,
  FILE_PARAM_TYPE,
  FILE_PARAM_TYPE_DESC,
} from '@/constants/file-config'
</script>

<template>
  <SysConfigSectionView
    title="文件存储"
    :sections="FILE_CONFIG_SECTIONS"
    :param-type="FILE_PARAM_TYPE"
    :param-type-desc="FILE_PARAM_TYPE_DESC"
    success-message="文件存储配置已保存"
  />
</template>
