<script setup>
import SysConfigSectionView from '@/components/common/SysConfigSectionView.vue'
import {
  SMS_CHANNEL_CONFIG_SECTIONS,
  SMS_CHANNEL_PARAM_TYPE,
  SMS_CHANNEL_PARAM_TYPE_DESC,
} from '@/constants/sms-channel-config'
</script>

<template>
  <SysConfigSectionView
    title="短信通道"
    :sections="SMS_CHANNEL_CONFIG_SECTIONS"
    :param-type="SMS_CHANNEL_PARAM_TYPE"
    :param-type-desc="SMS_CHANNEL_PARAM_TYPE_DESC"
    success-message="短信通道配置已保存"
  />
</template>
