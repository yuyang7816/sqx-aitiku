<script setup>
import { computed, onMounted, reactive, ref, toRef } from 'vue'
import AppDialog from '@/components/common/AppDialog.vue'
import BasePagination from '@/components/common/BasePagination.vue'
import TableRowActions from '@/components/common/TableRowActions.vue'
import TableStateSwitch from '@/components/common/TableStateSwitch.vue'
import {
  bindPaperQuestionsApi,
  loadPaperSetupOptions,
  pagePaperItemsApi,
  questionPaperApi,
  searchPaperQuestionsApi,
  unbindPaperQuestionApi,
  updateQuestionPaperStateApi,
} from '@/api/modules/paper-set'
import {
  PAPER_DEFAULT_FORM,
  PAPER_STATE_OPTIONS,
  PAPER_TYPE_OPTIONS,
  getPaperStateLabel,
  getPaperTypeLabel,
} from '@/constants/paper-set'
import FilterSelect from '@/components/common/FilterSelect.vue'
import FilterSummary from '@/components/common/FilterSummary.vue'
import { useFilterFormKey } from '@/composables/useFilterFormKey'
import { useSetupStageCascade } from '@/composables/useSetupStageCascade'
import { FILTER_SELECT_EMPTY, isFilterEmpty, normalizeNumberFilterQuery } from '@/utils/filter-form'
import { EMPTY_SETUP_OPTIONS } from '@/utils/setup-options'
import { patchFormModel, validateForm } from '@/utils/form'
import { confirmAction, showError, showSuccess } from '@/utils/modal'

const loading = ref(false)
const dialogVisible = ref(false)
const dialogLoading = ref(false)
const pickerVisible = ref(false)
const pickerLoading = ref(false)
const itemsVisible = ref(false)
const itemsLoading = ref(false)
const formRef = ref()
const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const editingId = ref(null)
const stateUpdatingId = ref(null)
const { filterFormKey, bumpFilterFormKey } = useFilterFormKey()
const setupOptions = ref({ ...EMPTY_SETUP_OPTIONS })

const currentPaper = ref(null)
const boundItems = ref([])
const boundTotal = ref(0)
const boundPageNum = ref(1)
const boundPageSize = ref(10)
const boundFilters = reactive({ title: '' })
const boundQuestionIds = ref(new Set())

const pickerFilters = reactive({ title: '' })
const pickerData = ref([])
const pickerTotal = ref(0)
const pickerPageNum = ref(1)
const pickerPageSize = ref(10)
/** dialog=新增/编辑弹窗选题，manage=管理题目弹窗选题 */
const pickerMode = ref('manage')
const draftQuestions = ref([])

const FILTER_FIELDS = computed(() => [
  { key: 'title', label: '标题', type: 'text', placeholder: '题集标题' },
  { key: 'paperType', label: '类型', type: 'select', options: PAPER_TYPE_OPTIONS, placeholder: '全部类型' },
  { key: 'stageCode', label: '学段', type: 'select', options: filterGradeStages.value, placeholder: '全部学段' },
  { key: 'grade', label: '年级', type: 'select', options: filterGrades.value, placeholder: '全部年级' },
  { key: 'subject', label: '学科', type: 'select', options: filterSubjects.value, placeholder: '全部学科' },
  { key: 'state', label: '上下架', type: 'select', options: PAPER_STATE_OPTIONS, placeholder: '全部状态' },
])

const filters = reactive({
  title: '',
  paperType: FILTER_SELECT_EMPTY,
  grade: FILTER_SELECT_EMPTY,
  subject: FILTER_SELECT_EMPTY,
  state: FILTER_SELECT_EMPTY,
})

const form = reactive({ ...PAPER_DEFAULT_FORM })

const {
  stageCode: filterStageCode,
  gradeStages: filterGradeStages,
  filteredGrades: filterGrades,
  filteredSubjects: filterSubjects,
  handleStageChange: handleFilterStageChange,
  resetStage: resetFilterStage,
} = useSetupStageCascade(setupOptions, {
  gradeRef: toRef(filters, 'grade'),
  subjectRef: toRef(filters, 'subject'),
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
  gradeRef: toRef(form, 'grade'),
  subjectRef: toRef(form, 'subject'),
  filterMode: false,
})

const filterSummaryFilters = computed(() => ({
  ...filters,
  stageCode: filterStageCode.value,
}))

