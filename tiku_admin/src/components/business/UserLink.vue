<script setup>
import { computed } from 'vue'
import { useAppUserDetail } from '@/composables/useAppUserDetail'

const props = defineProps({
  userId: { type: [Number, String], default: null },
  label: { type: String, default: '' },
  phone: { type: String, default: '' },
})

const { openUserDetail } = useAppUserDetail()

const displayText = computed(() => {
  if (props.label) return props.label
  if (props.phone) return props.phone
  if (props.userId) return `用户${props.userId}`
  return '—'
})

function handleClick() {
  openUserDetail(Number(props.userId))
}
</script>

<template>
  <el-button v-if="userId" link type="primary" @click.stop="handleClick">
    {{ displayText }}
  </el-button>
  <span v-else>{{ displayText }}</span>
</template>
