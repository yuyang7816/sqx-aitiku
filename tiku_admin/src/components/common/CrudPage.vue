<script setup>
import { ref } from 'vue'
import AppDialog from '@/components/common/AppDialog.vue'
import BasePagination from '@/components/common/BasePagination.vue'
import RecordDetailDialog from '@/components/common/RecordDetailDialog.vue'
import TableRowActions from '@/components/common/TableRowActions.vue'
import UserLink from '@/components/business/UserLink.vue'
import OrderLink from '@/components/business/OrderLink.vue'
import WithdrawAccountNumberCell from '@/components/business/WithdrawAccountNumberCell.vue'
import { useCrudPage } from '@/composables/useCrudPage'
import { useFilterFormKey } from '@/composables/useFilterFormKey'
import { useSystemConfigEmbedded } from '@/composables/useSystemConfigEmbedded'
import FilterSelect from '@/components/common/FilterSelect.vue'
import FilterSummary from '@/components/common/FilterSummary.vue'
import { resolveUserDisplayName, resolveUserPhone } from '@/utils/user-display'
import { validateForm } from '@/utils/form'

const props = defineProps({
  title: { type: String, required: true },
  api: { type: Object, required: true },
  columns: { type: Array, required: true },
  idKey: { type: String, default: 'id' },
  createDefaults: { type: Object, default: () => ({}) },
  showCreate: { type: Boolean, default: true },
  showEdit: { type: Boolean, default: true },
  showDelete: { type: Boolean, default: true },
  detailFields: { type: Array, default: () => [] },
  filterFields: { type: Array, default: () => [] },
  defaultQuery: { type: Object, default: () => ({}) },
})

const formRef = ref()
const embedded = useSystemConfigEmbedded()
const { filterFormKey, bumpFilterFormKey } = useFilterFormKey()

const {
  loading,
  dialogVisible,
  dialogLoading,
  detailVisible,
  detailLoading,
  detailData,
  tableData,
  total,
  pageNum,
  pageSize,
  editingId,
  form,
  filters,
  loadData,
  handleSearch,
  handleResetFilters,
  openCreate,
  openDetail,
  openEdit,
  submitForm,
  removeRow,
} = useCrudPage(props.api, {
  idKey: props.idKey,
  detailFields: props.detailFields,
  filterFields: props.filterFields,
  defaultQuery: props.defaultQuery,
})

function handleCreate() {
  openCreate({ ...props.createDefaults })
}

async function handleSubmit() {
  const valid = await validateForm(formRef)
  if (!valid) return
  await submitForm()
}

function onResetFilters() {
  handleResetFilters()
  bumpFilterFormKey()
}

function renderCell(column, row) {
  const raw = row?.[column.prop]
  if (typeof column.formatter === 'function') {
    return column.formatter(raw, row)
  }
  if (raw === null || raw === undefined || raw === '') {
    return '—'
  }
  return raw
}

defineExpose({ loadData })
</script>

<template>
  <el-card shadow="never" class="page-card">
    <template #header>
      <div class="card-header">
        <strong v-if="!embedded">{{ title }}</strong>
        <el-button v-if="showCreate" type="primary" @click="handleCreate">新增</el-button>
      </div>
    </template>

    <el-form
      v-if="filterFields.length"
      :key="filterFormKey"
      :inline="true"
      class="search-grid"
      @submit.prevent
    >
      <el-form-item v-for="field in filterFields" :key="field.key" :label="field.label">
        <el-input
          v-if="field.type === 'text' || !field.type"
          v-model="filters[field.key]"
          clearable
          :placeholder="field.placeholder || `请输入${field.label}`"
          @keyup.enter="handleSearch"
        />
        <FilterSelect
          v-else-if="field.type === 'select'"
          v-model="filters[field.key]"
          clearable
          :placeholder="field.placeholder || `请选择${field.label}`"
          style="min-width: 160px"
        >
          <el-option
            v-for="option in field.options || []"
            :key="`${field.key}-${option.value}`"
            :label="option.label"
            :value="option.value"
          />
        </FilterSelect>
      </el-form-item>
      <el-form-item>
        <el-space>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="onResetFilters">重置</el-button>
        </el-space>
      </el-form-item>
    </el-form>

    <FilterSummary :filters="filters" :fields="filterFields" />

    <slot name="toolbar" :load-data="loadData" :filters="filters" />

    <el-table v-loading="loading" :data="tableData" border>
      <template v-for="column in columns" :key="column.prop || column.label">
        <el-table-column v-if="column.prop === 'userId'" v-bind="column" :label="column.label || '用户'">
          <template #default="{ row }">
            <UserLink
              :user-id="row.userId"
              :label="resolveUserDisplayName(row)"
              :phone="resolveUserPhone(row)"
            />
          </template>
        </el-table-column>
        <el-table-column v-else-if="column.prop === 'orderId'" v-bind="column">
          <template #default="{ row }">
            <OrderLink :order-id="row.orderId" :label="String(row.orderId)" />
          </template>
        </el-table-column>
        <el-table-column v-else-if="column.type === 'withdrawAccountNumber'" v-bind="{ ...column, type: undefined, formatter: undefined }">
          <template #default="{ row }">
            <WithdrawAccountNumberCell :row="row" :field-key="column.prop || 'accountNumber'" compact />
          </template>
        </el-table-column>
        <el-table-column v-else-if="column.formatter" v-bind="{ ...column, formatter: undefined }">
          <template #default="{ row }">
            {{ renderCell(column, row) }}
          </template>
        </el-table-column>
        <el-table-column v-else v-bind="column" />
      </template>
      <el-table-column label="操作" min-width="210" fixed="right">
        <template #default="{ row }">
          <TableRowActions>
            <slot
              name="actions"
              :row="row"
              :open-edit="() => openEdit(row)"
              :open-detail="() => openDetail(row)"
              :remove-row="() => removeRow(row)"
            />
            <el-button link type="primary" @click="openDetail(row)">查看详情</el-button>
            <el-button v-if="showEdit" link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button v-if="showDelete" link type="danger" @click="removeRow(row)">删除</el-button>
          </TableRowActions>
        </template>
      </el-table-column>
    </el-table>

    <BasePagination
      :current-page="pageNum"
      :page-size="pageSize"
      :total="total"
      @update:current-page="pageNum = $event; loadData()"
      @update:page-size="pageSize = $event; pageNum = 1; loadData()"
    />

    <AppDialog v-model="dialogVisible" :title="editingId ? `编辑${title}` : `新增${title}`" :loading="dialogLoading" @confirm="handleSubmit">
      <el-form ref="formRef" :model="form" label-width="110px">
        <slot name="form" :form="form" :editing-id="editingId" />
      </el-form>
    </AppDialog>

    <RecordDetailDialog
      v-model="detailVisible"
      :title="`${title}详情`"
      :loading="detailLoading"
      :data="detailData"
      :fields="detailFields"
    />
  </el-card>
</template>
