<script setup>
import { computed, onMounted, reactive, ref, toRef } from 'vue'
import { RouterLink } from 'vue-router'
import AppDialog from '@/components/common/AppDialog.vue'
import BasePagination from '@/components/common/BasePagination.vue'
import TableRowActions from '@/components/common/TableRowActions.vue'
import TableStateSwitch from '@/components/common/TableStateSwitch.vue'
import {
  loadChapterSetupOptions,
  importQuestionsApi,
  questionApi,
  searchChapterOptions,
  searchKnowledgePointOptions,
  searchPaperOptions,
  searchSectionOptions,
  updateQuestionStateApi,
} from '@/api/modules/question'
import {
  QUESTION_DEFAULT_FORM,
  QUESTION_DEFAULT_OPTION,
  QUESTION_DIFFICULTY_OPTIONS,
  QUESTION_PRACTICE_ENTRY_OPTIONS,
  QUESTION_STATE_OPTIONS,
  QUESTION_TYPE_OPTIONS,
  getQuestionDifficultyLabel,
  getQuestionPracticeEntryLabel,
  getQuestionStateLabel,
  getQuestionTypeLabel,
  isPaperPracticeEntry,
} from '@/constants/question'
import FilterSelect from '@/components/common/FilterSelect.vue'
import FilterSummary from '@/components/common/FilterSummary.vue'
import FileUrlInput from '@/components/common/FileUrlInput.vue'
import VideoPreview from '@/components/common/VideoPreview.vue'
import ImageUpload from '@/components/common/ImageUpload.vue'
import { useFilterFormKey } from '@/composables/useFilterFormKey'
import { useSetupStageCascade } from '@/composables/useSetupStageCascade'
import { FILTER_SELECT_EMPTY, isFilterEmpty, normalizeNumberFilterQuery } from '@/utils/filter-form'
import { EMPTY_SETUP_OPTIONS } from '@/utils/setup-options'
import { normalizeStoredMediaPath } from '@/utils/image-field'
import { patchFormModel, resetFormModel, validateForm } from '@/utils/form'
import { confirmAction, showError, showSuccess } from '@/utils/modal'
import {
  downloadQuestionImportTemplate,
  parseQuestionImportFile,
} from '@/constants/question-import'

const loading = ref(false)
const dialogVisible = ref(false)
const dialogLoading = ref(false)
const formRef = ref()
const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const editingId = ref(null)
const setupOptions = ref({ ...EMPTY_SETUP_OPTIONS })
const chapterOptions = ref([])
const sectionOptions = ref([])
const knowledgeOptions = ref([])
const paperOptions = ref([])
const chapterOptionsLoading = ref(false)
const sectionOptionsLoading = ref(false)
const bindOptionsLoading = ref(false)
const detailVisible = ref(false)
const detailLoading = ref(false)
const detailData = ref(null)

const stateUpdatingId = ref(null)
const importVisible = ref(false)
const importLoading = ref(false)
const importSkipExisting = ref(true)
const importResult = ref(null)
const { filterFormKey, bumpFilterFormKey } = useFilterFormKey()

const filters = reactive({
  practiceEntry: FILTER_SELECT_EMPTY,
  grade: FILTER_SELECT_EMPTY,
  subject: FILTER_SELECT_EMPTY,
  version: FILTER_SELECT_EMPTY,
  title: '',
  questionType: FILTER_SELECT_EMPTY,
  tagKeyword: '',
  chapterTitle: '',
  sectionTitle: '',
  state: FILTER_SELECT_EMPTY,
})

const form = reactive({ ...QUESTION_DEFAULT_FORM })

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

