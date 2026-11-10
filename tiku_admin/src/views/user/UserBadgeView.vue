<script setup>
import { onMounted, reactive, ref } from 'vue'
import AppDialog from '@/components/common/AppDialog.vue'
import BasePagination from '@/components/common/BasePagination.vue'
import TableRowActions from '@/components/common/TableRowActions.vue'
import TableStateSwitch from '@/components/common/TableStateSwitch.vue'
import { userBadgeApi } from '@/api/modules/user-badge'
import {
  USER_BADGE_RULE_TYPE_OPTIONS,
  USER_BADGE_STATE_OPTIONS,
  getUserBadgeRuleTypeLabel,
} from '@/constants/user-badge'
import FilterSelect from '@/components/common/FilterSelect.vue'
import FilterSummary from '@/components/common/FilterSummary.vue'
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

const FILTER_FIELDS = [
  { key: 'name', label: '勋章名称', type: 'text', placeholder: '如：初出茅庐' },
  { key: 'state', label: '状态', type: 'select', options: USER_BADGE_STATE_OPTIONS, placeholder: '全部状态' },
]

const filters = reactive({
  name: '',
  state: FILTER_SELECT_EMPTY,
})

const form = reactive({
  badgeCode: '',
  name: '',
  conditionText: '',
  ruleType: 'submit_count',
  targetValue: 1,
  sortOrder: 0,
  state: 1,
})

const rules = {
  badgeCode: [{ required: true, message: '请输入勋章编码', trigger: 'blur' }],
  name: [{ required: true, message: '请输入勋章名称', trigger: 'blur' }],
  conditionText: [{ required: true, message: '请输入获得条件', trigger: 'blur' }],
  ruleType: [{ required: true, message: '请选择规则类型', trigger: 'change' }],
  targetValue: [{ required: true, message: '请输入目标值', trigger: 'change' }],
  sortOrder: [{ required: true, message: '请输入排序', trigger: 'change' }],
}

function buildQueryParams() {
  return {
    page: pageNum.value,
    limit: pageSize.value,
    name: filters.name || undefined,
    state: normalizeNumberFilterQuery(filters.state),
  }
}

async function loadData() {
  loading.value = true
  try {
    const data = await userBadgeApi.page(buildQueryParams())
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
  Object.assign(filters, { name: '', state: FILTER_SELECT_EMPTY })
  pageNum.value = 1
  loadData()
}

function resetForm(initial = {}) {
  Object.assign(form, {
    badgeCode: '',
    name: '',
    conditionText: '',
    ruleType: 'submit_count',
    targetValue: 1,
    sortOrder: 0,
    state: 1,
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
    const data = await userBadgeApi.info({ id: row.id })
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
      conditionText: form.conditionText,
      ruleType: form.ruleType,
      targetValue: form.targetValue,
      sortOrder: form.sortOrder,
      state: form.state,
    }
    if (editingId.value) {
      await userBadgeApi.update({ ...payload, id: editingId.value })
      showSuccess('更新成功')
    } else {
      await userBadgeApi.save({ ...payload, badgeCode: form.badgeCode })
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
  await confirmAction(`确认删除勋章「${row.name}」吗？`)
  try {
    await userBadgeApi.delete({ id: row.id })
    showSuccess('删除成功')
    await loadData()
  } catch (error) {
    showError(error?.msg || error?.message || '删除失败')
  }
}

async function handleStateChange(row, nextStatus) {
  const previousStatus = row.state
  if (nextStatus === previousStatus) return
  stateUpdatingId.value = row.id
  try {
    await userBadgeApi.update({ id: row.id, state: nextStatus })
    row.state = nextStatus
    showSuccess(nextStatus === 1 ? '已启用' : '已禁用')
  } catch (error) {
    row.state = previousStatus
    showError(error?.msg || error?.message || '状态更新失败')
  } finally {
    stateUpdatingId.value = null
  }
}

onMounted(() => loadData())
</script>

<template>
  <el-card shadow="never" class="page-card">
    <template #header>
      <div class="card-header">
        <strong v-if="!embedded">用户勋章管理</strong>
        <el-button type="primary" @click="openCreate">新增勋章</el-button>
      </div>
    </template>

    <p class="page-desc">
      维护 C 端「我的勋章」里程碑的名称、解锁条件与规则。仅「启用」状态的勋章会参与解锁判定并展示在用户端。
    </p>

    <el-form :inline="true" class="filter-form" @submit.prevent="handleSearch">
      <el-form-item label="勋章名称">
        <el-input v-model="filters.name" clearable placeholder="如：初出茅庐" @keyup.enter="handleSearch" />
      </el-form-item>
      <el-form-item label="状态">
        <FilterSelect v-model="filters.state" clearable placeholder="全部状态">
          <el-option
            v-for="item in USER_BADGE_STATE_OPTIONS"
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

    <el-table v-loading="loading" :data="tableData" border empty-text="暂无勋章">
      <el-table-column prop="name" label="名称" min-width="110" />
      <el-table-column prop="conditionText" label="获得条件" min-width="200" show-overflow-tooltip />
      <el-table-column label="规则类型" width="120">
        <template #default="{ row }">{{ getUserBadgeRuleTypeLabel(row.ruleType) }}</template>
      </el-table-column>
      <el-table-column prop="targetValue" label="目标值" width="90" />
      <el-table-column prop="sortOrder" label="排序" width="80" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <TableStateSwitch
            :model-value="row.state"
            :active-value="1"
            :inactive-value="0"
            :loading="stateUpdatingId === row.id"
            @change="(nextStatus) => handleStateChange(row, nextStatus)"
          />
        </template>
      </el-table-column>
      <el-table-column prop="updateTime" label="更新时间" width="170" />
      <el-table-column label="操作" min-width="140" fixed="right">
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
      :title="editingId ? '编辑勋章' : '新增勋章'"
      :loading="dialogLoading"
      @confirm="submitForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="108px">
        <el-form-item v-if="!editingId" label="内部标识" prop="badgeCode">
          <el-input
            v-model="form.badgeCode"
            maxlength="64"
            placeholder="英文标识，如 first_practice"
          />
          <p class="form-hint">创建后不可修改，用户端不会展示</p>
        </el-form-item>
        <el-form-item v-else label="内部标识">
          <el-input v-model="form.badgeCode" disabled />
        </el-form-item>
        <el-form-item label="勋章名称" prop="name">
          <el-input v-model="form.name" maxlength="32" />
        </el-form-item>
        <el-form-item label="获得条件" prop="conditionText">
          <el-input v-model="form.conditionText" type="textarea" :rows="2" maxlength="255" show-word-limit />
        </el-form-item>
        <el-form-item label="规则类型" prop="ruleType">
          <el-select v-model="form.ruleType" placeholder="请选择">
            <el-option
              v-for="item in USER_BADGE_RULE_TYPE_OPTIONS"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="目标值" prop="targetValue">
          <el-input-number v-model="form.targetValue" :min="1" :max="999999" />
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="form.sortOrder" :min="0" :max="9999" />
        </el-form-item>
        <el-form-item label="状态" prop="state">
          <el-radio-group v-model="form.state">
            <el-radio v-for="item in USER_BADGE_STATE_OPTIONS" :key="item.value" :value="item.value">
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
.form-hint {
  margin: 6px 0 0;
  color: var(--el-text-color-secondary);
  font-size: 12px;
}
</style>
