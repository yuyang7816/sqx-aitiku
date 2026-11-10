<script setup>
import { computed } from 'vue'

const props = defineProps({
  name: { type: String, default: '' },
})

const modules = import.meta.glob('/src/icons/svg/*.svg', { eager: true, query: '?raw', import: 'default' })

function normalizeIconName(name) {
  return String(name || '').trim().replace(/^icon-/, '')
}

function normalizeSvgMarkup(markup) {
  if (!markup) {
    return ''
  }

  return markup
    .replace(/<\?xml[\s\S]*?\?>/gi, '')
    .replace(/<!DOCTYPE[\s\S]*?>/gi, '')
    .replace(/<svg([^>]*)>/i, '<svg$1 class="icon-svg__svg" aria-hidden="true" focusable="false">')
}

const iconMarkup = computed(() => {
  const iconName = normalizeIconName(props.name)
  if (!iconName) {
    return ''
  }
  return normalizeSvgMarkup(modules[`/src/icons/svg/icon-${iconName}.svg`] || '')
})
</script>

<template>
  <span v-if="iconMarkup" class="icon-svg" v-html="iconMarkup" />
  <span v-else class="icon-svg icon-svg--empty" />
</template>
