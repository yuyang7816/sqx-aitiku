<script setup>
import { onMounted, reactive, ref } from 'vue'
import AppDialog from '@/components/common/AppDialog.vue'
import BasePagination from '@/components/common/BasePagination.vue'
import ImageUpload from '@/components/common/ImageUpload.vue'
import TableRowActions from '@/components/common/TableRowActions.vue'
import { vipRightsApi } from '@/api/modules/vip-rights'
import {
  VIP_RIGHTS_CODE_OPTIONS,
  getVipRightsCodeLabel,
} from '@/constants/vip-rights'
import FilterSelect from '@/components/common/FilterSelect.vue'
import FilterSummary from '@/components/common/FilterSummary.vue'
import { useFilterFormKey } from '@/composables/useFilterFormKey'
import { FILTER_SELECT_EMPTY, normalizeSelectFilterQuery } from '@/utils/filter-form'
import { validateForm } from '@/utils/form'
import { confirmAction, showError, showSuccess } from '@/utils/modal'
import { useSystemConfigEmbedded } from '@/composables/useSystemConfigEmbedded'

const embedded = useSystemConfigEmbedded()
import { getApiBaseUrl } from '@/utils/tool'

const loading = ref(false)
const dialogVisible = ref(false)
const dialogLoading = ref(false)
const formRef = ref()
const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const editingId = ref(null)
const { filterFormKey, bumpFilterFormKey } = useFilterFormKey()

const FILTER_FIELDS = [
  { key: 'name', label: '权益名称', type: 'text', placeholder: '如：AI 答疑' },
  { key: 'rightsCode', label: '权益业务键', type: 'select', options: VIP_RIGHTS_CODE_OPTIONS, placeholder: '全部权益' },
]

const filters = reactive({
  name: '',
  rightsCode: FILTER_SELECT_EMPTY,
})

const form = reactive({
  name: '',
  rightsCode: '',
  img: '',
  freeText: '',
  vipText: '',
  sort: 1,
})

const rules = {
  name: [{ required: true, message: '请输入权益名称', trigger: 'blur' }],
  sort: [{ required: true, message: '请输入排序', trigger: 'change' }],
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
    rightsCode: normalizeSelectFilterQuery(filters.rightsCode),
  }
}

async function loadData() {
  loading.value = true
  try {
    const data = await vipRightsApi.page(buildQueryParams())
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
  Object.assign(filters, { name: '', rightsCode: FILTER_SELECT_EMPTY })
  bumpFilterFormKey()
  pageNum.value = 1
  loadData()
}

function resetForm(initial = {}) {
  Object.assign(form, {
    name: '',
    rightsCode: '',
    img: '',
    freeText: '',
    vipText: '',
    sort: 1,
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
    const data = await vipRightsApi.info(row.id)
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
      rightsCode: form.rightsCode || undefined,
      img: form.img,
      freeText: form.freeText,
      vipText: form.vipText,
      sort: form.sort,
    }
    if (editingId.value) {
      await vipRightsApi.update({ ...payload, id: editingId.value })
      showSuccess('更新成功')
    } else {
      await vipRightsApi.save(payload)
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
  await confirmAction(`确认删除权益「${row.name}」吗？`)
  await vipRightsApi.delete({ id: row.id })
  showSuccess('删除成功')
  await loadData()
}

onMounted(() => loadData())
</script>

<template>
  <el-card shadow="never" class="page-card">
    <template #header>
      <div class="card-header">
        <strong v-if="!embedded">VIP 权益配置</strong>
        <el-button type="primary" @click="openCreate">新增权益</el-button>
      </div>
    </template>

    <p class="page-desc">
      维护会员权益对比项与支付页图标。对比表展示在 App 会员中心，图标用于支付成功页等场景。
      AI/OCR 免费次数文案会随 OCR、AI 配置自动更新。
    </p>

    <el-form :key="filterFormKey" :inline="true" class="filter-form" @submit.prevent="handleSearch">
      <el-form-item label="权益名称">
        <el-input v-model="filters.name" clearable placeholder="如：AI 答疑" @keyup.enter="handleSearch" />
      </el-form-item>
      <el-form-item label="权益业务键">
        <FilterSelect v-model="filters.rightsCode" clearable placeholder="全部权益">
          <el-option
            v-for="item in VIP_RIGHTS_CODE_OPTIONS"
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
      <el-table-column prop="name" label="权益名称" min-width="140" show-overflow-tooltip />
      <el-table-column label="图标" width="100">
        <template #default="{ row }">
          <el-image
            v-if="row.img"
            :src="resolveFileUrl(row.img)"
            fit="cover"
            style="width: 48px; height: 48px; border-radius: 4px"
            :preview-src-list="[resolveFileUrl(row.img)]"
          />
          <span v-else class="text-muted">未上传</span>
        </template>
      </el-table-column>
      <el-table-column prop="freeText" label="免费版文案" min-width="120" show-overflow-tooltip />
      <el-table-column prop="vipText" label="会员版文案" min-width="120" show-overflow-tooltip />
      <el-table-column prop="sort" label="排序" width="80" />
      <el-table-column prop="createTime" label="创建时间" width="170" />
      <el-table-column label="操作" min-width="150" fixed="right">
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
      :title="editingId ? '编辑 VIP 权益' : '新增 VIP 权益'"
      :loading="dialogLoading"
      @confirm="submitForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="权益名称" prop="name">
          <el-input v-model="form.name" maxlength="50" show-word-limit placeholder="如：AI 答疑" />
        </el-form-item>
        <el-form-item label="权益业务键">
          <el-select v-model="form.rightsCode" clearable placeholder="对比表项必选，纯图标项可留空" style="width: 100%">
            <el-option
              v-for="item in VIP_RIGHTS_CODE_OPTIONS"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
          <p class="form-hint">填写后将出现在 App 会员权益对比表中；AI/OCR 项的免费文案以配置页为准</p>
        </el-form-item>
        <el-form-item label="权益图标">
          <ImageUpload v-model="form.img" :limit="1" />
          <p class="form-hint">用于支付成功页等场景的权益图标展示</p>
        </el-form-item>
        <el-form-item label="免费版文案">
          <el-input v-model="form.freeText" maxlength="64" show-word-limit placeholder="如：5次/日、基础版" />
        </el-form-item>
        <el-form-item label="会员版文案">
          <el-input v-model="form.vipText" maxlength="64" show-word-limit placeholder="如：无限次、✓" />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="form.sort" :min="0" :max="9999" />
          <span class="form-hint">数值越小越靠前</span>
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
.text-muted { color: var(--el-text-color-secondary); font-size: 12px; }
.form-hint {
  margin: 6px 0 0;
  color: var(--el-text-color-secondary);
  font-size: 12px;
  line-height: 1.5;
}
</style>