const rules = {
  title: [{ required: true, message: '请输入题集标题', trigger: 'blur' }],
  paperType: [{ required: true, message: '请选择题集类型', trigger: 'change' }],
  grade: [{ required: true, message: '请选择年级', trigger: 'change' }],
  subject: [{ required: true, message: '请选择学科', trigger: 'change' }],
  sortOrder: [{ required: true, message: '请输入排序', trigger: 'change' }],
  state: [{ required: true, message: '请选择上下架状态', trigger: 'change' }],
}

const dialogTitle = computed(() => (editingId.value ? '编辑题集' : '新增题集'))

function buildQueryParams() {
  return {
    page: pageNum.value,
    limit: pageSize.value,
    title: filters.title || undefined,
    paperType: isFilterEmpty(filters.paperType) ? undefined : filters.paperType,
    grade: isFilterEmpty(filters.grade) ? undefined : filters.grade,
    subject: isFilterEmpty(filters.subject) ? undefined : filters.subject,
    state: normalizeNumberFilterQuery(filters.state),
  }
}

async function loadSetupOptions() {
  try {
    setupOptions.value = await loadPaperSetupOptions()
  } catch (error) {
    showError(error?.msg || error?.message || '加载年级学科选项失败')
  }
}

async function loadData() {
  loading.value = true
  try {
    const data = await questionPaperApi.page(buildQueryParams())
    tableData.value = data.list
    total.value = data.total
  } catch (error) {
    showError(error?.msg || error?.message || '加载题集列表失败')
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
  Object.assign(filters, {
    title: '',
    paperType: FILTER_SELECT_EMPTY,
    grade: FILTER_SELECT_EMPTY,
    subject: FILTER_SELECT_EMPTY,
    state: FILTER_SELECT_EMPTY,
  })
  bumpFilterFormKey()
  pageNum.value = 1
  loadData()
}

function resetForm(initial = {}) {
  patchFormModel(form, initial, { ...PAPER_DEFAULT_FORM })
  draftQuestions.value = []
}

async function loadDraftQuestions(paperId) {
  if (!paperId) {
    draftQuestions.value = []
    return
  }
  try {
    const data = await pagePaperItemsApi({
      paperId,
      page: 1,
      limit: 5000,
    })
    draftQuestions.value = (data.list || []).map((item) => ({
      questionId: item.questionId,
      questionCode: item.questionCode,
      title: item.questionTitle,
      questionType: item.questionType,
      sortOrder: item.sortOrder,
    }))
  } catch {
    draftQuestions.value = []
  }
}

function handleGradeChange(value) {
  syncFormStageFromGrade(value)
  const matched = setupOptions.value.grades.find((item) => item.value === value)
  if (matched?.code) {
    form.gradeShort = matched.code
  }
}

function openCreate() {
  editingId.value = null
  resetForm()
  resetFormStage()
  dialogVisible.value = true
}

async function openEdit(row) {
  editingId.value = row.id
  dialogLoading.value = true
  dialogVisible.value = true
  try {
    const data = await questionPaperApi.info(row.id)
    resetForm(data || row)
    syncFormStageFromGrade(form.grade)
    await loadDraftQuestions(row.id)
  } catch (error) {
    showError(error?.msg || error?.message || '加载题集详情失败')
    dialogVisible.value = false
  } finally {
    dialogLoading.value = false
  }
}

function buildDraftItemsPayload() {
  return draftQuestions.value.map((item, index) => ({
    questionCode: item.questionCode,
    sortOrder: item.sortOrder ?? index + 1,
  }))
}

function buildSubmitPayload() {
  const payload = {
    title: form.title,
    paperType: form.paperType,
    description: form.description,
    grade: form.grade,
    gradeShort: form.gradeShort,
    subject: form.subject,
    sortOrder: form.sortOrder,
    state: form.state,
  }
  if (editingId.value || draftQuestions.value.length) {
    payload.items = buildDraftItemsPayload()
  }
  return payload
}

async function submitForm() {
  const valid = await validateForm(formRef)
  if (!valid) return
  dialogLoading.value = true
  try {
    const payload = buildSubmitPayload()
    if (editingId.value) {
      await questionPaperApi.update({ ...payload, id: editingId.value })
    } else {
      await questionPaperApi.save(payload)
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
  await confirmAction(`确认删除题集「${row.title}」吗？`)
  await questionPaperApi.delete({ id: row.id })
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
    await updateQuestionPaperStateApi({ id: row.id, state: nextState })
    row.state = nextState
    showSuccess(`已${actionLabel}`)
  } catch (error) {
    row.state = previousState
    showError(error?.msg || error?.message || `${actionLabel}失败`)
  } finally {
    stateUpdatingId.value = null
  }
}

async function loadBoundIds() {
  if (!currentPaper.value?.id) {
    boundQuestionIds.value = new Set()
    return
  }
  try {
    const data = await pagePaperItemsApi({
      paperId: currentPaper.value.id,
      page: 1,
      limit: 5000,
    })
    boundQuestionIds.value = new Set((data.list || []).map((item) => item.questionId).filter(Boolean))
  } catch {
    boundQuestionIds.value = new Set()
  }
}

async function loadBoundItems() {
  if (!currentPaper.value?.id) return
  itemsLoading.value = true
  try {
    const data = await pagePaperItemsApi({
      paperId: currentPaper.value.id,
      page: boundPageNum.value,
      limit: boundPageSize.value,
      title: boundFilters.title || undefined,
    })
    boundItems.value = data.list
    boundTotal.value = data.total
  } catch (error) {
    showError(error?.msg || error?.message || '加载绑定题目失败')
  } finally {
    itemsLoading.value = false
  }
}

async function openItemsManage(row) {
  currentPaper.value = { ...row }
  boundFilters.title = ''
  boundPageNum.value = 1
  itemsVisible.value = true
  await loadBoundIds()
  await loadBoundItems()
}

function handleBoundSearch() {
  boundPageNum.value = 1
  loadBoundItems()
}

async function handleUnbind(row) {
  await confirmAction(`确认从题集中移除「${row.questionTitle || '该题目'}」吗？`)
  try {
    await unbindPaperQuestionApi({
      paperId: currentPaper.value.id,
      questionId: row.questionId,
    })
    showSuccess('已移除')
    await loadBoundIds()
    await loadBoundItems()
    await syncCurrentPaperCount()
  } catch (error) {
    showError(error?.msg || error?.message || '移除失败')
  }
}

async function syncCurrentPaperCount() {
  await loadData()
  const matched = tableData.value.find((item) => item.id === currentPaper.value?.id)
  if (matched) {
    currentPaper.value = { ...currentPaper.value, questionCount: matched.questionCount }
  }
}

async function loadPickerData() {
  pickerLoading.value = true
  try {
    const data = await searchPaperQuestionsApi({
      page: pickerPageNum.value,
      limit: pickerPageSize.value,
      title: pickerFilters.title || undefined,
    })
    pickerData.value = data.list
    pickerTotal.value = data.total
  } catch (error) {
    showError(error?.msg || error?.message || '搜索题目失败')
  } finally {
    pickerLoading.value = false
  }
}

function openQuestionPicker(mode = 'manage') {
  if (mode === 'manage' && !currentPaper.value?.id) {
    showError('请先保存题集后再绑定题目')
    return
  }
  pickerMode.value = mode
  pickerFilters.title = ''
  pickerPageNum.value = 1
  pickerVisible.value = true
  loadPickerData()
}

function handlePickerSearch() {
  pickerPageNum.value = 1
  loadPickerData()
}

function isQuestionBound(questionId) {
  if (pickerMode.value === 'dialog') {
    return draftQuestions.value.some((item) => item.questionId === questionId)
  }
  return boundQuestionIds.value.has(questionId)
}

function removeDraftQuestion(index) {
  draftQuestions.value.splice(index, 1)
}

async function addQuestionToPaper(row) {
  if (isQuestionBound(row.questionId)) {
    showError('该题目已在题集中')
    return
  }
  if (pickerMode.value === 'dialog') {
    draftQuestions.value.push({
      questionId: row.questionId,
      questionCode: row.questionCode,
      title: row.title,
      questionType: row.questionType,
      sortOrder: draftQuestions.value.length + 1,
    })
    showSuccess('已添加题目')
    return
  }
  try {
    await bindPaperQuestionsApi({
      paperId: currentPaper.value.id,
      questionIds: [row.questionId],
    })
    showSuccess('已添加题目')
    await loadBoundIds()
    await loadBoundItems()
    await syncCurrentPaperCount()
  } catch (error) {
    showError(error?.msg || error?.message || '添加失败')
  }
}

onMounted(async () => {
  await loadSetupOptions()
  await loadData()
})
</script>

<template>
  <el-card shadow="never" class="page-card">
    <template #header>
      <div class="card-header">
        <div>
          <strong>专项/真题题集管理</strong>
          <p class="header-desc">
            维护专项/真题题集基本信息；新增时可从题目列表选题，也可在「管理题目」中分页维护。
          </p>
        </div>
        <el-button type="primary" @click="openCreate">新增题集</el-button>
      </div>
    </template>

    <el-form :key="filterFormKey" :inline="true" class="filter-form" @submit.prevent="handleSearch">
      <el-form-item label="标题">
        <el-input v-model="filters.title" clearable placeholder="题集标题" @keyup.enter="handleSearch" />
      </el-form-item>
      <el-form-item label="类型">
        <FilterSelect v-model="filters.paperType" clearable placeholder="全部类型">
          <el-option v-for="item in PAPER_TYPE_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
        </FilterSelect>
      </el-form-item>
      <el-form-item label="学段">
        <FilterSelect
          :model-value="filterStageCode"
          clearable
          filterable
          placeholder="全部学段"
          @update:model-value="handleFilterStageChange"
        >
          <el-option v-for="item in filterGradeStages" :key="item.value" :label="item.label" :value="item.value" />
        </FilterSelect>
      </el-form-item>
      <el-form-item label="年级">
        <FilterSelect v-model="filters.grade" clearable filterable placeholder="全部年级">
          <el-option v-for="item in filterGrades" :key="item.value" :label="item.label" :value="item.value" />
        </FilterSelect>
      </el-form-item>
      <el-form-item label="学科">
        <FilterSelect v-model="filters.subject" clearable filterable placeholder="全部学科">
          <el-option v-for="item in filterSubjects" :key="item.value" :label="item.label" :value="item.value" />
        </FilterSelect>
      </el-form-item>
      <el-form-item label="上下架">
        <FilterSelect v-model="filters.state" clearable placeholder="全部状态">
          <el-option v-for="item in PAPER_STATE_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
        </FilterSelect>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
      </el-form-item>
    </el-form>

    <FilterSummary :filters="filterSummaryFilters" :fields="FILTER_FIELDS" />

    <el-table v-loading="loading" :data="tableData" border empty-text="暂无题集">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="title" label="标题" min-width="160" show-overflow-tooltip />
      <el-table-column label="类型" width="110">
        <template #default="{ row }">{{ getPaperTypeLabel(row.paperType) }}</template>
      </el-table-column>
      <el-table-column prop="grade" label="年级" min-width="120" />
      <el-table-column prop="gradeShort" label="年级简称" width="100" />
      <el-table-column prop="subject" label="学科" width="90" />
      <el-table-column prop="questionCount" label="题目数" width="90" />
      <el-table-column prop="sortOrder" label="排序" width="80" />
      <el-table-column label="上下架" width="110">
        <template #default="{ row }">
          <TableStateSwitch
            :model-value="row.state"
            :loading="stateUpdatingId === row.id"
            @change="(nextState) => handleStateChange(row, nextState)"
          />
        </template>
      </el-table-column>
      <el-table-column prop="description" label="简介" min-width="160" show-overflow-tooltip />
      <el-table-column prop="createTime" label="创建时间" width="170" />
      <el-table-column label="操作" min-width="220" fixed="right">
        <template #default="{ row }">
          <TableRowActions>
            <el-button link type="primary" @click="openItemsManage(row)">管理题目</el-button>
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
      :title="dialogTitle"
      :loading="dialogLoading"
      width="720px"
      @confirm="submitForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="题集标题" prop="title">
          <el-input v-model="form.title" maxlength="128" show-word-limit placeholder="如 有理数专项训练" />
        </el-form-item>
        <el-form-item label="题集类型" prop="paperType">
          <el-radio-group v-model="form.paperType">
            <el-radio v-for="item in PAPER_TYPE_OPTIONS" :key="item.value" :value="item.value">
              {{ item.label }}
            </el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="学段">
          <el-select
            :model-value="formStageCode"
            clearable
            filterable
            placeholder="选择学段"
            @update:model-value="handleFormStageChange"
          >
            <el-option v-for="item in formGradeStages" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="年级" prop="grade">
          <el-select v-model="form.grade" filterable placeholder="选择年级" @change="handleGradeChange">
            <el-option v-for="item in formGrades" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="年级简称">
          <el-input v-model="form.gradeShort" placeholder="如 初三（C 端 gradeShort）" />
        </el-form-item>
        <el-form-item label="学科" prop="subject">
          <el-select v-model="form.subject" filterable placeholder="选择学科">
            <el-option v-for="item in formSubjects" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="form.sortOrder" :min="0" :max="9999" />
          <span class="form-hint">数值越小越靠前，C 端列表按此字段升序</span>
        </el-form-item>
        <el-form-item label="上下架" prop="state">
          <el-radio-group v-model="form.state">
            <el-radio v-for="item in PAPER_STATE_OPTIONS" :key="item.value" :value="item.value">
              {{ item.label }}
            </el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="简介">
          <el-input v-model="form.description" type="textarea" :rows="2" maxlength="512" show-word-limit placeholder="题集简介，C 端展示" />
        </el-form-item>
        <el-form-item label="题目">
          <div class="draft-questions">
            <div class="draft-questions__toolbar">
              <span>已选 {{ draftQuestions.length }} 题</span>
              <el-button type="primary" link @click="openQuestionPicker('dialog')">从题目列表添加</el-button>
            </div>
            <el-table v-if="draftQuestions.length" :data="draftQuestions" border size="small" max-height="220" empty-text="暂未选择题目">
              <el-table-column prop="questionId" label="题目ID" width="80" />
              <el-table-column prop="title" label="题干" min-width="200" show-overflow-tooltip />
              <el-table-column prop="questionType" label="题型" width="90" />
              <el-table-column label="操作" width="80" fixed="right">
                <template #default="{ $index }">
                  <el-button link type="danger" @click="removeDraftQuestion($index)">移除</el-button>
                </template>
              </el-table-column>
            </el-table>
            <el-empty v-else description="可从题目列表选择题目，保存时一并绑定" :image-size="56" />
          </div>
        </el-form-item>
      </el-form>
    </AppDialog>

    <AppDialog
      v-model="itemsVisible"
      :title="`管理题目 - ${currentPaper?.title || ''}`"
      :show-footer="false"
      width="900px"
    >
      <div class="items-toolbar">
        <span>已绑定 {{ currentPaper?.questionCount ?? boundTotal }} 题</span>
        <el-button type="primary" @click="openQuestionPicker('manage')">添加题目</el-button>
      </div>
      <el-form :inline="true" class="filter-form" @submit.prevent="handleBoundSearch">
        <el-form-item label="题干">
          <el-input v-model="boundFilters.title" clearable placeholder="题干关键词" @keyup.enter="handleBoundSearch" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleBoundSearch">搜索</el-button>
        </el-form-item>
      </el-form>
      <el-table v-loading="itemsLoading" :data="boundItems" border size="small" empty-text="暂未绑定题目">
        <el-table-column prop="questionId" label="题目ID" width="80" />
        <el-table-column prop="sortOrder" label="序号" width="70" />
        <el-table-column prop="questionTitle" label="题干" min-width="260" show-overflow-tooltip />
        <el-table-column prop="questionType" label="题型" width="90" />
        <el-table-column label="操作" width="90" fixed="right">
          <template #default="{ row }">
            <el-button link type="danger" @click="handleUnbind(row)">移除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <BasePagination
        :current-page="boundPageNum"
        :page-size="boundPageSize"
        :total="boundTotal"
        @update:current-page="boundPageNum = $event; loadBoundItems()"
        @update:page-size="boundPageSize = $event; boundPageNum = 1; loadBoundItems()"
      />
    </AppDialog>

    <AppDialog
      v-model="pickerVisible"
      title="搜索并添加题目"
      :show-footer="false"
      width="760px"
    >
      <el-form :inline="true" class="filter-form" @submit.prevent="handlePickerSearch">
        <el-form-item label="题干">
          <el-input v-model="pickerFilters.title" clearable placeholder="题干关键词" @keyup.enter="handlePickerSearch" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handlePickerSearch">搜索</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="pickerLoading" :data="pickerData" border size="small" empty-text="暂无题目">
        <el-table-column prop="questionId" label="题目ID" width="80" />
        <el-table-column prop="title" label="题干" min-width="220" show-overflow-tooltip />
        <el-table-column prop="questionType" label="题型" width="90" />
        <el-table-column prop="difficulty" label="难度" width="90" />
        <el-table-column label="操作" width="90" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" :disabled="isQuestionBound(row.questionId)" @click="addQuestionToPaper(row)">
              {{ isQuestionBound(row.questionId) ? '已添加' : '添加' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <BasePagination
        :current-page="pickerPageNum"
        :page-size="pickerPageSize"
        :total="pickerTotal"
        @update:current-page="pickerPageNum = $event; loadPickerData()"
        @update:page-size="pickerPageSize = $event; pickerPageNum = 1; loadPickerData()"
      />
    </AppDialog>
  </el-card>
</template>

<style scoped>
.filter-form { margin-bottom: 16px; }
.header-desc {
  margin: 4px 0 0;
  font-size: 13px;
  color: var(--el-text-color-secondary);
  font-weight: normal;
}
.form-hint {
  margin-left: 12px;
  color: var(--el-text-color-secondary);
  font-size: 12px;
}
.items-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  font-size: 13px;
  color: var(--el-text-color-secondary);
}
.draft-questions {
  width: 100%;
}
.draft-questions__toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  font-size: 13px;
  color: var(--el-text-color-secondary);
}
</style>
