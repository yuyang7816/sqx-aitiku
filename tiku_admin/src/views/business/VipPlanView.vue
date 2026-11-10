<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import AppDialog from '@/components/common/AppDialog.vue'
import BasePagination from '@/components/common/BasePagination.vue'
import TableRowActions from '@/components/common/TableRowActions.vue'
import TableStateSwitch from '@/components/common/TableStateSwitch.vue'
import { vipPlanApi } from '@/api/modules/vip-plan'
import {
  VIP_PLAN_MONTH_PRESETS,
  VIP_PLAN_STATUS_OPTIONS,
  resolveVipPlanDays,
} from '@/constants/vip-plan'
import FilterSelect from '@/components/common/FilterSelect.vue'
import FilterSummary from '@/components/common/FilterSummary.vue'
import { useFilterFormKey } from '@/composables/useFilterFormKey'
import { FILTER_SELECT_EMPTY, normalizeNumberFilterQuery } from '@/utils/filter-form'
import { validateForm } from '@/utils/form'
import { confirmAction, showError, showSuccess } from '@/utils/modal'
import { useSystemConfigEmbedded } from '@/composables/useSystemConfigEmbedded'

const embedded = useSystemConfigEmbedded()

const loading = ref(false)
const dialogVisible = ref(false)
const dialogLoading = ref(false)
const formRef = ref()
const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const editingId = ref(null)
const stateUpdatingId = ref(null)
const { filterFormKey, bumpFilterFormKey } = useFilterFormKey()

const FILTER_FIELDS = [
  { key: 'name', label: '套餐名称', type: 'text', placeholder: '如：月卡、季卡' },
  { key: 'status', label: '上下架', type: 'select', options: VIP_PLAN_STATUS_OPTIONS, placeholder: '全部状态' },
]

const filters = reactive({
  name: '',
  status: FILTER_SELECT_EMPTY,
})

const form = reactive({
  name: '',
  month: 1,
  amount: 0,
  originalAmount: 0,
  sort: 1,
  status: 1,
})

const formDaysPreview = computed(() => resolveVipPlanDays(form.month))

const rules = {
  name: [{ required: true, message: '请输入套餐名称', trigger: 'blur' }],
  month: [{ required: true, message: '请输入有效月数', trigger: 'change' }],
  amount: [{ required: true, message: '请输入售价', trigger: 'change' }],
  sort: [{ required: true, message: '请输入排序', trigger: 'change' }],
  status: [{ required: true, message: '请选择上下架状态', trigger: 'change' }],
}

function buildQueryParams() {
  return {
    page: pageNum.value,
    limit: pageSize.value,
    name: filters.name || undefined,
    status: normalizeNumberFilterQuery(filters.status),
  }
}

