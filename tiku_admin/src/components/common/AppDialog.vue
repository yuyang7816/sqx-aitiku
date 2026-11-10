<template>
  <el-dialog :model-value="modelValue" :title="title" :width="width" destroy-on-close @close="$emit('update:modelValue', false)">
    <slot />
    <template #footer>
      <el-space v-if="showFooter">
        <el-button @click="$emit('update:modelValue', false)">取消</el-button>
        <el-button type="primary" :loading="loading" @click="$emit('confirm')">确定</el-button>
      </el-space>
    </template>
  </el-dialog>
</template>

<script setup>
defineProps({
  modelValue: { type: Boolean, default: false },
  title: { type: String, default: '' },
  width: { type: String, default: '620px' },
  loading: { type: Boolean, default: false },
  showFooter: { type: Boolean, default: true },
})

defineEmits(['update:modelValue', 'confirm'])
</script>
