<script setup>
import { computed, onMounted, reactive, ref, toRef, watch } from 'vue'
// toRef used by useSetupStageCascade bindings
import { RouterLink } from 'vue-router'
import AppDialog from '@/components/common/AppDialog.vue'
import BasePagination from '@/components/common/BasePagination.vue'
import TableRowActions from '@/components/common/TableRowActions.vue'
import TableStateSwitch from '@/components/common/TableStateSwitch.vue'
import {
  loadChapterSetupOptions,
  questionChapterApi,
  questionSectionApi,
  searchChapterOptions,
  updateQuestionChapterStateApi,
  updateQuestionSectionStateApi,
} from '@/api/modules/chapter-tree'
import {
  CHAPTER_DEFAULT_FORM,
  CHAPTER_STATE_OPTIONS,
  CHAPTER_TREE_TABS,
  SECTION_DEFAULT_FORM,
  getChapterStateLabel,
} from '@/constants/chapter-tree'
import FilterSelect from '@/components/common/FilterSelect.vue'
import FilterSummary from '@/components/common/FilterSummary.vue'
import { useFilterFormKey } from '@/composables/useFilterFormKey'
import { useSetupStageCascade } from '@/composables/useSetupStageCascade'
import {
  FILTER_SELECT_EMPTY,
  isFilterEmpty,
  normalizeNumberFilterQuery,
  normalizeSelectFilterQuery,
} from '@/utils/filter-form'
import { EMPTY_SETUP_OPTIONS } from '@/utils/setup-options'
import { patchFormModel, resetFormModel, validateForm } from '@/utils/form'
import { confirmAction, showError, showSuccess } from '@/utils/modal'

const activeTab = ref('chapter')
const loading = ref(false)
const dialogVisible = ref(false)
const dialogLoading = ref(false)
const formRef = ref()
const editingId = ref(null)
const stateUpdatingId = ref(null)
const { filterFormKey, bumpFilterFormKey } = useFilterFormKey()
const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const setupOptions = ref({ ...EMPTY_SETUP_OPTIONS })
const chapterOptions = ref([])
const chapterOptionsLoading = ref(false)

const sharedFilters = reactive({
  grade: FILTER_SELECT_EMPTY,
  subject: FILTER_SELECT_EMPTY,
  version: FILTER_SELECT_EMPTY,
})

const chapterFilters = reactive({
  title: '',
  state: FILTER_SELECT_EMPTY,
})

const sectionFilters = reactive({
  title: '',
  chapterTitle: '',
  state: FILTER_SELECT_EMPTY,
})

const sharedFilterFields = computed(() => [
  { key: 'stageCode', label: '学段', type: 'select', options: filterGradeStages.value, placeholder: '全部学段' },
  { key: 'grade', label: '年级', type: 'select', options: filterGrades.value, placeholder: '全部年级' },
  { key: 'subject', label: '学科', type: 'select', options: filterSubjects.value, placeholder: '全部学科' },
  { key: 'version', label: '教材版本', type: 'select', options: setupOptions.value.versions, placeholder: '全部版本' },
])

const chapterFilterFields = computed(() => [
  ...sharedFilterFields.value,
  { key: 'title', label: '章标题', type: 'text', placeholder: '章标题' },
  { key: 'state', label: '状态', type: 'select', options: CHAPTER_STATE_OPTIONS, placeholder: '全部状态' },
])

const sectionFilterFields = computed(() => [
  ...sharedFilterFields.value,
  { key: 'title', label: '节标题', type: 'text', placeholder: '节标题' },
  { key: 'chapterTitle', label: '所属章标题', type: 'text', placeholder: '章标题' },
  { key: 'state', label: '状态', type: 'select', options: CHAPTER_STATE_OPTIONS, placeholder: '全部状态' },
])

const currentFilterFields = computed(() => (
  isChapterTab.value ? chapterFilterFields.value : sectionFilterFields.value
))

const currentFilters = computed(() => (
  isChapterTab.value
    ? { ...filterSummaryFilters.value, ...chapterFilters }
    : { ...filterSummaryFilters.value, ...sectionFilters }
))

