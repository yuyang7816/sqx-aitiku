<template>
	<view class="app-page">
		<view class="page-scroll">
			<view class="screen order-screen">
				<view class="filter-card card">
					<view class="search-row">
						<up-icon name="search" size="16" color="#97a0af"></up-icon>
						<input
							v-model="orderNoKeyword"
							class="search-input"
							type="text"
							confirm-type="search"
							placeholder="搜索订单号"
							@confirm="reloadOrders"
						/>
						<text v-if="orderNoKeyword" class="search-clear" @tap="clearOrderNo">清除</text>
					</view>

					<view class="filter-group">
						<text class="filter-label">支付状态</text>
						<view class="chip-row">
							<view
								v-for="item in statusFilters"
								:key="`status-${item.label}`"
								class="filter-chip"
								:class="{ active: selectedPayStatus === item.value }"
								@tap="selectPayStatus(item.value)"
							>
								<text>{{ item.label }}</text>
							</view>
						</view>
					</view>

					<view class="filter-group">
						<text class="filter-label">套餐名称</text>
						<view class="chip-row">
							<view
								v-for="item in planFilters"
								:key="`plan-${item.label}`"
								class="filter-chip"
								:class="{ active: selectedPlanName === item.value }"
								@tap="selectPlanName(item.value)"
							>
								<text>{{ item.label }}</text>
							</view>
						</view>
					</view>
				</view>

				<view class="order-toolbar">
					<text class="subtle-text">共 {{ displayList.length }} 笔订单</text>
					<text v-if="loading" class="toolbar-hint">加载中…</text>
					<text v-else-if="loadHint" class="toolbar-hint warn">{{ loadHint }}</text>
				</view>

				<view v-if="loading" class="order-loading card">
					<text class="order-loading-text">正在加载历史订单…</text>
				</view>

				<view v-else-if="displayList.length" class="order-list">
					<view v-for="item in displayList" :key="item.orderNo" class="order-card card">
						<view class="order-card-head">
							<view class="order-plan-wrap">
								<text class="order-plan">至尊会员 · {{ item.planName }}</text>
								<text class="order-days">有效期 {{ item.planDays }} 天</text>
							</view>
							<text class="status-tag" :class="statusClass(item.payStatus)">{{ item.payStatusLabel }}</text>
						</view>

						<view class="order-meta-row">
							<text class="meta-label">订单号</text>
							<text class="meta-value">{{ item.orderNo }}</text>
						</view>
						<view class="order-meta-row">
							<text class="meta-label">支付方式</text>
							<text class="meta-value">{{ item.payChannelLabel }}</text>
						</view>
						<view class="order-meta-row">
							<text class="meta-label">下单时间</text>
							<text class="meta-value">{{ item.createTime || '—' }}</text>
						</view>
						<view v-if="item.payStatus === paidStatus && item.payTime" class="order-meta-row">
							<text class="meta-label">支付时间</text>
							<text class="meta-value">{{ item.payTime }}</text>
						</view>

						<view class="order-footer">
							<text class="order-amount">¥{{ formatAmount(item.amount) }}</text>
						</view>
					</view>
				</view>

				<view v-else class="order-empty card">
					<text class="order-empty-text">暂无符合条件的订单</text>
					<text class="order-empty-sub">开通会员后订单会出现在这里</text>
				</view>
			</view>
		</view>
	</view>
</template>

<script setup>
	import { computed, ref } from 'vue';
	import { onShow } from '@dcloudio/uni-app';
	import { MEMBER_ORDER_PAY_STATUS } from '@/common/member-constants';
	import {
		MEMBER_ORDER_PLAN_FILTERS,
		MEMBER_ORDER_STATUS_FILTERS,
		fetchMemberOrderList,
		filterMemberOrders,
		formatOrderAmount
	} from '@/common/vip-order-api';

	const statusFilters = MEMBER_ORDER_STATUS_FILTERS;
	const planFilters = MEMBER_ORDER_PLAN_FILTERS;
	const paidStatus = MEMBER_ORDER_PAY_STATUS.PAID;

	const orderNoKeyword = ref('');
	const selectedPayStatus = ref('');
	const selectedPlanName = ref('');
	const rawList = ref([]);
	const loading = ref(false);
	const loadHint = ref('');

	const currentFilters = computed(() => ({
		orderNo: orderNoKeyword.value,
		planName: selectedPlanName.value,
		payStatus: selectedPayStatus.value
	}));

	const displayList = computed(() => filterMemberOrders(rawList.value, currentFilters.value));

	const formatAmount = (amount) => formatOrderAmount(amount);

	const statusClass = (payStatus) => {
		if (Number(payStatus) === MEMBER_ORDER_PAY_STATUS.PAID) {
			return 'paid';
		}
		if (Number(payStatus) === MEMBER_ORDER_PAY_STATUS.PENDING) {
			return 'pending';
		}
		return 'closed';
	};

	const reloadOrders = async () => {
		loading.value = true;
		loadHint.value = '';
		const result = await fetchMemberOrderList(currentFilters.value);
		rawList.value = result.list || [];
		if (!result.fromApi && result.message) {
			loadHint.value = result.message;
		}
		loading.value = false;
	};

	const selectPayStatus = (value) => {
		selectedPayStatus.value = value;
		reloadOrders();
	};

	const selectPlanName = (value) => {
		selectedPlanName.value = value;
		reloadOrders();
	};

	const clearOrderNo = () => {
		orderNoKeyword.value = '';
		reloadOrders();
	};

	onShow(() => {
		reloadOrders();
	});
