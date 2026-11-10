import { ref } from 'vue'

/** 筛选区重置后强制重渲染，恢复 placeholder 等默认提示 */
export function useFilterFormKey() {
  const filterFormKey = ref(0)

  function bumpFilterFormKey() {
    filterFormKey.value += 1
  }

  return { filterFormKey, bumpFilterFormKey }
}