async function loadData() {
  loading.value = true
  try {
    const data = await vipPlanApi.page(buildQueryParams())
    tableData.value = data.list
    total.value = data.total
  } catch (error) {
    showError(error?.msg || error?.message || '加载列表失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pageNum.value = 1
  loadData()
}

function handleReset() {
  Object.assign(filters, { name: '', status: FILTER_SELECT_EMPTY })
  bumpFilterFormKey()
  pageNum.value = 1
  loadData()
}

function resetForm(initial = {}) {
  Object.assign(form, {
    name: '',
    month: 1,
    amount: 0,
    originalAmount: 0,
    sort: 1,
    status: 1,
    ...initial,
  })
}

function openCreate() {
  editingId.value = null
  resetForm()
  dialogVisible.value = true
}

async function openEdit(row) {
  editingId.value = row.id
  dialogLoading.value = true
  dialogVisible.value = true
  try {
    const data = await vipPlanApi.info(row.id)
    resetForm(data || row)
  } catch (error) {
    showError(error?.msg || error?.message || '加载详情失败')
    dialogVisible.value = false
  } finally {
    dialogLoading.value = false
  }
}

async function submitForm() {
  const valid = await validateForm(formRef)
  if (!valid) return
  dialogLoading.value = true
  try {
    const payload = {
      name: form.name,
      month: form.month,
      amount: form.amount,
      originalAmount: form.originalAmount,
      sort: form.sort,
      status: form.status,
    }
    if (editingId.value) {
      await vipPlanApi.update({ ...payload, id: editingId.value })
      showSuccess('更新成功')
    } else {
      await vipPlanApi.save(payload)
      showSuccess('创建成功')
    }
    dialogVisible.value = false
    await loadData()
  } catch (error) {
    showError(error?.msg || error?.message || '保存失败')
  } finally {
    dialogLoading.value = false
  }
}

async function removeRow(row) {
  await confirmAction(`确认删除套餐「${row.name}」吗？`)
  try {
    await vipPlanApi.delete({ id: row.id })
    showSuccess('删除成功')
    await loadData()
  } catch (error) {
    showError(error?.msg || error?.message || '删除失败')
  }
}

async function handleStateChange(row, nextStatus) {
  const previousStatus = row.status
  if (nextStatus === previousStatus) return
  const actionLabel = nextStatus === 1 ? '上架' : '下架'
  stateUpdatingId.value = row.id
  try {
    await vipPlanApi.update({ id: row.id, status: nextStatus })
    row.status = nextStatus
    showSuccess(`已${actionLabel}`)
  } catch (error) {
    row.status = previousStatus
    showError(error?.msg || error?.message || `${actionLabel}失败`)
  } finally {
    stateUpdatingId.value = null
  }
}

function applyMonthPreset(value) {
  form.month = value
}

onMounted(() => loadData())
</script>

<template>
  <el-card shadow="never" class="page-card">
    <template #header>
      <div class="card-header">
        <strong v-if="!embedded">VIP 套餐管理</strong>
        <el-button type="primary" @click="openCreate">新增套餐</el-button>
      </div>
    </template>

    <p class="page-desc">
      维护会员开通套餐的名称、售价、有效天数与上下架。仅「已上架」的套餐会展示在 App 会员中心。
    </p>

    <el-form :key="filterFormKey" :inline="true" class="filter-form" @submit.prevent="handleSearch">
      <el-form-item label="套餐名称">
        <el-input v-model="filters.name" clearable placeholder="如：月卡、季卡" @keyup.enter="handleSearch" />
      </el-form-item>
      <el-form-item label="上下架">
        <FilterSelect v-model="filters.status" clearable placeholder="全部状态">
          <el-option
            v-for="item in VIP_PLAN_STATUS_OPTIONS"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </FilterSelect>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
      </el-form-item>
    </el-form>
    <FilterSummary :filters="filters" :fields="FILTER_FIELDS" />

    <el-table v-loading="loading" :data="tableData" border>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="套餐名称" min-width="120" show-overflow-tooltip />
      <el-table-column label="售价（元）" width="110">
        <template #default="{ row }">{{ row.amount ?? '-' }}</template>
      </el-table-column>
      <el-table-column label="原价（元）" width="110">
        <template #default="{ row }">{{ row.originalAmount ?? '-' }}</template>
      </el-table-column>
      <el-table-column label="有效月数" width="100">
        <template #default="{ row }">{{ row.month ?? '-' }}</template>
      </el-table-column>
      <el-table-column label="有效天数" width="100">
        <template #default="{ row }">{{ resolveVipPlanDays(row.month) || '-' }}</template>
      </el-table-column>
      <el-table-column label="上下架" width="110">
        <template #default="{ row }">
          <TableStateSwitch
            :model-value="row.status"
            :active-value="1"
            :inactive-value="0"
            :loading="stateUpdatingId === row.id"
            @change="(nextStatus) => handleStateChange(row, nextStatus)"
          />
        </template>
      </el-table-column>
      <el-table-column prop="sort" label="排序" width="80" />
      <el-table-column prop="createTime" label="创建时间" width="170" />
      <el-table-column label="操作" min-width="160" fixed="right">
        <template #default="{ row }">
          <TableRowActions>
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="removeRow(row)">删除</el-button>
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

    <AppDialog
      v-model="dialogVisible"
      :title="editingId ? '编辑 VIP 套餐' : '新增 VIP 套餐'"
      :loading="dialogLoading"
      @confirm="submitForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="套餐名称" prop="name">
          <el-input v-model="form.name" maxlength="32" show-word-limit placeholder="如：月卡、季卡" />
        </el-form-item>
        <el-form-item label="有效月数" prop="month">
          <el-input-number v-model="form.month" :min="1" :max="120" />
          <div class="preset-row">
            <el-button
              v-for="item in VIP_PLAN_MONTH_PRESETS"
              :key="item.value"
              size="small"
              @click="applyMonthPreset(item.value)"
            >
              {{ item.label }}
            </el-button>
          </div>
          <p class="form-hint">C 端有效天数：{{ formDaysPreview }} 天（月卡 31 天，其余月数 ×30）</p>
        </el-form-item>
        <el-form-item label="售价（元）" prop="amount">
          <el-input-number v-model="form.amount" :min="0" :precision="2" :step="1" />
        </el-form-item>
        <el-form-item label="原价（元）">
          <el-input-number v-model="form.originalAmount" :min="0" :precision="2" :step="1" />
          <p class="form-hint">划线价，可选</p>
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="form.sort" :min="0" :max="9999" />
          <span class="form-hint">数值越小越靠前</span>
        </el-form-item>
        <el-form-item label="上下架" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio v-for="item in VIP_PLAN_STATUS_OPTIONS" :key="item.value" :value="item.value">
              {{ item.label }}
            </el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
    </AppDialog>
  </el-card>
</template>

<style scoped>
.page-desc {
  margin: 0 0 16px;
  color: var(--el-text-color-secondary);
  font-size: 13px;
  line-height: 1.6;
}
.filter-form { margin-bottom: 16px; }
.preset-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 8px;
}
.form-hint {
  margin: 6px 0 0;
  color: var(--el-text-color-secondary);
  font-size: 12px;
  line-height: 1.5;
}
</style>
