<script setup>
import { computed } from 'vue'
import { resolveImageUrl } from '@/utils/image-field'
import {
  formatWithdrawalAccountNumberText,
  getWithdrawalAccountNumber,
  shouldShowWechatQrImage,
} from '@/utils/withdrawal-account'

const props = defineProps({
  row: { type: Object, default: () => ({}) },
  fieldKey: { type: String, default: 'accountNumber' },
  compact: { type: Boolean, default: false },
})

const showQr = computed(() => shouldShowWechatQrImage(props.row, props.fieldKey))
const imageUrl = computed(() => resolveImageUrl(getWithdrawalAccountNumber(props.row, props.fieldKey)))
const previewList = computed(() => (imageUrl.value ? [imageUrl.value] : []))
const displayText = computed(() => formatWithdrawalAccountNumberText(props.row, props.fieldKey))
const imageStyle = computed(() => (
  props.compact
    ? { width: '56px', height: '56px' }
    : { width: '120px', height: '120px' }
))
</script>

<template>
  <div v-if="showQr" class="withdraw-qr-cell">
    <el-image
      :src="imageUrl"
      fit="contain"
      :preview-src-list="previewList"
      :style="imageStyle"
      preview-teleported
    />
  </div>
  <span v-else>{{ displayText }}</span>
</template>

<style scoped>
.withdraw-qr-cell {
  display: inline-flex;
  align-items: center;
  padding: 2px 0;
}
</style>
