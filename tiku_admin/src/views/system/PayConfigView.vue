<script setup>
import SysConfigSectionView from '@/components/common/SysConfigSectionView.vue'
import {
  PAY_CONFIG_SECTIONS,
  PAY_PARAM_TYPE,
  PAY_PARAM_TYPE_DESC,
} from '@/constants/pay-config'
</script>

<template>
  <SysConfigSectionView
    title="支付配置"
    :sections="PAY_CONFIG_SECTIONS"
    :param-type="PAY_PARAM_TYPE"
    :param-type-desc="PAY_PARAM_TYPE_DESC"
    success-message="支付配置已保存"
  />
</template>
