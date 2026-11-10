<script setup>
import { onMounted, reactive, ref } from 'vue'
import AppDialog from '@/components/common/AppDialog.vue'
import BasePagination from '@/components/common/BasePagination.vue'
import TableRowActions from '@/components/common/TableRowActions.vue'
import TableStateSwitch from '@/components/common/TableStateSwitch.vue'
import { homeShortcutApi, updateHomeShortcutStateApi } from '@/api/modules/home-shortcut'
import {
  HOME_SHORTCUT_ACTION_OPTIONS,
  HOME_SHORTCUT_STATE_OPTIONS,
  getHomeShortcutActionLabel,
} from '@/constants/home-shortcut'
import { useFilterFormKey } from '@/composables/useFilterFormKey'
import FilterSelect from '@/components/common/FilterSelect.vue'
import FilterSummary from '@/components/common/FilterSummary.vue'
import { FILTER_SELECT_EMPTY, normalizeNumberFilterQuery } from '@/utils/filter-form'
import ImageUpload from '@/components/common/ImageUpload.vue'
import { validateForm } from '@/utils/form'
import { parseCardBgColor, normalizeCardBgColor } from '@/utils/card-display-form'
import { resolveImageUrl } from '@/utils/image-field'
import { confirmAction, showError, showSuccess } from '@/utils/modal'

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
  { key: 'title', label: '标题', type: 'text', placeholder: '入口标题' },
  { key: 'state', label: '状态', type: 'select', options: HOME_SHORTCUT_STATE_OPTIONS, placeholder: '全部状态' },
]

const filters = reactive({
  title: '',
  state: FILTER_SELECT_EMPTY,
})

const DEFAULT_BG_COLOR = '#EAF1FF'

const form = reactive({
  title: '',
  icon: '',
  bgStyle: DEFAULT_BG_COLOR,
  url: '',
  actionType: '',
  sortOrder: 1,
  state: 1,
})

const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  icon: [{ required: true, message: '请上传图标', trigger: 'change' }],
  bgStyle: [{ required: true, message: '请选择背景色', trigger: 'change' }],
  sortOrder: [{ required: true, message: '请输入排序', trigger: 'change' }],
  state: [{ required: true, message: '请选择状态', trigger: 'change' }],
}

function buildQueryParams() {
  return {
    page: pageNum.value,
    limit: pageSize.value,
    title: filters.title || undefined,
    state: normalizeNumberFilterQuery(filters.state),
  }
}

