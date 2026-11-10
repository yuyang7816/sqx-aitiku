<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import AppDialog from '@/components/common/AppDialog.vue'
import BasePagination from '@/components/common/BasePagination.vue'
import FilterSummary from '@/components/common/FilterSummary.vue'
import TableRowActions from '@/components/common/TableRowActions.vue'
import TableStateSwitch from '@/components/common/TableStateSwitch.vue'
import {
  createSetupDictApi,
  deleteSetupDictApi,
  getSetupDictOptions,
  getSetupDictPage,
  importDefaultSetupDictItems,
  updateSetupDictApi,
} from '@/api/modules/setup-dict'
import {
  DEFAULT_SETUP_DICT_ITEMS,
  SETUP_DICT_STATUS,
  SETUP_DICT_TYPE,
  SETUP_DICT_TABS,
  GRADE_STAGE_OPTIONS,
  buildGradeStageSelectOptions,
  filterGarbledDictRows,
  formToSetupDictPayload,
  getSetupDictTab,
  isValidDisplayText,
  resolveGradeStageLabel,
  resolveGradeShortLabel,
  resolveSubjectStageCodes,
  formatSubjectStageLabels,
  defaultStagesForSubject,
  rowToSetupDictForm,
  suggestGradeFullName,
} from '@/constants/setup-dict'
import { patchFormModel, resetFormModel, validateForm } from '@/utils/form'
import { confirmAction, showError, showSuccess } from '@/utils/modal'
import { isAuth } from '@/utils/tool'
import { useSystemConfigEmbedded } from '@/composables/useSystemConfigEmbedded'

const embedded = useSystemConfigEmbedded()
const activeTab = ref('gradeStage')
const loading = ref(false)
const importLoading = ref(false)
const dialogVisible = ref(false)
const dialogLoading = ref(false)
const formRef = ref()
const editingId = ref(null)
const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const stateUpdatingId = ref(null)
const gradeStageRows = ref([])
const filters = reactive({ keyword: '' })

const currentTab = computed(() => getSetupDictTab(activeTab.value))

const FILTER_FIELDS = computed(() => [
  { key: 'keyword', label: currentTab.value.searchLabel, type: 'text', placeholder: currentTab.value.searchPlaceholder },
])
const initialForm = computed(() => ({ ...currentTab.value.defaultForm }))
const isGradeTab = computed(() => currentTab.value.key === 'grade')
const isGradeStageTab = computed(() => currentTab.value.key === 'gradeStage')
const isSubjectTab = computed(() => currentTab.value.key === 'subject')
const gradeStageSelectOptions = computed(() => buildGradeStageSelectOptions(gradeStageRows.value))

const canManage = computed(() => (
  isAuth('sys:dict:list')
  || isAuth('sys:dict:save')
  || isAuth('sys:dict:update')
  || isAuth('sys:dict:delete')
))

const rules = computed(() => {
  const rejectGarbled = {
    validator: (_rule, value, callback) => {
      if (!isValidDisplayText(value)) {
        callback(new Error('内容不能为乱码或仅包含问号'))
        return
      }
      callback()
    },
    trigger: 'blur',
  }
  if (isGradeTab.value) {
    return {
      stageCode: [{ required: true, message: '请选择所属学段', trigger: 'change' }],
      shortLabel: [
        { required: true, message: '请输入年级', trigger: 'blur' },
        rejectGarbled,
      ],
      fullName: [
        { required: true, message: '请输入年级全称', trigger: 'blur' },
        rejectGarbled,
      ],
    }
  }
  if (isSubjectTab.value) {
    return {
      displayName: [
        { required: true, message: `请输入${currentTab.value.searchLabel}`, trigger: 'blur' },
        rejectGarbled,
      ],
      stageCodes: [{
        type: 'array',
        required: true,
        min: 1,
        message: '请至少选择一个适用学段',
        trigger: 'change',
      }],
    }
  }
  return {
    displayName: [
      { required: true, message: `请输入${currentTab.value.searchLabel}`, trigger: 'blur' },
      rejectGarbled,
    ],
  }
})

const form = reactive({ ...initialForm.value })

function resolveStageDisplay(stageCode = '') {
  const hit = gradeStageRows.value.find((row) => row.code === stageCode)
  return hit?.name || resolveGradeStageLabel(stageCode)
}

async function loadGradeStageOptions() {
  try {
    gradeStageRows.value = await getSetupDictOptions(SETUP_DICT_TYPE.GRADE_STAGE)
  } catch {
    gradeStageRows.value = []
  }
}

