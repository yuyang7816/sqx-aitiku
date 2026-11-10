<script setup>
import SysConfigSectionView from '@/components/common/SysConfigSectionView.vue'
import {
  PROMO_CONFIG_SECTIONS,
  PROMO_PARAM_TYPE,
  PROMO_PARAM_TYPE_DESC,
} from '@/constants/promo-config'
</script>

<template>
  <SysConfigSectionView
    title="推广佣金"
    :sections="PROMO_CONFIG_SECTIONS"
    :param-type="PROMO_PARAM_TYPE"
    :param-type-desc="PROMO_PARAM_TYPE_DESC"
    success-message="推广配置已保存"
  />
</template>