const chapterForm = reactive({ ...CHAPTER_DEFAULT_FORM })
const sectionForm = reactive({ ...SECTION_DEFAULT_FORM })

const {
  stageCode: filterStageCode,
  gradeStages: filterGradeStages,
  filteredGrades: filterGrades,
  filteredSubjects: filterSubjects,
  handleStageChange: handleFilterStageChange,
  resetStage: resetFilterStage,
} = useSetupStageCascade(setupOptions, {
  gradeRef: toRef(sharedFilters, 'grade'),
  subjectRef: toRef(sharedFilters, 'subject'),
  filterMode: true,
})

const {
  stageCode: formStageCode,
  gradeStages: formGradeStages,
  filteredGrades: formGrades,
  filteredSubjects: formSubjects,
  handleStageChange: handleFormStageChange,
  syncStageFromGrade: syncFormStageFromGrade,
  resetStage: resetFormStage,
} = useSetupStageCascade(setupOptions, {
  gradeRef: toRef(chapterForm, 'grade'),
  subjectRef: toRef(chapterForm, 'subject'),
  filterMode: false,
})

const isChapterTab = computed(() => activeTab.value === 'chapter')

const filterSummaryFilters = computed(() => ({
  ...sharedFilters,
  stageCode: filterStageCode.value,
}))

const chapterRules = {
  title: [{ required: true, message: '请输入章标题', trigger: 'blur' }],
  grade: [{ required: true, message: '请选择年级', trigger: 'change' }],
  subject: [{ required: true, message: '请选择学科', trigger: 'change' }],
  version: [{ required: true, message: '请选择教材版本', trigger: 'change' }],
  sortOrder: [{ required: true, message: '请输入排序', trigger: 'change' }],
  state: [{ required: true, message: '请选择状态', trigger: 'change' }],
}

const sectionRules = {
  chapterId: [{ required: true, message: '请选择所属章', trigger: 'change' }],
  title: [{ required: true, message: '请输入节标题', trigger: 'blur' }],
  sortOrder: [{ required: true, message: '请输入排序', trigger: 'change' }],
  state: [{ required: true, message: '请选择状态', trigger: 'change' }],
}

const currentRules = computed(() => (isChapterTab.value ? chapterRules : sectionRules))
const currentForm = computed(() => (isChapterTab.value ? chapterForm : sectionForm))
const dialogTitle = computed(() => {
  const entity = isChapterTab.value ? '章' : '节'
  return editingId.value ? `编辑${entity}` : `新增${entity}`
})

function buildChapterQuery() {
  return {
    page: pageNum.value,
    limit: pageSize.value,
    grade: normalizeSelectFilterQuery(sharedFilters.grade),
    subject: normalizeSelectFilterQuery(sharedFilters.subject),
    version: normalizeSelectFilterQuery(sharedFilters.version),
    title: chapterFilters.title || undefined,
    state: normalizeNumberFilterQuery(chapterFilters.state),
  }
}

function buildSectionQuery() {
  return {
    page: pageNum.value,
    limit: pageSize.value,
    grade: normalizeSelectFilterQuery(sharedFilters.grade),
    subject: normalizeSelectFilterQuery(sharedFilters.subject),
    version: normalizeSelectFilterQuery(sharedFilters.version),
    title: sectionFilters.title || undefined,
    chapterTitle: sectionFilters.chapterTitle || undefined,
    state: normalizeNumberFilterQuery(sectionFilters.state),
  }
}

async function loadSetupOptions() {
  try {
    setupOptions.value = await loadChapterSetupOptions()
  } catch (error) {
    showError(error?.msg || error?.message || '加载年级学科版本选项失败')
  }
}

async function loadChapterOptions(keyword = '') {
  chapterOptionsLoading.value = true
  try {
    chapterOptions.value = await searchChapterOptions({
      grade: sharedFilters.grade,
      subject: sharedFilters.subject,
      version: sharedFilters.version,
      keyword,
      valueKey: 'id',
    })
  } catch (error) {
    chapterOptions.value = []
    showError(error?.msg || error?.message || '加载章列表失败')
  } finally {
    chapterOptionsLoading.value = false
  }
}

