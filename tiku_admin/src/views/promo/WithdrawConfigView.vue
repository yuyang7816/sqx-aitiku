<script setup>
import SysConfigSectionView from '@/components/common/SysConfigSectionView.vue'
import {
  WITHDRAW_CONFIG_SECTIONS,
  WITHDRAW_PARAM_TYPE,
  WITHDRAW_PARAM_TYPE_DESC,
} from '@/constants/withdraw-config'
</script>

<template>
  <SysConfigSectionView
    title="提现配置"
    :sections="WITHDRAW_CONFIG_SECTIONS"
    :param-type="WITHDRAW_PARAM_TYPE"
    :param-type-desc="WITHDRAW_PARAM_TYPE_DESC"
    success-message="提现配置已保存"
  />
</template>
