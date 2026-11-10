<script setup>
import { computed, nextTick, onMounted, reactive, ref } from 'vue'
import AppDialog from '@/components/common/AppDialog.vue'
import IconSvg from '@/components/icon-svg.vue'
import { createMenuApi, deleteMenuApi, getMenuInfoApi, getMenuListApi, getMenuSelectApi, updateMenuApi } from '@/api/modules/menus'
import { validateForm } from '@/utils/form'
import { confirmAction, showSuccess } from '@/utils/modal'
import { isAuth } from '@/utils/tool'

const loading = ref(false)
const dialogVisible = ref(false)
const dialogLoading = ref(false)
const detailLoading = ref(false)
const formRef = ref()
const tableData = ref([])
const treeSource = ref([])
const editingId = ref(0)

const typeOptions = [
  { label: '目录', value: 0, tagType: '' },
  { label: '菜单', value: 1, tagType: 'success' },
  { label: '按钮', value: 2, tagType: 'info' },
]

const initialForm = {
  menuId: 0,
  type: 1,
  name: '',
  parentId: 0,
  url: '',
  perms: '',
  orderNum: 0,
  icon: '',
}

const form = reactive({ ...initialForm })

const rules = {
  name: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  parentId: [{ required: true, message: '请选择上级菜单', trigger: 'change' }],
  url: [{ validator: validateMenuUrl, trigger: 'blur' }],
}

const treeSelectProps = {
  value: 'menuId',
  label: 'name',
  children: 'children',
  disabled: 'disabled',
}

const iconModules = import.meta.glob('/src/icons/svg/icon-*.svg', { eager: true, import: 'default' })
const iconOptions = Object.keys(iconModules)
  .map((path) => path.replace('/src/icons/svg/icon-', '').replace('.svg', ''))
  .sort((a, b) => a.localeCompare(b, 'zh-CN'))

const parentOptions = computed(() => {
  return [
    {
      menuId: 0,
      name: '顶级菜单',
      disabled: false,
      children: markDisabledNodes(cloneTree(treeSource.value), editingId.value),
    },
  ]
})

function validateMenuUrl(_rule, value, callback) {
  if (form.type === 1 && !String(value || '').trim()) {
    callback(new Error('请输入菜单 URL'))
    return
  }
  callback()
}

function getTypeMeta(type) {
  return typeOptions.find((item) => item.value === type) || typeOptions[0]
}

function cloneTree(list = []) {
  return list.map((item) => ({
    ...item,
    children: cloneTree(item.children || []),
  }))
}

function buildMenuTree(list = []) {
  const nodeMap = new Map()
  const roots = []

  list.forEach((item) => {
    nodeMap.set(item.menuId, {
      ...item,
      children: [],
    })
  })

  nodeMap.forEach((node) => {
    const parent = nodeMap.get(node.parentId)
    if (parent) {
      parent.children.push(node)
      return
    }
    roots.push(node)
  })

  const sorter = (items) => {
    items.sort((a, b) => {
      const orderDiff = Number(a.orderNum || 0) - Number(b.orderNum || 0)
      if (orderDiff !== 0) return orderDiff
      return Number(a.menuId || 0) - Number(b.menuId || 0)
    })
    items.forEach((item) => sorter(item.children || []))
    return items
  }

  return sorter(roots)
}

function collectDescendantIds(list = [], targetId, bucket = new Set()) {
  for (const item of list) {
    if (item.menuId === targetId) {
      walkChildren(item, bucket)
      break
    }
    collectDescendantIds(item.children || [], targetId, bucket)
  }
  return bucket
}

function walkChildren(node, bucket) {
  bucket.add(node.menuId)
  ;(node.children || []).forEach((child) => walkChildren(child, bucket))
}

