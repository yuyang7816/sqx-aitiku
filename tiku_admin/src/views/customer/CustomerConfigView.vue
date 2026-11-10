<script setup>
import SysConfigSectionView from '@/components/common/SysConfigSectionView.vue'
import {
  CUSTOMER_CONFIG_SECTIONS,
  CUSTOMER_PARAM_TYPE,
  CUSTOMER_PARAM_TYPE_DESC,
} from '@/constants/customer-config'
</script>

<template>
  <SysConfigSectionView
    title="客服配置"
    :sections="CUSTOMER_CONFIG_SECTIONS"
    :param-type="CUSTOMER_PARAM_TYPE"
    :param-type-desc="CUSTOMER_PARAM_TYPE_DESC"
    success-message="客服配置已保存"
  />
</template>
