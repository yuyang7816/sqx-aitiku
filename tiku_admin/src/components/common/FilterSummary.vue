<script setup>
import { computed } from 'vue'
import { buildFilterSummaryItems } from '@/utils/filter-form'

const props = defineProps({
  filters: { type: Object, default: () => ({}) },
  fields: { type: Array, default: () => [] },
  emptyText: { type: String, default: '未设置筛选条件' },
})

const activeItems = computed(() => buildFilterSummaryItems(props.filters, props.fields))
</script>

<template>
  <div v-if="fields.length" class="filter-summary">
    <span class="filter-summary__label">当前筛选：</span>
    <template v-if="activeItems.length">
      <el-tag
        v-for="item in activeItems"
        :key="item.key"
        size="small"
        type="info"
        class="filter-summary__tag"
      >
        {{ item.label }}：{{ item.value }}
      </el-tag>
    </template>
    <span v-else class="filter-summary__empty">{{ emptyText }}</span>
  </div>
</template>

<style scoped>
.filter-summary {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
  margin: -4px 0 16px;
  padding: 10px 12px;
  border-radius: 8px;
  background: var(--el-fill-color-light);
  font-size: 13px;
}

.filter-summary__label {
  color: var(--el-text-color-secondary);
  white-space: nowrap;
}

.filter-summary__empty {
  color: var(--el-text-color-placeholder);
}

.filter-summary__tag {
  margin: 0;
}
</style>