function markDisabledNodes(list = [], currentId) {
  if (!currentId) {
    return list
  }
  const disabledIds = collectDescendantIds(list, currentId)
  const patcher = (nodes) => {
    nodes.forEach((node) => {
      node.disabled = disabledIds.has(node.menuId)
      patcher(node.children || [])
    })
    return nodes
  }
  return patcher(list)
}

function resetForm() {
  Object.assign(form, initialForm)
}

function buildSubmitPayload() {
  return {
    menuId: editingId.value || undefined,
    type: form.type,
    name: form.name.trim(),
    parentId: form.parentId,
    url: form.type === 1 ? form.url.trim() : '',
    perms: form.type !== 0 ? form.perms.trim() : '',
    orderNum: form.type !== 2 ? Number(form.orderNum || 0) : 0,
    icon: form.type !== 2 ? form.icon.trim() : '',
  }
}

async function loadData() {
  loading.value = true
  try {
    const list = await getMenuListApi()
    tableData.value = buildMenuTree(list)
  } finally {
    loading.value = false
  }
}

async function loadTreeSource() {
  const list = await getMenuSelectApi()
  treeSource.value = buildMenuTree(list)
}

async function openCreate(parentId = 0) {
  editingId.value = 0
  resetForm()
  form.parentId = parentId
  dialogVisible.value = true
  detailLoading.value = true
  try {
    await loadTreeSource()
    await nextTick()
    formRef.value?.clearValidate()
  } finally {
    detailLoading.value = false
  }
}