const FILTER_FIELDS = computed(() => [
  { key: 'practiceEntry', label: '练习入口', type: 'select', options: QUESTION_PRACTICE_ENTRY_OPTIONS, placeholder: '全部入口' },
  { key: 'stageCode', label: '学段', type: 'select', options: filterGradeStages.value, placeholder: '全部学段' },
  { key: 'grade', label: '年级', type: 'select', options: filterGrades.value, placeholder: '全部年级' },
  { key: 'subject', label: '学科', type: 'select', options: filterSubjects.value, placeholder: '全部学科' },
  { key: 'version', label: '教材版本', type: 'select', options: setupOptions.value.versions, placeholder: '全部版本' },
  { key: 'title', label: '题干', type: 'text', placeholder: '题干关键词' },
  { key: 'questionType', label: '题型', type: 'select', options: QUESTION_TYPE_OPTIONS, placeholder: '全部题型' },
  { key: 'tagKeyword', label: '标签', type: 'text', placeholder: '标签关键字' },
  { key: 'chapterTitle', label: '章标题', type: 'text', placeholder: '章标题' },
  { key: 'sectionTitle', label: '节标题', type: 'text', placeholder: '节标题' },
  { key: 'state', label: '状态', type: 'select', options: QUESTION_STATE_OPTIONS, placeholder: '全部状态' },
])

const rules = {
  practiceEntry: [{ required: true, message: '请选择练习入口', trigger: 'change' }],
  subject: [{ required: true, message: '请选择学科', trigger: 'change' }],
  chapterCode: [{
    validator: (_rule, _value, callback) => {
      if (form.practiceEntry !== 'chapter') {
        callback()
        return
      }
      if (!form.chapterCode) {
        callback(new Error('请选择所属章'))
        return
      }
      callback()
    },
    trigger: 'change',
  }],
  pointCode: [{
    validator: (_rule, _value, callback) => {
      if (form.practiceEntry !== 'knowledge') {
        callback()
        return
      }
      if (!form.pointCode) {
        callback(new Error('请选择知识点'))
        return
      }
      callback()
    },
    trigger: 'change',
  }],
  paperCode: [{
    validator: (_rule, _value, callback) => {
      if (!isPaperPracticeEntry(form.practiceEntry)) {
        callback()
        return
      }
      if (!form.paperCode) {
        callback(new Error('请选择题集'))
        return
      }
      callback()
    },
    trigger: 'change',
  }],
  title: [{ required: true, message: '请输入题干', trigger: 'blur' }],
  questionType: [{ required: true, message: '请选择题型', trigger: 'change' }],
  rightAnswer: [{ required: true, message: '请输入正确答案', trigger: 'blur' }],
  analysis: [{ required: true, message: '请填写题目解析（每题独立，不可留空）', trigger: 'blur' }],
  videoUrl: [{ required: true, message: '请上传或填写讲解视频（每题独立，不可使用默认）', trigger: 'blur' }],
  sortOrder: [{ required: true, message: '请输入排序', trigger: 'change' }],
  state: [{ required: true, message: '请选择状态', trigger: 'change' }],
}

const currentPracticeEntryHint = computed(() =>
  QUESTION_PRACTICE_ENTRY_OPTIONS.find((item) => item.value === form.practiceEntry)?.hint || '',
)

const dialogTitle = computed(() => (editingId.value ? '编辑题目' : '新增题目'))
const optionLabelChoices = ['A', 'B', 'C', 'D', 'E', 'F']

function buildQueryParams() {
  return {
    page: pageNum.value,
    limit: pageSize.value,
    practiceEntry: isFilterEmpty(filters.practiceEntry) ? undefined : filters.practiceEntry,
    grade: isFilterEmpty(filters.grade) ? undefined : filters.grade,
    subject: isFilterEmpty(filters.subject) ? undefined : filters.subject,
    version: isFilterEmpty(filters.version) ? undefined : filters.version,
    title: filters.title || undefined,
    questionType: isFilterEmpty(filters.questionType) ? undefined : filters.questionType,
    tagKeyword: filters.tagKeyword || undefined,
    chapterTitle: filters.chapterTitle || undefined,
    sectionTitle: filters.sectionTitle || undefined,
    state: normalizeNumberFilterQuery(filters.state),
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
      grade: form.grade,
      subject: form.subject,
      version: form.version,
      keyword,
    })
  } catch (error) {
    chapterOptions.value = []
    showError(error?.msg || error?.message || '加载章列表失败')
  } finally {
    chapterOptionsLoading.value = false
  }
}

