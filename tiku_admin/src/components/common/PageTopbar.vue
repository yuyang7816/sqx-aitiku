<script setup>
import IconSvg from '@/components/icon-svg.vue'

defineProps({
  title: {
    type: String,
    default: '',
  },
})

const emit = defineEmits(['back'])

function handleBack(event) {
  event.preventDefault()
  emit('back')
}
</script>

<template>
  <div class="page-topbar">
    <div class="page-title">
      <a href="#" class="back-link" @click="handleBack">
        <icon-svg name="jiantou" class="back-icon" />
        返回
      </a>
      <span class="divider">|</span>
      <span>{{ title }}</span>
    </div>
    <el-space v-if="$slots.actions">
      <slot name="actions" />
    </el-space>
  </div>
</template>

<style scoped>
.page-topbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

.page-title {
  display: flex;
  align-items: center;
  gap: 14px;
  font-size: 18px;
}

.back-link {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: var(--el-text-color-regular);
  text-decoration: none;
  font-size: 14px;
}

.back-link:hover {
  color: var(--el-color-primary);
}

.back-icon {
  width: 1.2em;
  height: 1.2em;
}

.divider {
  color: #d9d9d9;
}
</style>