async function loadData() {
  loading.value = true
  try {
    const data = await homeShortcutApi.page(buildQueryParams())
    tableData.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pageNum.value = 1
  loadData()
}

function handleReset() {
  Object.assign(filters, { title: '', state: FILTER_SELECT_EMPTY })
  bumpFilterFormKey()
  pageNum.value = 1
  loadData()
}

function resetForm(initial = {}) {
  const bgStyle = parseCardBgColor(initial.bgStyle, DEFAULT_BG_COLOR)
  Object.assign(form, {
    title: '',
    icon: '',
    bgStyle: DEFAULT_BG_COLOR,
    url: '',
    actionType: '',
    sortOrder: 1,
    state: 1,
    ...initial,
    bgStyle,
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
    const data = await homeShortcutApi.info(row.id)
    resetForm({
      ...(data || row),
      actionType: data?.actionType || '',
    })
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
    const bgStyle = normalizeCardBgColor(form.bgStyle)
    if (!bgStyle) {
      showError('请选择背景色')
      dialogLoading.value = false
      return
    }
    const payload = {
      title: form.title,
      icon: form.icon,
      iconColor: '',
      bgStyle,
      url: form.url || undefined,
      actionType: form.actionType || undefined,
      sortOrder: form.sortOrder,
    }
    if (editingId.value) {
      await homeShortcutApi.update({ ...payload, id: editingId.value })
      await updateHomeShortcutStateApi({ id: editingId.value, state: form.state })
    } else {
      await homeShortcutApi.save(payload)
      await loadData()
      if (form.state !== 1) {
        const created = tableData.value.find((row) => row.title === form.title && row.sortOrder === form.sortOrder)
        if (created) {
          await updateHomeShortcutStateApi({ id: created.id, state: form.state })
        }
      }
    }
    showSuccess(editingId.value ? '更新成功' : '创建成功')
    dialogVisible.value = false
    await loadData()
  } catch (error) {
    showError(error?.msg || error?.message || '保存失败')
  } finally {
    dialogLoading.value = false
  }
}

async function removeRow(row) {
  await confirmAction(`确认删除快捷入口「${row.title}」吗？`)
  await homeShortcutApi.delete({ id: row.id })
  showSuccess('删除成功')
  await loadData()
}

async function handleStateChange(row, nextState) {
  const previousState = row.state
  if (nextState === previousState) return
  const actionLabel = nextState === 1 ? '启用' : '禁用'
  stateUpdatingId.value = row.id
  try {
    await updateHomeShortcutStateApi({ id: row.id, state: nextState })
    row.state = nextState
    showSuccess(`已${actionLabel}`)
  } catch (error) {
    row.state = previousState
    showError(error?.msg || error?.message || `${actionLabel}失败`)
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
        <strong>首页快捷入口</strong>
        <el-button type="primary" @click="openCreate">新增入口</el-button>
      </div>
    </template>

    <el-form :key="filterFormKey" :inline="true" class="filter-form" @submit.prevent="handleSearch">
      <el-form-item label="标题">
        <el-input v-model="filters.title" clearable placeholder="入口标题" @keyup.enter="handleSearch" />
      </el-form-item>
      <el-form-item label="状态">
        <FilterSelect v-model="filters.state" clearable placeholder="全部状态">
          <el-option v-for="item in HOME_SHORTCUT_STATE_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
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
      <el-table-column prop="title" label="标题" min-width="120" show-overflow-tooltip />
      <el-table-column label="图标" width="80">
        <template #default="{ row }">
          <el-image
            v-if="row.icon"
            class="icon-preview"
            :src="resolveImageUrl(row.icon)"
            fit="contain"
          />
        </template>
      </el-table-column>
      <el-table-column label="背景色" width="90">
        <template #default="{ row }">
          <span class="color-preview" :style="{ background: parseCardBgColor(row.bgStyle, DEFAULT_BG_COLOR) }"></span>
        </template>
      </el-table-column>
      <el-table-column prop="url" label="跳转路径" min-width="180" show-overflow-tooltip />
      <el-table-column label="特殊动作" width="110">
        <template #default="{ row }">{{ getHomeShortcutActionLabel(row.actionType) }}</template>
      </el-table-column>
      <el-table-column prop="sortOrder" label="排序" width="80" />
      <el-table-column label="状态" width="110">
        <template #default="{ row }">
          <TableStateSwitch
            :model-value="row.state"
            active-text="启用"
            inactive-text="禁用"
            :loading="stateUpdatingId === row.id"
            @change="(nextState) => handleStateChange(row, nextState)"
          />
        </template>
      </el-table-column>
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
      :title="editingId ? '编辑快捷入口' : '新增快捷入口'"
      :loading="dialogLoading"
      @confirm="submitForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" maxlength="32" show-word-limit placeholder="如：刷题中心" />
        </el-form-item>
        <el-form-item label="图标" prop="icon">
          <ImageUpload v-model="form.icon" :limit="1" />
          <p class="form-hint block-hint">建议上传 PNG 透明底图标，尺寸 96×96 左右</p>
        </el-form-item>
        <el-form-item label="背景色" prop="bgStyle">
          <div class="color-field">
            <el-color-picker v-model="form.bgStyle" :show-alpha="false" />
            <span class="color-field__value">{{ form.bgStyle }}</span>
          </div>
          <p class="form-hint block-hint">图标底色，直接在色板里点选即可</p>
        </el-form-item>
        <el-form-item label="跳转路径">
          <el-input v-model="form.url" placeholder="uniapp 页面路径，特殊动作时可作兜底" />
        </el-form-item>
        <el-form-item label="特殊动作">
          <el-select v-model="form.actionType" clearable style="width: 100%">
            <el-option v-for="item in HOME_SHORTCUT_ACTION_OPTIONS" :key="item.label" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="form.sortOrder" :min="0" :max="9999" />
          <span class="form-hint">数值越小越靠前</span>
        </el-form-item>
        <el-form-item label="状态" prop="state">
          <el-radio-group v-model="form.state">
            <el-radio v-for="item in HOME_SHORTCUT_STATE_OPTIONS" :key="item.value" :value="item.value">
              {{ item.label }}
            </el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
    </AppDialog>
  </el-card>
</template>

<style scoped>
.filter-form { margin-bottom: 16px; }
.form-hint { margin-left: 12px; color: var(--el-text-color-secondary); font-size: 12px; }
.block-hint { margin: 6px 0 0; margin-left: 0 !important; }
.icon-preview { width: 40px; height: 40px; border-radius: 8px; border: 1px solid var(--el-border-color); }
.color-preview { display: inline-block; width: 28px; height: 28px; border-radius: 6px; border: 1px solid var(--el-border-color); }
.color-field { display: flex; align-items: center; gap: 12px; }
.color-field__value { color: var(--el-text-color-secondary); font-size: 13px; }
</style>
