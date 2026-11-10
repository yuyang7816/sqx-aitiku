<script setup>
import SysConfigSectionView from '@/components/common/SysConfigSectionView.vue'
import {
  AUTH_CONFIG_MIRROR_KEYS,
  AUTH_CONFIG_SECTIONS,
  AUTH_PARAM_TYPE,
  AUTH_PARAM_TYPE_DESC,
} from '@/constants/auth-config'
</script>

<template>
  <SysConfigSectionView
    title="登录认证"
    :sections="AUTH_CONFIG_SECTIONS"
    :param-type="AUTH_PARAM_TYPE"
    :param-type-desc="AUTH_PARAM_TYPE_DESC"
    :mirror-keys="AUTH_CONFIG_MIRROR_KEYS"
    success-message="登录认证配置已保存"
  />
</template>
