<script setup>
defineOptions({ inheritAttrs: false })
</script>

<template>
  <el-select
    v-bind="$attrs"
    :empty-values="[undefined, null, '']"
    :value-on-clear="() => undefined"
  >
    <slot />
  </el-select>
</template>