async function loadBindTargetOptions(keyword = '') {
  bindOptionsLoading.value = true
  try {
    if (form.practiceEntry === 'knowledge') {
      knowledgeOptions.value = await searchKnowledgePointOptions({
        grade: form.grade,
        subject: form.subject,
        keyword,
      })
      return
    }
    if (isPaperPracticeEntry(form.practiceEntry)) {
      paperOptions.value = await searchPaperOptions({
        grade: form.grade,
        subject: form.subject,
        paperType: form.practiceEntry,
        keyword,
      })
    }
  } catch (error) {
    knowledgeOptions.value = []
    paperOptions.value = []
    showError(error?.msg || error?.message || '加载绑定目标失败')
  } finally {
    bindOptionsLoading.value = false
  }
}

async function reloadBindOptions(clearSelection = true) {
  if (clearSelection) {
    form.chapterCode = ''
    form.sectionCode = ''
    form.pointCode = ''
    form.paperCode = ''
    sectionOptions.value = []
  }
  knowledgeOptions.value = []
  paperOptions.value = []
  if (form.practiceEntry === 'chapter') {
    await loadChapterOptions()
    return
  }
  if (form.practiceEntry === 'knowledge' || isPaperPracticeEntry(form.practiceEntry)) {
    await loadBindTargetOptions()
  }
}

function handleBindContextChange() {
  if (!dialogVisible.value || dialogLoading.value) return
  if (!form.subject) return
  reloadBindOptions(true)
}

async function loadSectionOptions(keyword = '') {
  if (!form.chapterCode) {
    sectionOptions.value = []
    return
  }
  sectionOptionsLoading.value = true
  try {
    sectionOptions.value = await searchSectionOptions({
      chapterCode: form.chapterCode,
      keyword,
    })
  } catch (error) {
    sectionOptions.value = []
    showError(error?.msg || error?.message || '加载节列表失败')
  } finally {
    sectionOptionsLoading.value = false
  }
}

