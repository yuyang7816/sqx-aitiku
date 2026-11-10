<script setup>
import { onMounted, reactive, ref } from 'vue'
import AppDialog from '@/components/common/AppDialog.vue'
import BasePagination from '@/components/common/BasePagination.vue'
import FilterSummary from '@/components/common/FilterSummary.vue'
import { createDictApi, deleteDictApi, getDictPage, updateDictApi } from '@/api/modules/dicts'
import { patchFormModel, resetFormModel, validateForm } from '@/utils/form'
import { confirmAction, showSuccess } from '@/utils/modal'
import { isAuth } from '@/utils/tool'
import { useSystemConfigEmbedded } from '@/composables/useSystemConfigEmbedded'

const embedded = useSystemConfigEmbedded()

const loading = ref(false)
const dialogVisible = ref(false)
const dialogLoading = ref(false)
const formRef = ref()
const editingId = ref(null)
const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const filters = reactive({ keyword: '', type: '' })

const FILTER_FIELDS = [
  { key: 'keyword', label: '关键词', type: 'text', placeholder: '名称 / 类型' },
  { key: 'type', label: '类型', type: 'text', placeholder: '字典类型' },
]
const initialForm = { name: '', type: '', code: '', value: '', orderNum: 0, remark: '' }
const form = reactive({ ...initialForm })
const rules = {
  name: [{ required: true, message: '请输入字典名称', trigger: 'blur' }],
  type: [{ required: true, message: '请输入字典类型', trigger: 'blur' }],
  code: [{ required: true, message: '请输入字典码', trigger: 'blur' }],
  value: [{ required: true, message: '请输入字典值', trigger: 'blur' }],
}

async function loadData() {
  loading.value = true
  try {
    const data = await getDictPage({
      page: pageNum.value,
      limit: pageSize.value,
      keyword: filters.keyword,
      type: filters.type,
    })
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
  Object.assign(filters, { keyword: '', type: '' })
  pageNum.value = 1
  loadData()
}

function openCreate() {
  editingId.value = null
  resetFormModel(form, initialForm)
  dialogVisible.value = true
}

function openEdit(row) {
  editingId.value = row.id
  patchFormModel(form, row, initialForm)
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await validateForm(formRef)
  if (!valid) return
  dialogLoading.value = true
  try {
    if (editingId.value) await updateDictApi({ ...form, id: editingId.value })
    else await createDictApi(form)
    showSuccess(editingId.value ? '字典已更新' : '字典已新增')
    dialogVisible.value = false
    loadData()
  } finally {
    dialogLoading.value = false
  }
}

async function handleDelete(row) {
  await confirmAction(`确认删除字典“${row.name}”吗？`)
  await deleteDictApi([row.id])
  showSuccess('字典已删除')
  loadData()
}

onMounted(loadData)
</script>

<template>
  <el-card shadow="never" class="page-card">
    <template #header>
      <div class="card-header">
        <strong v-if="!embedded">字典管理</strong>
        <el-button v-if="isAuth('sys:dict:save')" type="primary" @click="openCreate">新增字典</el-button>
      </div>
    </template>
    <el-form :inline="true" class="search-grid">
      <el-form-item label="关键词">
        <el-input v-model="filters.keyword" clearable placeholder="名称 / 类型" />
      </el-form-item>
      <el-form-item label="类型">
        <el-input v-model="filters.type" clearable placeholder="字典类型" />
      </el-form-item>
      <el-form-item>
        <el-space>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-space>
      </el-form-item>
    </el-form>
    <FilterSummary :filters="filters" :fields="FILTER_FIELDS" />
    <el-table v-loading="loading" :data="tableData" border>
      <el-table-column prop="name" label="字典名称" min-width="140" />
      <el-table-column prop="type" label="字典类型" min-width="140" />
      <el-table-column prop="code" label="字典码" min-width="120" />
      <el-table-column prop="value" label="字典值" min-width="120" />
      <el-table-column prop="orderNum" label="排序" min-width="80" />
      <el-table-column prop="remark" label="备注" min-width="180" />
      <el-table-column label="操作" min-width="150" fixed="right">
        <template #default="{ row }">
          <el-space>
            <el-button v-if="isAuth('sys:dict:update')" link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button v-if="isAuth('sys:dict:delete')" link type="danger" @click="handleDelete(row)">删除</el-button>
          </el-space>
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
    <AppDialog v-model="dialogVisible" :title="editingId ? '编辑字典' : '新增字典'" :loading="dialogLoading" @confirm="handleSubmit">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="96px">
        <el-form-item label="字典名称" prop="name"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="字典类型" prop="type"><el-input v-model="form.type" /></el-form-item>
        <el-form-item label="字典码" prop="code"><el-input v-model="form.code" /></el-form-item>
        <el-form-item label="字典值" prop="value"><el-input v-model="form.value" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.orderNum" :min="0" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="3" /></el-form-item>
      </el-form>
    </AppDialog>
  </el-card>
</template>
