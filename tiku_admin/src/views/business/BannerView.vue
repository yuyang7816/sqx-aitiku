<script setup>
import { onMounted, reactive, ref } from 'vue'
import AppDialog from '@/components/common/AppDialog.vue'
import BasePagination from '@/components/common/BasePagination.vue'
import ImageUpload from '@/components/common/ImageUpload.vue'
import TableRowActions from '@/components/common/TableRowActions.vue'
import TableStateSwitch from '@/components/common/TableStateSwitch.vue'
import { bannerApi, updateBannerStateApi } from '@/api/modules/banner'
import {
  BANNER_CLASSIFY_OPTIONS,
  BANNER_STATE_OPTIONS,
  getBannerClassifyLabel,
  getBannerStateLabel,
} from '@/constants/banner'
import { useFilterFormKey } from '@/composables/useFilterFormKey'
import FilterSelect from '@/components/common/FilterSelect.vue'
import FilterSummary from '@/components/common/FilterSummary.vue'
import { FILTER_SELECT_EMPTY, isFilterEmpty, normalizeNumberFilterQuery } from '@/utils/filter-form'
import { validateForm } from '@/utils/form'
import { confirmAction, showError, showSuccess } from '@/utils/modal'
import { getApiBaseUrl } from '@/utils/tool'
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
  { key: 'name', label: '名称', type: 'text', placeholder: 'Banner 名称' },
  { key: 'classify', label: '展示位置', type: 'select', options: BANNER_CLASSIFY_OPTIONS, placeholder: '全部位置' },
  { key: 'state', label: '上下架', type: 'select', options: BANNER_STATE_OPTIONS, placeholder: '全部状态' },
]

const filters = reactive({
  name: '',
  classify: FILTER_SELECT_EMPTY,
  state: FILTER_SELECT_EMPTY,
})

const form = reactive({
  name: '',
  imageUrl: '',
  url: '',
  classify: 1,
  sort: 1,
  state: 1,
  description: '',
})

const rules = {
  name: [{ required: true, message: '请输入 Banner 名称', trigger: 'blur' }],
  imageUrl: [{ required: true, message: '请上传 Banner 图片', trigger: 'change' }],
  classify: [{ required: true, message: '请选择展示位置', trigger: 'change' }],
  sort: [{ required: true, message: '请输入排序', trigger: 'change' }],
  state: [{ required: true, message: '请选择上下架状态', trigger: 'change' }],
}

