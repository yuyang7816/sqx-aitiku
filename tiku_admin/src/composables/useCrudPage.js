import { onMounted, reactive, ref } from 'vue'
import { confirmAction, showError, showSuccess } from '@/utils/modal'
import { FILTER_SELECT_EMPTY, isFilterEmpty } from '@/utils/filter-form'

function buildInitialFilters(filterFields = []) {
  const initial = {}
  filterFields.forEach((field) => {
    if (field.default !== undefined) {
      initial[field.key] = field.default
      return
    }
    initial[field.key] = field.type === 'select' ? FILTER_SELECT_EMPTY : ''
  })
  return initial
}

export function useCrudPage(api, {
  defaultQuery = {},
  idKey = 'id',
  detailFields = [],
  filterFields = [],
} = {}) {
  const loading = ref(false)
  const dialogVisible = ref(false)
  const dialogLoading = ref(false)
  const detailVisible = ref(false)
  const detailLoading = ref(false)
  const detailData = ref(null)
  const tableData = ref([])
  const total = ref(0)
  const pageNum = ref(1)
  const pageSize = ref(10)
  const editingId = ref(null)
  const form = reactive({})
  const filters = reactive(buildInitialFilters(filterFields))

  function buildQuery(extra = {}) {
    const query = {
      page: pageNum.value,
      limit: pageSize.value,
      ...defaultQuery,
      ...extra,
    }
    filterFields.forEach((field) => {
      const value = filters[field.key]
      if (isFilterEmpty(value)) {
        return
      }
      query[field.key] = value
    })
    return query
  }

  async function loadData(extra = {}) {
    loading.value = true
    try {
      const data = await api.page(buildQuery(extra))
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

  function handleResetFilters() {
    Object.assign(filters, buildInitialFilters(filterFields))
    pageNum.value = 1
    loadData()
  }

  function openCreate(initial = {}) {
    editingId.value = null
    Object.keys(form).forEach((key) => delete form[key])
    Object.assign(form, initial)
    dialogVisible.value = true
  }

  async function openDetail(row) {
    detailVisible.value = true
    detailLoading.value = true
    detailData.value = null
    try {
      if (typeof api.info === 'function') {
        detailData.value = await api.info(row[idKey])
      } else {
        detailData.value = { ...row }
      }
    } catch (error) {
      showError(error?.msg || error?.message || '加载详情失败')
      detailVisible.value = false
    } finally {
      detailLoading.value = false
    }
  }

  async function openEdit(row) {
    editingId.value = row[idKey]
    dialogLoading.value = true
    dialogVisible.value = true
    try {
      const data = await api.info(row[idKey])
      Object.keys(form).forEach((key) => delete form[key])
      Object.assign(form, data || row)
    } catch (error) {
      showError(error?.msg || error?.message || '加载详情失败')
      dialogVisible.value = false
    } finally {
      dialogLoading.value = false
    }
  }

  async function submitForm(extra = {}) {
    dialogLoading.value = true
    try {
      const payload = { ...form, ...extra }
      if (editingId.value) {
        await api.update({ ...payload, [idKey]: editingId.value })
        showSuccess('更新成功')
      } else {
        await api.save(payload)
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
    await confirmAction('确认删除该记录吗？')
    await api.delete({ [idKey]: row[idKey] })
    showSuccess('删除成功')
    await loadData()
  }

  onMounted(() => loadData())

  return {
    loading,
    dialogVisible,
    dialogLoading,
    detailVisible,
    detailLoading,
    detailData,
    detailFields,
    tableData,
    total,
    pageNum,
    pageSize,
    editingId,
    form,
    filters,
    loadData,
    handleSearch,
    handleResetFilters,
    openCreate,
    openDetail,
    openEdit,
    submitForm,
    removeRow,
  }
}
