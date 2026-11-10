<script setup>
import { computed, nextTick, onMounted, reactive, ref } from 'vue'
import AppDialog from '@/components/common/AppDialog.vue'
import FilterSummary from '@/components/common/FilterSummary.vue'
import { createMenuApi, deleteMenuApi, getMenuInfoApi, getMenuListApi, getMenuSelectApi, updateMenuApi } from '@/api/modules/menus'
import { validateForm } from '@/utils/form'
import { confirmAction, showSuccess } from '@/utils/modal'
import { isAuth } from '@/utils/tool'

const loading = ref(false)
const dialogVisible = ref(false)
const dialogLoading = ref(false)
const detailLoading = ref(false)
const formRef = ref()
const menuList = ref([])
const parentOptions = ref([])
const editingId = ref(0)
const filters = reactive({ keyword: '', parentName: '' })

const FILTER_FIELDS = [
  { key: 'keyword', label: '权限名称/标识', type: 'text', placeholder: '名称或授权标识' },
  { key: 'parentName', label: '所属菜单', type: 'text', placeholder: '上级菜单名称' },
]

const form = reactive({
  menuId: 0,
  name: '',
  parentId: 0,
  perms: '',
  orderNum: 0,
})

const rules = {
  name: [{ required: true, message: '请输入权限名称', trigger: 'blur' }],
  parentId: [{ required: true, message: '请选择所属菜单', trigger: 'change' }],
  perms: [{ required: true, message: '请输入授权标识', trigger: 'blur' }],
}

const tableData = computed(() => {
  const parentMap = new Map(menuList.value.map((item) => [item.menuId, item.name]))
  return menuList.value
    .filter((item) => item.type === 2)
    .map((item) => ({
      ...item,
      parentName: parentMap.get(item.parentId) || '-',
    }))
    .filter((item) => {
      const keyword = filters.keyword.trim()
      const parentName = filters.parentName.trim()
      const matchKeyword = !keyword || item.name?.includes(keyword) || item.perms?.includes(keyword)
      const matchParent = !parentName || item.parentName?.includes(parentName)
      return matchKeyword && matchParent
    })
})

function resetForm() {
  Object.assign(form, { menuId: 0, name: '', parentId: 0, perms: '', orderNum: 0 })
}

async function loadData() {
  loading.value = true
  try {
    menuList.value = await getMenuListApi()
    const selectList = await getMenuSelectApi()
    parentOptions.value = selectList.filter((item) => item.type === 1)
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  // computed 自动过滤
}

function handleReset() {
  filters.keyword = ''
  filters.parentName = ''
}

async function openCreate() {
  editingId.value = 0
  resetForm()
  dialogVisible.value = true
  await nextTick()
  formRef.value?.clearValidate()
}

async function openEdit(row) {
  editingId.value = row.menuId
  dialogVisible.value = true
  detailLoading.value = true
  try {
    const detail = await getMenuInfoApi(row.menuId)
    Object.assign(form, {
      menuId: detail.menuId || 0,
      name: detail.name || '',
      parentId: detail.parentId || 0,
      perms: detail.perms || '',
      orderNum: Number(detail.orderNum || 0),
    })
    await nextTick()
    formRef.value?.clearValidate()
  } finally {
    detailLoading.value = false
  }
}

async function handleSubmit() {
  const valid = await validateForm(formRef)
  if (!valid) return
  dialogLoading.value = true
  try {
    const payload = {
      menuId: editingId.value || undefined,
      type: 2,
      name: form.name.trim(),
      parentId: form.parentId,
      perms: form.perms.trim(),
      orderNum: Number(form.orderNum || 0),
      url: '',
      icon: '',
    }
    if (editingId.value) await updateMenuApi(payload)
    else await createMenuApi(payload)
    showSuccess(editingId.value ? '权限已更新' : '权限已新增')
    dialogVisible.value = false
    await loadData()
  } finally {
    dialogLoading.value = false
  }
}

async function handleDelete(row) {
  await confirmAction(`确认删除权限“${row.name}”吗？`)
  await deleteMenuApi(row.menuId)
  showSuccess('权限已删除')
  await loadData()
}

onMounted(loadData)
</script>

<template>
  <el-card shadow="never" class="page-card">
    <template #header>
      <div class="card-header">
        <strong>权限管理</strong>
        <el-button v-if="isAuth('sys:menu:save')" type="primary" @click="openCreate">新增权限</el-button>
      </div>
    </template>
    <el-form :inline="true" class="search-grid" @submit.prevent>
      <el-form-item label="权限名称/标识">
        <el-input v-model="filters.keyword" clearable placeholder="名称或授权标识" @keyup.enter="handleSearch" />
      </el-form-item>
      <el-form-item label="所属菜单">
        <el-input v-model="filters.parentName" clearable placeholder="上级菜单名称" @keyup.enter="handleSearch" />
      </el-form-item>
      <el-form-item>
        <el-space>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-space>
      </el-form-item>
    </el-form>
    <FilterSummary :filters="filters" :fields="FILTER_FIELDS" />
    <el-table v-loading="loading" :data="tableData" border>
      <el-table-column prop="name" label="权限名称" min-width="140" />
      <el-table-column prop="parentName" label="所属菜单" min-width="160" />
      <el-table-column prop="perms" label="授权标识" min-width="220" show-overflow-tooltip />
      <el-table-column prop="orderNum" label="排序" width="80" />
      <el-table-column label="操作" fixed="right" min-width="150">
        <template #default="{ row }">
          <el-space>
            <el-button v-if="isAuth('sys:menu:update')" link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button v-if="isAuth('sys:menu:delete')" link type="danger" @click="handleDelete(row)">删除</el-button>
          </el-space>
        </template>
      </el-table-column>
    </el-table>
    <AppDialog
      v-model="dialogVisible"
      :title="editingId ? '编辑权限' : '新增权限'"
      :loading="dialogLoading"
      width="640px"
      @confirm="handleSubmit"
    >
      <div v-loading="detailLoading">
        <el-form ref="formRef" :model="form" :rules="rules" label-width="96px">
          <el-form-item label="权限名称" prop="name">
            <el-input v-model="form.name" placeholder="如：查看、新增" />
          </el-form-item>
          <el-form-item label="所属菜单" prop="parentId">
            <el-select v-model="form.parentId" placeholder="请选择所属菜单" filterable>
              <el-option v-for="item in parentOptions" :key="item.menuId" :label="item.name" :value="item.menuId" />
            </el-select>
          </el-form-item>
          <el-form-item label="授权标识" prop="perms">
            <el-input v-model="form.perms" placeholder="如 sys:user:list" />
          </el-form-item>
          <el-form-item label="排序">
            <el-input-number v-model="form.orderNum" :min="0" />
          </el-form-item>
        </el-form>
      </div>
    </AppDialog>
  </el-card>
</template>