async function openEdit(row) {
  editingId.value = row.menuId
  resetForm()
  dialogVisible.value = true
  detailLoading.value = true
  try {
    const [tree, detail] = await Promise.all([getMenuSelectApi(), getMenuInfoApi(row.menuId)])
    treeSource.value = buildMenuTree(tree)
    Object.assign(form, {
      menuId: detail.menuId || 0,
      type: detail.type ?? 1,
      name: detail.name || '',
      parentId: detail.parentId ?? 0,
      url: detail.url || '',
      perms: detail.perms || '',
      orderNum: Number(detail.orderNum || 0),
      icon: detail.icon || '',
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
    const payload = buildSubmitPayload()
    if (editingId.value) {
      await updateMenuApi(payload)
      showSuccess('菜单已更新')
    } else {
      await createMenuApi(payload)
      showSuccess('菜单已新增')
    }
    dialogVisible.value = false
    await loadData()
  } finally {
    dialogLoading.value = false
  }
}

async function handleDelete(row) {
  await confirmAction(`确认删除菜单“${row.name}”吗？`)
  await deleteMenuApi(row.menuId)
  showSuccess('菜单已删除')
  await loadData()
}

function handleDialogClose(value) {
  dialogVisible.value = value
  if (!value) {
    editingId.value = 0
    resetForm()
  }
}

onMounted(loadData)
</script>

<template>
  <el-card shadow="never" class="page-card">
    <template #header>
      <div class="card-header">
        <strong>菜单管理</strong>
        <el-button v-if="isAuth('sys:menu:save')" type="primary" @click="openCreate()">新增菜单</el-button>
      </div>
    </template>

    <el-table v-loading="loading" :data="tableData" row-key="menuId" border>
      <el-table-column prop="name" label="名称" min-width="180" />
      <el-table-column prop="parentName" label="上级菜单" min-width="140" align="center" />
      <el-table-column label="图标" width="96" align="center">
        <template #default="{ row }">
          <IconSvg v-if="row.icon" :name="row.icon" />
        </template>
      </el-table-column>
      <el-table-column label="类型" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="getTypeMeta(row.type).tagType">{{ getTypeMeta(row.type).label }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="orderNum" label="排序号" width="100" align="center" />
      <el-table-column prop="url" label="菜单 URL" min-width="180" show-overflow-tooltip />
      <el-table-column prop="perms" label="授权标识" min-width="180" show-overflow-tooltip />
      <el-table-column label="操作" fixed="right" min-width="180" align="center">
        <template #default="{ row }">
          <el-space>
            <el-button v-if="isAuth('sys:menu:update')" link type="primary" @click="openEdit(row)">修改</el-button>
            <el-button v-if="isAuth('sys:menu:delete')" link type="danger" @click="handleDelete(row)">删除</el-button>
          </el-space>
        </template>
      </el-table-column>
    </el-table>

    <AppDialog
      :model-value="dialogVisible"
      :title="editingId ? '修改菜单' : '新增菜单'"
      :loading="dialogLoading"
      width="720px"
      @update:modelValue="handleDialogClose"
      @confirm="handleSubmit"
    >
      <div v-loading="detailLoading">
        <el-form ref="formRef" :model="form" :rules="rules" label-width="92px">
          <el-form-item label="类型" prop="type">
            <el-radio-group v-model="form.type">
              <el-radio v-for="item in typeOptions" :key="item.value" :value="item.value">{{ item.label }}</el-radio>
            </el-radio-group>
          </el-form-item>

          <el-form-item :label="`${getTypeMeta(form.type).label}名称`" prop="name">
            <el-input v-model="form.name" :placeholder="`请输入${getTypeMeta(form.type).label}名称`" maxlength="50" />
          </el-form-item>

          <el-form-item label="上级菜单" prop="parentId">
            <el-tree-select
              v-model="form.parentId"
              :data="parentOptions"
              :props="treeSelectProps"
              node-key="menuId"
              check-strictly
              default-expand-all
              class="menu-tree-select"
              placeholder="请选择上级菜单"
            />
          </el-form-item>

          <el-form-item v-if="form.type === 1" label="菜单路由" prop="url">
            <el-input v-model="form.url" placeholder="请输入菜单路由，例如 sys/menu" maxlength="120" />
          </el-form-item>

          <el-form-item v-if="form.type !== 0" label="授权标识" prop="perms">
            <el-input v-model="form.perms" placeholder="多个标识用逗号分隔" maxlength="200" />
          </el-form-item>

          <el-form-item v-if="form.type !== 2" label="排序号" prop="orderNum">
            <el-input-number v-model="form.orderNum" :min="0" controls-position="right" />
          </el-form-item>

          <el-form-item v-if="form.type !== 2" label="菜单图标" prop="icon">
            <el-popover placement="bottom-start" trigger="click" :width="460">
              <template #reference>
                <el-input v-model="form.icon" readonly placeholder="请选择菜单图标">
                  <template #prefix>
                    <IconSvg v-if="form.icon" :name="form.icon" />
                  </template>
                </el-input>
              </template>
              <el-scrollbar max-height="280px">
                <div class="menu-icon-grid">
                  <button
                    v-for="item in iconOptions"
                    :key="item"
                    type="button"
                    class="menu-icon-item"
                    :class="{ 'is-active': item === form.icon }"
                    @click="form.icon = item"
                  >
                    <IconSvg :name="item" />
                    <span>{{ item }}</span>
                  </button>
                </div>
              </el-scrollbar>
            </el-popover>
          </el-form-item>
        </el-form>
      </div>
    </AppDialog>
  </el-card>
</template>

<style scoped>
.menu-tree-select {
  width: 100%;
}

.menu-icon-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 10px;
}

.menu-icon-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  min-height: 74px;
  padding: 10px 8px;
  border: 1px solid var(--el-border-color);
  border-radius: 14px;
  background: #fff;
  color: var(--el-text-color-regular);
  cursor: pointer;
  transition: all 0.2s ease;
}

.menu-icon-item span {
  max-width: 100%;
  font-size: 12px;
  line-height: 1.2;
  word-break: break-all;
}

.menu-icon-item:hover,
.menu-icon-item.is-active {
  border-color: var(--el-color-primary);
  color: var(--el-color-primary);
  box-shadow: 0 8px 18px rgb(64 158 255 / 0.12);
}
</style>