async function loadData() {
  loading.value = true
  try {
    const data = await getSetupDictPage({
      type: currentTab.value.type,
      page: pageNum.value,
      limit: pageSize.value,
      name: filters.keyword,
    })
    tableData.value = data.list
    total.value = data.total
    if (isGradeStageTab.value) {
      gradeStageRows.value = data.list
    }
  } catch (error) {
    showError(error?.msg || error?.message || '加载失败')
    tableData.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pageNum.value = 1
  loadData()
}

function handleReset() {
  filters.keyword = ''
  pageNum.value = 1
  loadData()
}

function openCreate() {
  editingId.value = null
  resetFormModel(form, initialForm.value)
  dialogVisible.value = true
}

function openEdit(row) {
  editingId.value = row.id
  patchFormModel(form, rowToSetupDictForm(activeTab.value, row), initialForm.value)
  dialogVisible.value = true
}

function syncGradeFullName() {
  if (!isGradeTab.value) return
  const suggested = suggestGradeFullName(form.stageCode, form.shortLabel)
  if (!suggested) return
  if (!form.fullName || form.fullName === suggestGradeFullName(form.stageCode, '')) {
    form.fullName = suggested
  }
}

async function handleSubmit() {
  const valid = await validateForm(formRef)
  if (!valid) return
  dialogLoading.value = true
  try {
    const payload = {
      ...formToSetupDictPayload(activeTab.value, form),
      type: currentTab.value.type,
    }
    if (editingId.value) {
      await updateSetupDictApi({ ...payload, id: editingId.value })
      showSuccess('保存成功')
    } else {
      await createSetupDictApi(payload)
      showSuccess('新增成功')
    }
    dialogVisible.value = false
    await loadData()
    if (!isGradeStageTab.value) {
      await loadGradeStageOptions()
    }
  } catch (error) {
    showError(error?.msg || error?.message || '保存失败')
  } finally {
    dialogLoading.value = false
  }
}

async function handleDelete(row) {
  const label = row.name || row.code || '该项'
  await confirmAction(`确认删除「${label}」吗？`)
  try {
    await deleteSetupDictApi([row.id])
    showSuccess('删除成功')
    await loadData()
    if (!isGradeStageTab.value) {
      await loadGradeStageOptions()
    }
  } catch (error) {
    showError(error?.msg || error?.message || '删除失败')
  }
}

async function handleStatusChange(row, nextStatus) {
  stateUpdatingId.value = row.id
  try {
    await updateSetupDictApi({
      id: row.id,
      type: row.type,
      name: row.name,
      code: row.code,
      value: row.value,
      orderNum: row.orderNum ?? 0,
      remark: row.remark || '',
      status: nextStatus,
    })
    row.status = nextStatus
    showSuccess(nextStatus === SETUP_DICT_STATUS.VISIBLE ? '已设为显示' : '已设为隐藏')
  } catch (error) {
    showError(error?.msg || error?.message || '状态更新失败')
  } finally {
    stateUpdatingId.value = null
  }
}

async function handleImportDefaults() {
  await confirmAction('确定导入推荐选项吗？已有内容会自动跳过。')
  importLoading.value = true
  try {
    const items = DEFAULT_SETUP_DICT_ITEMS[currentTab.value.type] || []
    const created = await importDefaultSetupDictItems(currentTab.value.type, items)
    showSuccess(created > 0 ? `已导入 ${created} 条` : '推荐选项已存在')
    await loadData()
    if (!isGradeStageTab.value) {
      await loadGradeStageOptions()
    }
  } catch (error) {
    showError(error?.msg || error?.message || '导入失败')
  } finally {
    importLoading.value = false
  }
}

watch(activeTab, () => {
  pageNum.value = 1
  filters.keyword = ''
  loadData()
})

watch(
  () => form.displayName,
  (name) => {
    if (!dialogVisible.value || !isSubjectTab.value || editingId.value) {
      return
    }
    form.stageCodes = [...defaultStagesForSubject(name)]
  }
)

watch(
  () => [form.stageCode, form.shortLabel],
  () => {
    if (dialogVisible.value && isGradeTab.value && !editingId.value) {
      syncGradeFullName()
    }
  }
)

onMounted(async () => {
  await loadGradeStageOptions()
  await loadData()
})
</script>

<template>
  <el-card shadow="never" class="page-card">
    <template #header>
      <div class="card-header">
        <strong v-if="!embedded">年级学科配置</strong>
        <el-space>
          <el-button
            v-if="canManage"
            :loading="importLoading"
            @click="handleImportDefaults"
          >
            导入推荐选项
          </el-button>
          <el-button v-if="canManage" type="primary" @click="openCreate">
            {{ currentTab.createLabel }}
          </el-button>
        </el-space>
      </div>
    </template>

    <el-tabs v-model="activeTab">
      <el-tab-pane
        v-for="tab in SETUP_DICT_TABS"
        :key="tab.key"
        :label="tab.label"
        :name="tab.key"
      />
    </el-tabs>

    <el-form :inline="true" class="search-grid">
      <el-form-item :label="currentTab.searchLabel">
        <el-input
          v-model="filters.keyword"
          clearable
          :placeholder="currentTab.searchPlaceholder"
          @keyup.enter="handleSearch"
        />
      </el-form-item>
      <el-form-item>
        <el-space>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-space>
      </el-form-item>
    </el-form>
    <FilterSummary :filters="filters" :fields="FILTER_FIELDS" />

    <el-table
      v-loading="loading"
      :data="tableData"
      border
      :empty-text="currentTab.emptyText"
    >
      <el-table-column prop="orderNum" label="排序" width="72" align="center" />

      <template v-if="isGradeStageTab">
        <el-table-column prop="name" label="学段名称" min-width="200" />
      </template>

      <template v-else-if="isGradeTab">
        <el-table-column label="所属学段" min-width="120">
          <template #default="{ row }">
            {{ resolveStageDisplay(row.remark) }}
          </template>
        </el-table-column>
        <el-table-column label="年级" min-width="120">
          <template #default="{ row }">
            {{ resolveGradeShortLabel(row.code, row.name) }}
          </template>
        </el-table-column>
        <el-table-column prop="name" label="年级全称" min-width="180" />
      </template>

      <template v-else-if="isSubjectTab">
        <el-table-column prop="name" :label="currentTab.searchLabel" min-width="160" />
        <el-table-column label="适用学段" min-width="220">
          <template #default="{ row }">
            {{ formatSubjectStageLabels(resolveSubjectStageCodes(row.name, row.remark)) }}
          </template>
        </el-table-column>
      </template>

      <template v-else>
        <el-table-column prop="name" :label="currentTab.searchLabel" min-width="200" />
      </template>

      <el-table-column label="显示" width="110" align="center">
        <template #default="{ row }">
          <TableStateSwitch
            :model-value="row.status ?? SETUP_DICT_STATUS.VISIBLE"
            :active-value="SETUP_DICT_STATUS.VISIBLE"
            :inactive-value="SETUP_DICT_STATUS.HIDDEN"
            active-text="显示"
            inactive-text="隐藏"
            :loading="stateUpdatingId === row.id"
            :disabled="!canManage"
            @change="(nextStatus) => handleStatusChange(row, nextStatus)"
          />
        </template>
      </el-table-column>

      <el-table-column label="操作" min-width="150" fixed="right">
        <template #default="{ row }">
          <TableRowActions v-if="canManage">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </TableRowActions>
          <span v-else class="text-muted">-</span>
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
      :title="editingId ? `编辑${currentTab.label}` : currentTab.createLabel"
      :loading="dialogLoading"
      @confirm="handleSubmit"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <template v-if="isGradeStageTab">
          <el-form-item label="学段名称" prop="displayName">
            <el-input
              v-model="form.displayName"
              placeholder="如：小学、初中、高中"
            />
          </el-form-item>
        </template>

        <template v-else-if="isGradeTab">
          <el-form-item label="所属学段" prop="stageCode">
            <el-select v-model="form.stageCode" placeholder="请选择" style="width: 100%">
              <el-option
                v-for="item in gradeStageSelectOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="年级" prop="shortLabel">
            <el-input
              v-model="form.shortLabel"
              placeholder="如：初一、二年级"
              @blur="syncGradeFullName"
            />
          </el-form-item>
          <el-form-item label="年级全称" prop="fullName">
            <el-input v-model="form.fullName" placeholder="如：初中一年级" />
          </el-form-item>
        </template>

        <template v-else-if="isSubjectTab">
          <el-form-item :label="currentTab.searchLabel" prop="displayName">
            <el-input
              v-model="form.displayName"
              :placeholder="currentTab.searchPlaceholder"
            />
          </el-form-item>
          <el-form-item label="适用学段" prop="stageCodes">
            <el-checkbox-group v-model="form.stageCodes">
              <el-checkbox
                v-for="item in GRADE_STAGE_OPTIONS"
                :key="item.value"
                :label="item.value"
              >
                {{ item.label }}
              </el-checkbox>
            </el-checkbox-group>
          </el-form-item>
        </template>

        <template v-else>
          <el-form-item :label="currentTab.searchLabel" prop="displayName">
            <el-input
              v-model="form.displayName"
              :placeholder="currentTab.searchPlaceholder"
            />
          </el-form-item>
        </template>

        <el-form-item label="排序">
          <el-input-number v-model="form.orderNum" :min="0" />
        </el-form-item>

        <el-form-item label="显示状态">
          <TableStateSwitch
            v-model="form.status"
            :active-value="SETUP_DICT_STATUS.VISIBLE"
            :inactive-value="SETUP_DICT_STATUS.HIDDEN"
            active-text="显示"
            inactive-text="隐藏"
          />
        </el-form-item>
      </el-form>
    </AppDialog>
  </el-card>
</template>
