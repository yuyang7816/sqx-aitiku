<script setup>
import { computed } from 'vue'
import { useVipOrderDetail } from '@/composables/useVipOrderDetail'

const props = defineProps({
  orderId: { type: [Number, String], default: null },
  label: { type: String, default: '' },
})

const { openOrderDetail } = useVipOrderDetail()

const displayText = computed(() => {
  if (props.label) return props.label
  if (props.orderId) return `订单${props.orderId}`
  return '—'
})

function handleClick() {
  openOrderDetail(Number(props.orderId))
}
</script>

<template>
  <el-button v-if="orderId" link type="primary" @click.stop="handleClick">
    {{ displayText }}
  </el-button>
  <span v-else>{{ displayText }}</span>
</template>
