import { computed, ref } from 'vue'
import { defineStore } from 'pinia'

const TAB_KEY = 'admin-tabs'

function readTabs() {
  if (typeof window === 'undefined') {
    return []
  }
  const raw = window.sessionStorage.getItem(TAB_KEY)
  return raw ? JSON.parse(raw) : []
}

export const useAppStore = defineStore('app', () => {
  const collapsed = ref(false)
  const tabs = ref(readTabs())
  const tabPaths = computed(() => tabs.value.map((item) => item.path))

  function persistTabs() {
    window.sessionStorage.setItem(TAB_KEY, JSON.stringify(tabs.value))
  }

  function toggleCollapsed() {
    collapsed.value = !collapsed.value
  }

  function addTab(tab) {
    if (!tab.path || tabPaths.value.includes(tab.path)) {
      return
    }
    tabs.value.push(tab)
    persistTabs()
  }

  function removeTab(path) {
    tabs.value = tabs.value.filter((item) => item.path !== path)
    persistTabs()
  }

  function clearTabs(keepAffix = true) {
    tabs.value = keepAffix ? tabs.value.filter((item) => item.affix) : []
    persistTabs()
  }

  function closeOtherTabs(path) {
    tabs.value = tabs.value.filter((item) => item.affix || item.path === path)
    persistTabs()
  }

  function closeRightTabs(path) {
    const currentIndex = tabs.value.findIndex((item) => item.path === path)
    if (currentIndex === -1) {
      return
    }
    tabs.value = tabs.value.filter((item, index) => item.affix || index <= currentIndex)
    persistTabs()
  }

  return { collapsed, tabs, toggleCollapsed, addTab, removeTab, clearTabs, closeOtherTabs, closeRightTabs }
})