function ensureChapterOption(chapterId, chapterTitle) {
  if (!chapterId) return
  if (!chapterOptions.value.some((item) => item.value === chapterId)) {
    chapterOptions.value = [
      { label: chapterTitle || `章 #${chapterId}`, value: chapterId, id: chapterId },
      ...chapterOptions.value,
    ]
  }
}

async function loadData() {
  loading.value = true
  try {
    const data = isChapterTab.value
      ? await questionChapterApi.page(buildChapterQuery())
      : await questionSectionApi.page(buildSectionQuery())
    tableData.value = data.list
    total.value = data.total
  } catch (error) {
    showError(error?.msg || error?.message || '加载列表失败')
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
  resetFilterStage()
  Object.assign(sharedFilters, {
    grade: FILTER_SELECT_EMPTY,
    subject: FILTER_SELECT_EMPTY,
    version: FILTER_SELECT_EMPTY,
  })
  if (isChapterTab.value) {
    Object.assign(chapterFilters, { title: '', state: FILTER_SELECT_EMPTY })
  } else {
    Object.assign(sectionFilters, {
      title: '',
      chapterTitle: '',
      state: FILTER_SELECT_EMPTY,
    })
  }
  bumpFilterFormKey()
  pageNum.value = 1
  loadData()
}

function openCreate() {
  editingId.value = null
  if (isChapterTab.value) {
    resetFormModel(chapterForm, {
      ...CHAPTER_DEFAULT_FORM,
      grade: sharedFilters.grade || '',
      subject: sharedFilters.subject || '',
      version: sharedFilters.version || '',
    })
    if (chapterForm.grade) {
      syncFormStageFromGrade(chapterForm.grade)
    } else if (!isFilterEmpty(filterStageCode.value)) {
      formStageCode.value = filterStageCode.value
    } else {
      resetFormStage()
    }
  } else {
    resetFormModel(sectionForm, { ...SECTION_DEFAULT_FORM })
    loadChapterOptions()
  }
  dialogVisible.value = true
}

async function openEdit(row) {
  editingId.value = row.id
  dialogVisible.value = true
  dialogLoading.value = true
  try {
    if (isChapterTab.value) {
      const data = await questionChapterApi.info(row.id)
      patchFormModel(chapterForm, data || row, CHAPTER_DEFAULT_FORM)
      syncFormStageFromGrade(chapterForm.grade)
    } else {
      const data = await questionSectionApi.info(row.id)
      patchFormModel(sectionForm, data || row, SECTION_DEFAULT_FORM)
      await loadChapterOptions()
      ensureChapterOption(data?.chapterId || row.chapterId, data?.chapterTitle || row.chapterTitle)
    }
  } catch (error) {
    showError(error?.msg || error?.message || '加载详情失败')
    dialogVisible.value = false
  } finally {
    dialogLoading.value = false
  }
}

function handleGradeChange(gradeValue) {
  syncFormStageFromGrade(gradeValue)
  const matched = setupOptions.value.grades.find((item) => item.value === gradeValue)
  if (matched?.code) {
    chapterForm.gradeShort = matched.code
  }
}

async function handleSubmit() {
  const valid = await validateForm(formRef)
  if (!valid) return
  dialogLoading.value = true
  try {
    if (isChapterTab.value) {
      const payload = { ...chapterForm }
      if (editingId.value) {
        await questionChapterApi.update({ ...payload, id: editingId.value })
        showSuccess('章已更新')
      } else {
        await questionChapterApi.save(payload)
        showSuccess('章已新增')
      }
    } else {
      const payload = { ...sectionForm }
      if (editingId.value) {
        await questionSectionApi.update({ ...payload, id: editingId.value })
        showSuccess('节已更新')
      } else {
        await questionSectionApi.save(payload)
        showSuccess('节已新增')
      }
    }
    dialogVisible.value = false
    loadData()
  } catch (error) {
    showError(error?.msg || error?.message || '保存失败')
  } finally {
    dialogLoading.value = false
  }
}

async function handleDelete(row) {
  const label = isChapterTab.value ? row.title : row.title
  await confirmAction(`确认删除「${label}」吗？`)
  try {
    if (isChapterTab.value) {
      await questionChapterApi.delete({ id: row.id })
    } else {
      await questionSectionApi.delete({ id: row.id })
    }
    showSuccess('删除成功')
    loadData()
  } catch (error) {
    showError(error?.msg || error?.message || '删除失败')
  }
}

async function handleStateChange(row, nextState) {
  const previousState = row.state
  if (nextState === previousState) {
    return
  }
  const actionLabel = nextState === 1 ? '启用' : '禁用'
  stateUpdatingId.value = row.id
  try {
    if (isChapterTab.value) {
      await updateQuestionChapterStateApi({ id: row.id, state: nextState })
    } else {
      await updateQuestionSectionStateApi({ id: row.id, state: nextState })
    }
    row.state = nextState
    showSuccess(`${actionLabel}成功`)
  } catch (error) {
    row.state = previousState
    showError(error?.msg || error?.message || `${actionLabel}失败`)
  } finally {
    stateUpdatingId.value = null
  }
}

watch(activeTab, () => {
  pageNum.value = 1
  loadData()
})

onMounted(async () => {
  await loadSetupOptions()
  loadData()
})
</script>

<template>
  <el-card shadow="never" class="page-card">
    <template #header>
      <div class="card-header">
        <div>
          <strong>章节题库管理</strong>
          <p class="header-desc">
            维护章/节树结构，供移动端按年级、学科、版本展示。
            年级/学科/版本选项请在
            <RouterLink to="/setup-options">年级学科配置</RouterLink>
            中维护。
          </p>
        </div>
        <el-button type="primary" @click="openCreate">
          {{ isChapterTab ? '新增章' : '新增节' }}
        </el-button>
      </div>
    </template>

    <el-tabs v-model="activeTab">
      <el-tab-pane
        v-for="tab in CHAPTER_TREE_TABS"
        :key="tab.key"
        :label="tab.label"
        :name="tab.key"
      />
    </el-tabs>

    <el-form :key="filterFormKey" :inline="true" class="search-grid">
      <el-form-item label="学段">
        <FilterSelect
          :model-value="filterStageCode"
          clearable
          filterable
          placeholder="全部学段"
          @update:model-value="handleFilterStageChange"
        >
          <el-option
            v-for="item in filterGradeStages"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </FilterSelect>
      </el-form-item>
      <el-form-item label="年级">
        <FilterSelect v-model="sharedFilters.grade" clearable filterable placeholder="全部年级">
          <el-option
            v-for="item in filterGrades"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </FilterSelect>
      </el-form-item>
      <el-form-item label="学科">
        <FilterSelect v-model="sharedFilters.subject" clearable filterable placeholder="全部学科">
          <el-option
            v-for="item in filterSubjects"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </FilterSelect>
      </el-form-item>
      <el-form-item label="教材版本">
        <FilterSelect v-model="sharedFilters.version" clearable filterable placeholder="全部版本">
          <el-option
            v-for="item in setupOptions.versions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </FilterSelect>
      </el-form-item>

      <template v-if="isChapterTab">
        <el-form-item label="章标题">
          <el-input v-model="chapterFilters.title" clearable placeholder="章标题" />
        </el-form-item>
      </template>

      <template v-else>
        <el-form-item label="节标题">
          <el-input v-model="sectionFilters.title" clearable placeholder="节标题" />
        </el-form-item>
        <el-form-item label="所属章标题">
          <el-input v-model="sectionFilters.chapterTitle" clearable placeholder="章标题" />
        </el-form-item>
      </template>

      <el-form-item label="状态">
        <FilterSelect
          :model-value="isChapterTab ? chapterFilters.state : sectionFilters.state"
          clearable
          placeholder="全部状态"
          @update:model-value="isChapterTab ? (chapterFilters.state = $event) : (sectionFilters.state = $event)"
        >
          <el-option
            v-for="item in CHAPTER_STATE_OPTIONS"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </FilterSelect>
      </el-form-item>

      <el-form-item>
        <el-space>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-space>
      </el-form-item>
    </el-form>

    <FilterSummary :filters="currentFilters" :fields="currentFilterFields" />

    <el-table v-loading="loading" :data="tableData" border empty-text="暂无数据">
      <template v-if="isChapterTab">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="章标题" min-width="160" />
        <el-table-column prop="grade" label="年级" min-width="120" />
        <el-table-column prop="gradeShort" label="年级简称" width="100" />
        <el-table-column prop="subject" label="学科" width="90" />
        <el-table-column prop="version" label="教材版本" width="100" />
        <el-table-column prop="questionCount" label="题目数" width="90" />
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
      </template>

      <template v-else>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="节标题" min-width="160" />
        <el-table-column prop="chapterTitle" label="所属章" min-width="160" />
        <el-table-column prop="questionCount" label="题目数" width="90" />
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
      </template>

      <el-table-column label="操作" min-width="160" fixed="right">
        <template #default="{ row }">
          <TableRowActions>
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
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
      :title="dialogTitle"
      :loading="dialogLoading"
      @confirm="handleSubmit"
    >
      <el-form ref="formRef" :model="currentForm" :rules="currentRules" label-width="120px">
        <template v-if="isChapterTab">
          <el-form-item label="章标题" prop="title">
            <el-input v-model="chapterForm.title" placeholder="如 第一章：有理数" />
          </el-form-item>
          <el-form-item label="学段">
            <el-select
              :model-value="formStageCode"
              filterable
              placeholder="选择学段"
              @update:model-value="handleFormStageChange"
            >
              <el-option
                v-for="item in formGradeStages"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="年级" prop="grade">
            <el-select
              v-model="chapterForm.grade"
              filterable
              placeholder="选择年级"
              @change="handleGradeChange"
            >
              <el-option
                v-for="item in formGrades"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="年级简称">
            <el-input v-model="chapterForm.gradeShort" placeholder="如 初三（C 端 gradeShort）" />
          </el-form-item>
          <el-form-item label="学科" prop="subject">
            <el-select v-model="chapterForm.subject" filterable placeholder="选择学科">
              <el-option
                v-for="item in formSubjects"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="教材版本" prop="version">
            <el-select v-model="chapterForm.version" filterable placeholder="选择版本">
              <el-option
                v-for="item in setupOptions.versions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="题目数量">
            <el-input-number v-model="chapterForm.questionCount" :min="0" />
          </el-form-item>
          <el-form-item label="排序" prop="sortOrder">
            <el-input-number v-model="chapterForm.sortOrder" :min="0" />
          </el-form-item>
          <el-form-item label="状态" prop="state">
            <el-radio-group v-model="chapterForm.state">
              <el-radio v-for="item in CHAPTER_STATE_OPTIONS" :key="item.value" :value="item.value">
                {{ item.label }}
              </el-radio>
            </el-radio-group>
          </el-form-item>
        </template>

        <template v-else>
          <el-form-item label="所属章" prop="chapterId">
            <el-select
              v-model="sectionForm.chapterId"
              filterable
              remote
              reserve-keyword
              :remote-method="loadChapterOptions"
              :loading="chapterOptionsLoading"
              placeholder="按章标题搜索"
            >
              <el-option
                v-for="item in chapterOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="节标题" prop="title">
            <el-input v-model="sectionForm.title" placeholder="如 1.1 正数和负数" />
          </el-form-item>
          <el-form-item label="题目数量">
            <el-input-number v-model="sectionForm.questionCount" :min="0" />
          </el-form-item>
          <el-form-item label="排序" prop="sortOrder">
            <el-input-number v-model="sectionForm.sortOrder" :min="0" />
          </el-form-item>
          <el-form-item label="状态" prop="state">
            <el-radio-group v-model="sectionForm.state">
              <el-radio v-for="item in CHAPTER_STATE_OPTIONS" :key="item.value" :value="item.value">
                {{ item.label }}
              </el-radio>
            </el-radio-group>
          </el-form-item>
        </template>
      </el-form>
    </AppDialog>
  </el-card>
</template>

<style scoped>
.header-desc {
  margin: 4px 0 0;
  font-size: 13px;
  color: var(--el-text-color-secondary);
  font-weight: normal;
}

.header-desc a {
  color: var(--el-color-primary);
}
</style>
