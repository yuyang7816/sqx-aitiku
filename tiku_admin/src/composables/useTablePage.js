import { reactive } from 'vue'
import { buildPageParams, cloneData } from '@/utils/form'

export function useTablePage(initialFilters, fetcher, initialPageSize = 10) {
  const state = reactive({
    loading: false,
    list: [],
    total: 0,
    pageNum: 1,
    pageSize: initialPageSize,
    filters: cloneData(initialFilters),
  })

  async function load() {
    state.loading = true
    try {
      const data = await fetcher(buildPageParams(state.filters, state.pageNum, state.pageSize))
      state.list = data.list || []
      state.total = data.total || 0
    } finally {
      state.loading = false
    }
  }

  async function search() {
    state.pageNum = 1
    await load()
  }

  async function reset() {
    state.filters = cloneData(initialFilters)
    state.pageNum = 1
    await load()
  }

  async function handleSizeChange(size) {
    state.pageSize = size
    state.pageNum = 1
    await load()
  }

  async function handleCurrentChange(page) {
    state.pageNum = page
    await load()
  }

  return { state, load, search, reset, handleSizeChange, handleCurrentChange }
}