async function loadData() {
  loading.value = true
  try {
    const data = await questionApi.page(buildQueryParams())
    tableData.value = data.list
    total.value = data.total
  } catch (error) {
    showError(error?.msg || error?.message || '加载题目列表失败')
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
  Object.assign(filters, {
    practiceEntry: FILTER_SELECT_EMPTY,
    grade: FILTER_SELECT_EMPTY,
    subject: FILTER_SELECT_EMPTY,
    version: FILTER_SELECT_EMPTY,
    title: '',
    questionType: FILTER_SELECT_EMPTY,
    tagKeyword: '',
    chapterTitle: '',
    sectionTitle: '',
    state: FILTER_SELECT_EMPTY,
  })
  bumpFilterFormKey()
  pageNum.value = 1
  loadData()
}

function resetForm(initial = {}) {
  patchFormModel(form, initial, {
    ...QUESTION_DEFAULT_FORM,
    options: QUESTION_DEFAULT_FORM.options.map((item) => ({ ...item })),
    tags: [],
  })
  if (!Array.isArray(form.options) || form.options.length === 0) {
    form.options = QUESTION_DEFAULT_FORM.options.map((item) => ({ ...item }))
  }
  if (!Array.isArray(form.tags)) {
    form.tags = []
  }
}

function openCreate() {
  editingId.value = null
  resetForm()
  resetFormStage()
  dialogVisible.value = true
}

async function openDetail(row) {
  detailVisible.value = true
  detailLoading.value = true
  detailData.value = null
  try {
    const data = await questionApi.info(row.id)
    detailData.value = {
      ...(data || row),
      options: (data?.options || []).map((item) => ({
        label: item.label || '',
        value: item.value || '',
      })),
      tags: [...(data?.tags || [])],
    }
  } catch (error) {
    showError(error?.msg || error?.message || '加载题目详情失败')
    detailVisible.value = false
  } finally {
    detailLoading.value = false
  }
}

async function openEdit(row) {
  editingId.value = row.id
  dialogVisible.value = true
  dialogLoading.value = true
  try {
    const data = await questionApi.info(row.id)
    resetForm(data || row)
    form.options = (data?.options || []).map((item) => ({
      label: item.label || '',
      value: item.value || '',
    }))
    form.tags = [...(data?.tags || [])]
    syncFormStageFromGrade(form.grade)
    const savedChapterCode = form.chapterCode
    const savedSectionCode = form.sectionCode
    const savedPointCode = form.pointCode
    const savedPaperCode = form.paperCode
    if (form.practiceEntry === 'chapter') {
      await reloadBindOptions(false)
      form.chapterCode = savedChapterCode
      form.sectionCode = savedSectionCode
      if (form.chapterCode) {
        await loadSectionOptions()
      }
    } else {
      await reloadBindOptions(false)
      form.pointCode = savedPointCode
      form.paperCode = savedPaperCode
    }
  } catch (error) {
    showError(error?.msg || error?.message || '加载题目详情失败')
    dialogVisible.value = false
  } finally {
    dialogLoading.value = false
  }
}

function handleFormGradeChange(gradeValue) {
  syncFormStageFromGrade(gradeValue)
  handleBindContextChange()
}

function handleChapterChange() {
  form.sectionCode = ''
  loadSectionOptions()
}

function addOptionRow() {
  const nextIndex = form.options.length
  const nextLabel = optionLabelChoices[nextIndex] || String(nextIndex + 1)
  form.options.push({ ...QUESTION_DEFAULT_OPTION, label: nextLabel })
}

function removeOptionRow(index) {
  if (form.options.length <= 2) {
    showError('至少保留两个选项')
    return
  }
  form.options.splice(index, 1)
}

function buildSubmitPayload() {
  return {
    practiceEntry: form.practiceEntry,
    subject: form.subject,
    grade: form.grade || undefined,
    version: form.version || undefined,
    chapterCode: form.practiceEntry === 'chapter' ? form.chapterCode : undefined,
    sectionCode: form.practiceEntry === 'chapter' ? (form.sectionCode || undefined) : undefined,
    pointCode: form.practiceEntry === 'knowledge' ? form.pointCode : undefined,
    paperCode: isPaperPracticeEntry(form.practiceEntry) ? form.paperCode : undefined,
    title: form.title,
    questionType: form.questionType,
    difficulty: form.difficulty || undefined,
    rightAnswer: form.rightAnswer,
    analysis: form.analysis || undefined,
    options: form.options
      .filter((item) => item.label && item.value)
      .map((item) => ({ label: item.label, value: item.value })),
    tags: (form.tags || []).filter(Boolean),
    sortOrder: form.sortOrder,
    state: form.state,
    videoUrl: normalizeStoredMediaPath(form.videoUrl) || undefined,
    videoPosterUrl: normalizeStoredMediaPath(form.videoPosterUrl) || undefined,
    videoDuration: form.videoDuration || undefined,
    videoTeacher: form.videoTeacher || undefined,
    videoSummary: form.videoSummary || undefined,
  }
}

async function submitForm() {
  const valid = await validateForm(formRef)
  if (!valid) return
  if (buildSubmitPayload().options.length < 2) {
    showError('请至少填写两个有效选项')
    return
  }
  dialogLoading.value = true
  try {
    const payload = buildSubmitPayload()
    if (editingId.value) {
      await questionApi.update({ ...payload, id: editingId.value })
      showSuccess('题目已更新')
    } else {
      await questionApi.save(payload)
      showSuccess('题目已新增')
    }
    dialogVisible.value = false
    await loadData()
  } catch (error) {
    showError(error?.msg || error?.message || '保存失败')
  } finally {
    dialogLoading.value = false
  }
}

async function handleDelete(row) {
  await confirmAction(`确认删除题目「${row.title}」吗？`)
  try {
    await questionApi.delete({ id: row.id })
    showSuccess('删除成功')
    await loadData()
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
    await updateQuestionStateApi({ id: row.id, state: nextState })
    row.state = nextState
    showSuccess(`${actionLabel}成功`)
  } catch (error) {
    row.state = previousState
    showError(error?.msg || error?.message || `${actionLabel}失败`)
  } finally {
    stateUpdatingId.value = null
  }
}

function formatTags(tags) {
  return Array.isArray(tags) && tags.length ? tags.join('、') : '-'
}

function openImport() {
  importResult.value = null
  importSkipExisting.value = true
  importVisible.value = true
}

async function handleImportFile(uploadFile) {
  importLoading.value = true
  importResult.value = null
  try {
    const items = await parseQuestionImportFile(uploadFile.raw)
    const result = await importQuestionsApi({
      items,
      skipExisting: importSkipExisting.value,
    })
    importResult.value = result
    if (Number(result?.successCount || 0) > 0) {
      await loadData()
    }
    const summary = `成功 ${result?.successCount || 0} 条，跳过 ${result?.skipCount || 0} 条，失败 ${result?.failCount || 0} 条`
    if (Number(result?.failCount || 0) > 0) {
      showError(summary)
    } else {
      showSuccess(summary)
    }
  } catch (error) {
    showError(error?.msg || error?.message || '导入失败')
  } finally {
    importLoading.value = false
  }
  return false
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
          <strong>题目管理</strong>
          <p class="header-desc">
            新增题目时先选择「练习入口」和学科，再绑定到章节、知识点或题集，题目会出现在 C 端刷题中心对应模块。
            章节/知识点/题集请分别在
            <RouterLink to="/business/chapter-tree">章节题库</RouterLink>
            与题集管理中维护。
          </p>
        </div>
        <el-space>
          <el-button @click="downloadQuestionImportTemplate">下载模板</el-button>
          <el-button @click="openImport">批量导入</el-button>
          <el-button type="primary" @click="openCreate">新增题目</el-button>
        </el-space>
      </div>
    </template>

    <el-form :key="filterFormKey" :inline="true" class="filter-form" @submit.prevent="handleSearch">
      <el-form-item label="练习入口">
        <FilterSelect v-model="filters.practiceEntry" clearable placeholder="全部入口">
          <el-option v-for="item in QUESTION_PRACTICE_ENTRY_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
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
      <el-form-item label="教材版本">
        <FilterSelect v-model="filters.version" clearable filterable placeholder="全部版本">
          <el-option v-for="item in setupOptions.versions" :key="item.value" :label="item.label" :value="item.value" />
        </FilterSelect>
      </el-form-item>
      <el-form-item label="题干">
        <el-input v-model="filters.title" clearable placeholder="题干关键词" @keyup.enter="handleSearch" />
      </el-form-item>
      <el-form-item label="题型">
        <FilterSelect v-model="filters.questionType" clearable placeholder="全部题型">
          <el-option v-for="item in QUESTION_TYPE_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
        </FilterSelect>
      </el-form-item>
      <el-form-item label="标签">
        <el-input v-model="filters.tagKeyword" clearable placeholder="标签关键字" @keyup.enter="handleSearch" />
      </el-form-item>
      <el-form-item label="章标题">
        <el-input v-model="filters.chapterTitle" clearable placeholder="章标题" @keyup.enter="handleSearch" />
      </el-form-item>
      <el-form-item label="节标题">
        <el-input v-model="filters.sectionTitle" clearable placeholder="节标题" @keyup.enter="handleSearch" />
      </el-form-item>
      <el-form-item label="状态">
        <FilterSelect v-model="filters.state" clearable placeholder="全部状态">
          <el-option v-for="item in QUESTION_STATE_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
        </FilterSelect>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
      </el-form-item>
    </el-form>

    <FilterSummary :filters="filterSummaryFilters" :fields="FILTER_FIELDS" />

    <el-table v-loading="loading" :data="tableData" border empty-text="暂无题目">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="title" label="题干" min-width="200" show-overflow-tooltip />
      <el-table-column label="练习入口" width="100">
        <template #default="{ row }">{{ getQuestionPracticeEntryLabel(row.practiceEntry) }}</template>
      </el-table-column>
      <el-table-column label="题型" width="90">
        <template #default="{ row }">{{ getQuestionTypeLabel(row.questionType) }}</template>
      </el-table-column>
      <el-table-column label="难度" width="80">
        <template #default="{ row }">{{ getQuestionDifficultyLabel(row.difficulty) }}</template>
      </el-table-column>
      <el-table-column prop="subject" label="学科" width="80" />
      <el-table-column prop="grade" label="年级" width="100" />
      <el-table-column prop="rightAnswer" label="答案" width="70" />
      <el-table-column label="标签" min-width="120" show-overflow-tooltip>
        <template #default="{ row }">{{ formatTags(row.tags) }}</template>
      </el-table-column>
      <el-table-column prop="sortOrder" label="排序" width="70" />
      <el-table-column label="解析" width="80">
        <template #default="{ row }">
          <el-tag v-if="row.analysis" type="success" size="small">已配置</el-tag>
          <el-tag v-else type="danger" size="small">未配置</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="讲解视频" width="180">
        <template #default="{ row }">
          <VideoPreview :src="row.videoUrl" :poster="row.videoPosterUrl" compact />
        </template>
      </el-table-column>
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
      <el-table-column label="操作" min-width="220" fixed="right">
        <template #default="{ row }">
          <TableRowActions>
            <el-button link type="primary" @click="openDetail(row)">查看详情</el-button>
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
      width="920px"
      @confirm="submitForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-divider content-position="left">刷题中心归属</el-divider>
        <el-form-item label="练习入口" prop="practiceEntry">
          <el-radio-group v-model="form.practiceEntry" @change="handleBindContextChange">
            <el-radio v-for="item in QUESTION_PRACTICE_ENTRY_OPTIONS" :key="item.value" :value="item.value">
              {{ item.label }}
            </el-radio>
          </el-radio-group>
          <p v-if="currentPracticeEntryHint" class="form-hint block-hint">{{ currentPracticeEntryHint }}</p>
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
        <el-form-item label="年级">
          <el-select
            v-model="form.grade"
            clearable
            filterable
            placeholder="筛选绑定目标（建议填写）"
            @change="handleFormGradeChange"
          >
            <el-option v-for="item in formGrades" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="学科" prop="subject">
          <el-select v-model="form.subject" clearable filterable placeholder="请选择学科" @change="handleBindContextChange">
            <el-option v-for="item in formSubjects" :key="item.value" :label="item.label" :value="item.label" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="form.practiceEntry === 'chapter'" label="教材版本">
          <el-select v-model="form.version" clearable filterable placeholder="可选，进一步筛选章节" @change="handleBindContextChange">
            <el-option v-for="item in setupOptions.versions" :key="item.value" :label="item.label" :value="item.label" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="form.practiceEntry === 'chapter'" label="所属章" prop="chapterCode">
          <el-select
            v-model="form.chapterCode"
            filterable
            remote
            reserve-keyword
            :disabled="!form.subject"
            :remote-method="loadChapterOptions"
            :loading="chapterOptionsLoading"
            placeholder="选择章节"
            @change="handleChapterChange"
          >
            <el-option v-for="item in chapterOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="form.practiceEntry === 'chapter'" label="所属节">
          <el-select
            v-model="form.sectionCode"
            clearable
            filterable
            remote
            reserve-keyword
            :disabled="!form.chapterCode"
            :remote-method="loadSectionOptions"
            :loading="sectionOptionsLoading"
            placeholder="可选，选择小节"
          >
            <el-option v-for="item in sectionOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="form.practiceEntry === 'knowledge'" label="知识点" prop="pointCode">
          <el-select
            v-model="form.pointCode"
            filterable
            remote
            reserve-keyword
            :disabled="!form.subject"
            :remote-method="loadBindTargetOptions"
            :loading="bindOptionsLoading"
            placeholder="选择知识点"
          >
            <el-option v-for="item in knowledgeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="isPaperPracticeEntry(form.practiceEntry)" label="题集" prop="paperCode">
          <el-select
            v-model="form.paperCode"
            filterable
            remote
            reserve-keyword
            :disabled="!form.subject"
            :remote-method="loadBindTargetOptions"
            :loading="bindOptionsLoading"
            placeholder="选择专项/真题题集"
          >
            <el-option v-for="item in paperOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-divider content-position="left">题目内容</el-divider>
        <el-form-item label="题干" prop="title">
          <el-input v-model="form.title" type="textarea" :rows="3" maxlength="512" show-word-limit placeholder="题目题干" />
        </el-form-item>
        <el-form-item label="题型" prop="questionType">
          <el-radio-group v-model="form.questionType">
            <el-radio v-for="item in QUESTION_TYPE_OPTIONS" :key="item.value" :value="item.value">
              {{ item.label }}
            </el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="难度">
          <el-select v-model="form.difficulty" clearable placeholder="选择难度">
            <el-option v-for="item in QUESTION_DIFFICULTY_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="选项">
          <div class="options-panel">
            <div class="options-toolbar">
              <span>选项列表（label 为 A/B/C，value 为选项内容）</span>
              <el-button type="primary" link @click="addOptionRow">添加选项</el-button>
            </div>
            <el-table :data="form.options" border size="small">
              <el-table-column label="标签" width="100">
                <template #default="{ row }">
                  <el-input v-model="row.label" maxlength="8" placeholder="A" />
                </template>
              </el-table-column>
              <el-table-column label="内容" min-width="280">
                <template #default="{ row }">
                  <el-input v-model="row.value" placeholder="选项内容" />
                </template>
              </el-table-column>
              <el-table-column label="操作" width="80">
                <template #default="{ $index }">
                  <el-button link type="danger" @click="removeOptionRow($index)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-form-item>
        <el-form-item label="正确答案" prop="rightAnswer">
          <el-input v-model="form.rightAnswer" placeholder="如 A 或 AB（多选）" />
        </el-form-item>
        <el-form-item label="解析" prop="analysis">
          <el-input v-model="form.analysis" type="textarea" :rows="3" placeholder="每题必填独立解析，C 端答题后展示" />
        </el-form-item>
        <el-divider content-position="left">高清视频解析（C 端会员专享，每题必填）</el-divider>
        <el-form-item label="讲解视频" prop="videoUrl">
          <FileUrlInput v-model="form.videoUrl" upload-only button-text="上传讲解视频" />
          <p class="form-hint block-hint">请上传本题专属讲解视频，不支持填写外部链接。</p>
        </el-form-item>
        <el-form-item label="视频封面">
          <ImageUpload v-model="form.videoPosterUrl" :limit="1" />
        </el-form-item>
        <el-form-item label="讲师">
          <el-input v-model="form.videoTeacher" maxlength="64" placeholder="如 王老师" />
        </el-form-item>
        <el-form-item label="时长文案">
          <el-input v-model="form.videoDuration" maxlength="16" placeholder="如 03:15" />
        </el-form-item>
        <el-form-item label="视频简介">
          <el-input v-model="form.videoSummary" type="textarea" :rows="2" maxlength="512" show-word-limit placeholder="C 端视频下方展示的简介" />
        </el-form-item>
        <el-form-item label="标签">
          <el-select
            v-model="form.tags"
            multiple
            filterable
            allow-create
            default-first-option
            placeholder="输入后回车添加标签"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="form.sortOrder" :min="0" :max="9999" />
          <span class="form-hint">同组题目按升序展示</span>
        </el-form-item>
        <el-form-item label="状态" prop="state">
          <el-radio-group v-model="form.state">
            <el-radio v-for="item in QUESTION_STATE_OPTIONS" :key="item.value" :value="item.value">
              {{ item.label }}
            </el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
    </AppDialog>

    <AppDialog
      v-model="detailVisible"
      title="题目详情"
      :show-footer="false"
      :loading="detailLoading"
      width="920px"
    >
      <template v-if="detailData">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="练习入口">{{ getQuestionPracticeEntryLabel(detailData.practiceEntry) }}</el-descriptions-item>
          <el-descriptions-item label="绑定位置">{{ detailData.bindTarget || '—' }}</el-descriptions-item>
          <el-descriptions-item label="学科">{{ detailData.subject || '—' }}</el-descriptions-item>
          <el-descriptions-item label="年级">{{ detailData.grade || '—' }}</el-descriptions-item>
          <el-descriptions-item label="题型">{{ getQuestionTypeLabel(detailData.questionType) }}</el-descriptions-item>
          <el-descriptions-item label="难度">{{ getQuestionDifficultyLabel(detailData.difficulty) }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ getQuestionStateLabel(detailData.state) }}</el-descriptions-item>
          <el-descriptions-item label="正确答案">{{ detailData.rightAnswer || '—' }}</el-descriptions-item>
          <el-descriptions-item label="标签">{{ formatTags(detailData.tags) }}</el-descriptions-item>
          <el-descriptions-item label="题干" :span="2">{{ detailData.title || '—' }}</el-descriptions-item>
          <el-descriptions-item label="解析" :span="2">{{ detailData.analysis || '—' }}</el-descriptions-item>
          <el-descriptions-item label="讲解视频" :span="2">
            <VideoPreview :src="detailData.videoUrl" :poster="detailData.videoPosterUrl" />
          </el-descriptions-item>
          <el-descriptions-item label="讲师">{{ detailData.videoTeacher || '—' }}</el-descriptions-item>
          <el-descriptions-item label="时长">{{ detailData.videoDuration || '—' }}</el-descriptions-item>
          <el-descriptions-item label="视频简介" :span="2">{{ detailData.videoSummary || '—' }}</el-descriptions-item>
        </el-descriptions>
        <div class="detail-section">
          <strong>选项列表</strong>
          <el-table :data="detailData.options || []" border size="small" empty-text="暂无选项">
            <el-table-column prop="label" label="标签" width="80" />
            <el-table-column prop="value" label="内容" min-width="280" show-overflow-tooltip />
          </el-table>
        </div>
      </template>
    </AppDialog>

    <AppDialog
      v-model="importVisible"
      title="批量导入题目"
      :show-footer="false"
      width="640px"
    >
      <el-alert
        type="info"
        :closable="false"
        show-icon
        title="导入说明"
        description="请上传 JSON 文件（数组格式）。每条需包含 practiceEntry、subject、analysis 及绑定标题：chapter 用 chapterTitle/sectionTitle，knowledge 用 pointTitle，special/exam 用 paperTitle；videoUrl 须为本系统 /file/upload 上传后的路径（导入后可在编辑页补传视频）。题目编号可留空由系统自动生成。"
        class="import-alert"
      />
      <el-form label-width="120px" class="import-form">
        <el-form-item label="重复题目">
          <el-checkbox v-model="importSkipExisting">跳过（不覆盖）</el-checkbox>
        </el-form-item>
        <el-form-item label="选择文件">
          <el-upload
            accept=".json,application/json"
            :show-file-list="false"
            :disabled="importLoading"
            :before-upload="() => false"
            :on-change="(file) => handleImportFile(file)"
          >
            <el-button type="primary" :loading="importLoading">上传 JSON 并导入</el-button>
          </el-upload>
        </el-form-item>
      </el-form>
      <div v-if="importResult" class="import-result">
        <p>
          成功 {{ importResult.successCount || 0 }} 条，
          跳过 {{ importResult.skipCount || 0 }} 条，
          失败 {{ importResult.failCount || 0 }} 条
        </p>
        <el-table
          v-if="importResult.errors?.length"
          :data="importResult.errors"
          border
          size="small"
          max-height="240"
        >
          <el-table-column prop="rowNum" label="行号" width="70" />
          <el-table-column prop="message" label="失败原因" min-width="280" show-overflow-tooltip />
        </el-table>
      </div>
    </AppDialog>
  </el-card>
</template>

<style scoped>
.filter-form {
  margin-bottom: 16px;
}
.header-desc {
  margin: 4px 0 0;
  font-size: 13px;
  color: var(--el-text-color-secondary);
  font-weight: normal;
}
.detail-section {
  margin-top: 16px;
}
.detail-section strong {
  display: block;
  margin-bottom: 8px;
}
.form-hint {
  margin-left: 12px;
  color: var(--el-text-color-secondary);
  font-size: 12px;
}
.block-hint {
  display: block;
  margin: 8px 0 0;
  margin-left: 0;
}
.options-panel {
  width: 100%;
}
.options-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  font-size: 13px;
  color: var(--el-text-color-secondary);
}
.import-alert {
  margin-bottom: 16px;
}
.import-form {
  margin-bottom: 12px;
}
.import-result p {
  margin: 0 0 8px;
  font-size: 13px;
}
</style>