</script>

<style lang="scss" scoped>
	.order-screen {
		padding-top: 12rpx;
		padding-bottom: calc(24rpx + env(safe-area-inset-bottom));
	}

	.filter-card {
		margin-top: 16rpx;
		padding: 24rpx;
		display: flex;
		flex-direction: column;
		gap: 24rpx;
	}

	.search-row {
		display: flex;
		align-items: center;
		gap: 12rpx;
		padding: 18rpx 20rpx;
		border-radius: var(--radius-lg);
		background: #f6f8fc;
	}

	.search-input {
		flex: 1;
		min-width: 0;
		font-size: var(--text-base);
		color: var(--text-main);
	}

	.search-clear {
		font-size: var(--text-sm);
		color: var(--primary);
	}

	.filter-group {
		display: flex;
		flex-direction: column;
		gap: 14rpx;
	}

	.filter-label {
		font-size: var(--text-sm);
		color: #97a0af;
		font-weight: 600;
	}

	.chip-row {
		display: flex;
		flex-wrap: wrap;
		gap: 12rpx;
	}

	.filter-chip {
		padding: 12rpx 22rpx;
		border-radius: 999rpx;
		background: #f3f6fb;
		font-size: var(--text-sm);
		color: #5d6777;
		transition: all 0.15s ease;
	}

	.filter-chip.active {
		background: var(--primary-light);
		color: var(--primary);
		font-weight: 700;
	}

	.order-toolbar {
		margin-top: 20rpx;
		display: flex;
		align-items: center;
		justify-content: space-between;
		gap: 16rpx;
	}

	.subtle-text {
		font-size: var(--text-sm);
		color: #97a0af;
	}

	.toolbar-hint {
		font-size: var(--text-sm);
		color: var(--primary);
	}

	.toolbar-hint.warn {
		color: #d97706;
	}

	.order-loading,
	.order-empty {
		margin-top: 24rpx;
		padding: 48rpx 32rpx;
		display: flex;
		flex-direction: column;
		align-items: center;
		gap: 12rpx;
	}

	.order-loading-text,
	.order-empty-text {
		font-size: var(--text-lg);
		color: var(--text-main);
		font-weight: 700;
	}

	.order-empty-sub {
		font-size: var(--text-sm);
		color: #97a0af;
	}

	.order-list {
		margin-top: 24rpx;
		display: flex;
		flex-direction: column;
		gap: 18rpx;
	}

	.order-card {
		padding: 28rpx 24rpx;
	}

	.order-card-head {
		display: flex;
		align-items: flex-start;
		justify-content: space-between;
		gap: 16rpx;
	}

	.order-plan-wrap {
		flex: 1;
		min-width: 0;
	}

	.order-plan {
		display: block;
		font-size: var(--text-xl);
		font-weight: 800;
		color: var(--text-main);
	}

	.order-days {
		display: block;
		margin-top: 8rpx;
		font-size: var(--text-sm);
		color: #97a0af;
	}

	.status-tag {
		padding: 8rpx 16rpx;
		border-radius: 999rpx;
		font-size: var(--text-xs);
		font-weight: 700;
		flex-shrink: 0;
	}

	.status-tag.paid {
		background: rgba(34, 197, 94, 0.12);
		color: #16a34a;
	}

	.status-tag.pending {
		background: rgba(245, 158, 11, 0.12);
		color: #d97706;
	}

	.status-tag.closed {
		background: #f1f3f7;
		color: #97a0af;
	}

	.order-meta-row {
		margin-top: 16rpx;
		display: flex;
		align-items: flex-start;
		gap: 16rpx;
	}

	.meta-label {
		width: 128rpx;
		flex-shrink: 0;
		font-size: var(--text-sm);
		color: #97a0af;
	}

	.meta-value {
		flex: 1;
		min-width: 0;
		font-size: var(--text-sm);
		color: var(--text-main);
		word-break: break-all;
	}

	.order-footer {
		margin-top: 22rpx;
		padding-top: 22rpx;
		border-top: 2rpx solid #f1f3f7;
		display: flex;
		justify-content: flex-end;
	}

	.order-amount {
		font-size: 40rpx;
		font-weight: 800;
		color: var(--primary);
	}
</style>
