<script setup>
const props = defineProps({
  activeValue: { type: [Number, String], default: 1 },
  inactiveValue: { type: [Number, String], default: 2 },
  modelValue: { type: [Number, String], required: true },
  activeText: { type: String, default: '上架' },
  inactiveText: { type: String, default: '下架' },
  loading: { type: Boolean, default: false },
  disabled: { type: Boolean, default: false },
})

const emit = defineEmits(['update:modelValue', 'change'])

function handleChange(checked) {
  const nextValue = checked ? props.activeValue : props.inactiveValue
  emit('update:modelValue', nextValue)
  emit('change', nextValue)
}
</script>

<template>
  <el-switch
    :model-value="modelValue === activeValue"
    inline-prompt
    :active-text="activeText"
    :inactive-text="inactiveText"
    :loading="loading"
    :disabled="disabled"
    @change="handleChange"
  />
</template>
