<script setup>
import CrudPage from '@/components/common/CrudPage.vue'
import { referralCommissionApi } from '@/api/modules/referral-commission'
import {
  REFERRAL_COMMISSION_DIRECTION_MAP,
  REFERRAL_COMMISSION_DIRECTION_OPTIONS,
  REFERRAL_COMMISSION_TYPE_MAP,
  REFERRAL_COMMISSION_TYPE_OPTIONS,
  USER_FILTER_FIELDS,
  formatEnumColumn,
} from '@/constants/admin-enums'

const filterFields = [
  ...USER_FILTER_FIELDS,
  { key: 'type', label: '类型', type: 'select', options: REFERRAL_COMMISSION_TYPE_OPTIONS, placeholder: '全部类型' },
  { key: 'direction', label: '方向', type: 'select', options: REFERRAL_COMMISSION_DIRECTION_OPTIONS, placeholder: '全部方向' },
]

const columns = [
  { prop: 'userId', label: '用户', minWidth: 120 },
  { prop: 'amount', label: '佣金', width: 100 },
  { prop: 'rate', label: '比例', width: 80 },
  { prop: 'type', label: '类型', width: 100, formatter: formatEnumColumn(REFERRAL_COMMISSION_TYPE_MAP) },
  { prop: 'direction', label: '方向', width: 90, formatter: formatEnumColumn(REFERRAL_COMMISSION_DIRECTION_MAP) },
  { prop: 'title', label: '标题', minWidth: 140, showOverflowTooltip: true },
  { prop: 'createTime', label: '创建时间', minWidth: 180 },
]

const detailFields = [
  { key: 'userName', label: '用户' },
  { key: 'phone', label: '手机号' },
  { key: 'amount', label: '佣金金额' },
  { key: 'rate', label: '佣金比例' },
  { key: 'type', label: '类型', formatter: (value) => formatEnumColumn(REFERRAL_COMMISSION_TYPE_MAP)(value) },
  { key: 'direction', label: '方向', formatter: (value) => formatEnumColumn(REFERRAL_COMMISSION_DIRECTION_MAP)(value) },
  { key: 'title', label: '标题' },
  { key: 'description', label: '描述' },
  { key: 'createTime', label: '创建时间' },
  { key: 'updateTime', label: '更新时间' },
]
</script>

<template>
  <CrudPage
    title="分销佣金"
    :api="referralCommissionApi"
    :columns="columns"
    :detail-fields="detailFields"
    :filter-fields="filterFields"
    :show-create="false"
  />
</template>
