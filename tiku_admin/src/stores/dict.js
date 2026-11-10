import { defineStore } from 'pinia'
import {
  categoryOptions,
  dataScopeOptions,
  decorationModuleOptions,
  dictTypeOptions,
  displayStatusOptions,
  memberLevelOptions,
  menuTypeOptions,
  roleOptions,
  sourceOptions,
  statusOptions,
} from '@/constants/options'

export const useDictStore = defineStore('dict', {
  state: () => ({
    statusOptions,
    displayStatusOptions,
    roleOptions,
    categoryOptions,
    decorationModuleOptions,
    dictTypeOptions,
    memberLevelOptions,
    sourceOptions,
    menuTypeOptions,
    dataScopeOptions,
  }),
})