function resolveFileUrl(path = '') {
  const value = String(path).trim()
  if (!value) return ''
  if (/^https?:\/\//i.test(value)) return value
  const base = getApiBaseUrl().replace(/\/$/, '')
  return `${base}/${value.replace(/^\//, '')}`
}

function buildQueryParams() {
  return {
    page: pageNum.value,
    limit: pageSize.value,
    name: filters.name || undefined,
    classify: normalizeNumberFilterQuery(filters.classify),
    state: normalizeNumberFilterQuery(filters.state),
  }
}

async function loadData() {
  loading.value = true
  try {
    const data = await bannerApi.page(buildQueryParams())
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
  Object.assign(filters, { name: '', classify: FILTER_SELECT_EMPTY, state: FILTER_SELECT_EMPTY })
  bumpFilterFormKey()
  pageNum.value = 1
  loadData()
}

function resetForm(initial = {}) {
  Object.assign(form, {
    name: '',
    imageUrl: '',
    url: '',
    classify: 1,
    sort: 1,
    state: 1,
    description: '',
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
    const data = await bannerApi.info(row.id)
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
      imageUrl: form.imageUrl,
      url: form.url,
      classify: form.classify,
      sort: form.sort,
      description: form.description,
    }
    if (editingId.value) {
      await bannerApi.update({ ...payload, id: editingId.value })
      await updateBannerStateApi({ id: editingId.value, state: form.state })
    } else {
      await bannerApi.save(payload)
      await loadData()
      if (form.state !== 1) {
        const created = tableData.value.find(
          (row) => row.name === form.name && row.classify === form.classify,
        )
        if (created) {
          await updateBannerStateApi({ id: created.id, state: form.state })
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
  await confirmAction(`确认删除 Banner「${row.name}」吗？`)
  await bannerApi.delete({ id: row.id })
  showSuccess('删除成功')
  await loadData()
}

async function handleStateChange(row, nextState) {
  const previousState = row.state
  if (nextState === previousState) {
    return
  }
  const actionLabel = nextState === 1 ? '上架' : '下架'
  stateUpdatingId.value = row.id
  try {
    await updateBannerStateApi({ id: row.id, state: nextState })
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
        <strong v-if="!embedded">Banner 轮播配置</strong>
        <el-button type="primary" @click="openCreate">新增 Banner</el-button>
      </div>
    </template>

    <el-form :key="filterFormKey" :inline="true" class="filter-form" @submit.prevent="handleSearch">
      <el-form-item label="名称">
        <el-input v-model="filters.name" clearable placeholder="Banner 名称" @keyup.enter="handleSearch" />
      </el-form-item>
      <el-form-item label="展示位置">
        <FilterSelect v-model="filters.classify" clearable placeholder="全部位置">
          <el-option v-for="item in BANNER_CLASSIFY_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
        </FilterSelect>
      </el-form-item>
      <el-form-item label="上下架">
        <FilterSelect v-model="filters.state" clearable placeholder="全部状态">
          <el-option v-for="item in BANNER_STATE_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
        </FilterSelect>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
      </el-form-item>
    </el-form>

    <FilterSummary :filters="filters" :fields="FILTER_FIELDS" />

    <el-table v-loading="loading" :data="tableData" border>
      <el-table-column prop="name" label="名称" min-width="140" show-overflow-tooltip />
      <el-table-column label="图片" width="120">
        <template #default="{ row }">
          <el-image
            v-if="row.imageUrl"
            :src="resolveFileUrl(row.imageUrl)"
            fit="cover"
            style="width: 88px; height: 48px; border-radius: 4px"
            :preview-src-list="[resolveFileUrl(row.imageUrl)]"
          />
          <span v-else class="text-muted">未上传</span>
        </template>
      </el-table-column>
      <el-table-column prop="url" label="跳转链接" min-width="180" show-overflow-tooltip />
      <el-table-column label="展示位置" width="130">
        <template #default="{ row }">{{ getBannerClassifyLabel(row.classify) }}</template>
      </el-table-column>
      <el-table-column prop="sort" label="排序" width="80" />
      <el-table-column label="上下架" width="110">
        <template #default="{ row }">
          <TableStateSwitch
            :model-value="row.state"
            :loading="stateUpdatingId === row.id"
            @change="(nextState) => handleStateChange(row, nextState)"
          />
        </template>
      </el-table-column>
      <el-table-column prop="description" label="描述" min-width="140" show-overflow-tooltip />
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
      :title="editingId ? '编辑 Banner' : '新增 Banner'"
      :loading="dialogLoading"
      @confirm="submitForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" maxlength="50" show-word-limit placeholder="如：会员中心推广" />
        </el-form-item>
        <el-form-item label="Banner 图片" prop="imageUrl">
          <ImageUpload v-model="form.imageUrl" :limit="1" />
        </el-form-item>
        <el-form-item label="跳转链接">
          <el-input v-model="form.url" placeholder="uniapp 页面路径或 https 外链" />
        </el-form-item>
        <el-form-item label="展示位置" prop="classify">
          <el-select v-model="form.classify" style="width: 100%">
            <el-option v-for="item in BANNER_CLASSIFY_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="form.sort" :min="0" :max="9999" />
          <span class="form-hint">数值越小越靠前，移动端按此顺序展示</span>
        </el-form-item>
        <el-form-item label="上下架" prop="state">
          <el-radio-group v-model="form.state">
            <el-radio v-for="item in BANNER_STATE_OPTIONS" :key="item.value" :value="item.value">
              {{ item.label }}
            </el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="3" maxlength="200" show-word-limit placeholder="运营备注，C 端不展示" />
        </el-form-item>
      </el-form>
    </AppDialog>
  </el-card>
</template>

<style scoped>
.filter-form { margin-bottom: 16px; }
.text-muted { color: var(--el-text-color-secondary); font-size: 12px; }
.form-hint { margin-left: 12px; color: var(--el-text-color-secondary); font-size: 12px; }
</style>
