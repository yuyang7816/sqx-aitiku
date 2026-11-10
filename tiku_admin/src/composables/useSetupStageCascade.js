import { computed, ref } from 'vue'
import { FILTER_SELECT_EMPTY, isFilterEmpty } from '@/utils/filter-form'
import {
  buildGradeStagesFromGrades,
  filterGradesByStage,
  filterSubjectsByStage,
  resolveStageCodeFromGrade,
} from '@/utils/setup-options'

/**
 * 学段 → 年级 → 学科 三级联动（筛选栏 / 表单通用）
 * @param {import('vue').Ref} setupOptionsRef
 * @param {{ gradeRef?: import('vue').Ref, subjectRef?: import('vue').Ref, subjectsRef?: import('vue').Ref, filterMode?: boolean }} [bindings]
 */
export function useSetupStageCascade(setupOptionsRef, bindings = {}) {
  const {
    gradeRef = null,
    subjectRef = null,
    subjectsRef = null,
    filterMode = false,
  } = bindings

  const stageCode = ref(filterMode ? FILTER_SELECT_EMPTY : '')
  const emptyValue = filterMode ? FILTER_SELECT_EMPTY : ''

  const gradeStages = computed(() => {
    const fromApi = setupOptionsRef.value?.gradeStages || []
    if (fromApi.length) {
      return fromApi
    }
    return buildGradeStagesFromGrades(setupOptionsRef.value?.grades || [])
  })

  const allGrades = computed(() => setupOptionsRef.value?.grades || [])
  const allSubjects = computed(() => setupOptionsRef.value?.subjects || [])

  const filteredGrades = computed(() => {
    if (isFilterEmpty(stageCode.value)) {
      return allGrades.value
    }
    return filterGradesByStage(allGrades.value, stageCode.value)
  })

  const filteredSubjects = computed(() => {
    if (isFilterEmpty(stageCode.value)) {
      return allSubjects.value
    }
    return filterSubjectsByStage(allSubjects.value, stageCode.value)
  })

  function isEmpty(val) {
    return filterMode ? isFilterEmpty(val) : !val
  }

  function handleStageChange(nextStage = '') {
    stageCode.value = nextStage ?? emptyValue
    pruneGradeSelection()
    pruneSubjectSelection()
  }

  function pruneGradeSelection() {
    if (!gradeRef) {
      return
    }
    if (isEmpty(gradeRef.value)) {
      return
    }
    const valid = filteredGrades.value.some((item) => item.value === gradeRef.value)
    if (!valid) {
      gradeRef.value = emptyValue
    }
  }

  function pruneSubjectSelection() {
    if (subjectRef) {
      if (isEmpty(subjectRef.value)) {
        return
      }
      const valid = filteredSubjects.value.some(
        (item) => item.value === subjectRef.value || item.label === subjectRef.value,
      )
      if (!valid) {
        subjectRef.value = emptyValue
      }
    }
    if (subjectsRef && Array.isArray(subjectsRef.value)) {
      const allowed = new Set(filteredSubjects.value.map((item) => item.label))
      subjectsRef.value = subjectsRef.value.filter((name) => allowed.has(name))
    }
  }

  function syncStageFromGrade(gradeValue) {
    const code = resolveStageCodeFromGrade(allGrades.value, gradeValue)
    if (code) {
      stageCode.value = code
    }
  }

  function resetStage() {
    stageCode.value = emptyValue
  }

  return {
    stageCode,
    gradeStages,
    filteredGrades,
    filteredSubjects,
    handleStageChange,
    syncStageFromGrade,
    resetStage,
  }
}
