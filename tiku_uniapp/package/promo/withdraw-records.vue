<template>
	<view class="app-page records-page">
		<view class="screen records-screen">
			<view class="filter-card card">
				<text class="filter-label">审核状态</text>
				<view class="chip-row">
					<view
						v-for="item in statusFilters"
						:key="`status-${item.label}`"
						class="filter-chip"
						:class="{ active: selectedStatus === item.value }"
						@tap="selectStatus(item.value)"
					>
						<text>{{ item.label }}</text>
					</view>
				</view>
			</view>

			<view class="records-toolbar">
				<text class="subtle-text">共 {{ displayList.length }} 条记录</text>
				<text v-if="loading" class="toolbar-hint">加载中…</text>
				<text v-else-if="loadHint" class="toolbar-hint warn">{{ loadHint }}</text>
			</view>

			<view v-if="loading && !records.length" class="records-loading card">
				<text class="records-loading-text">正在加载提现记录…</text>
			</view>

			<view v-else-if="displayList.length" class="record-list">
				<view v-for="item in displayList" :key="item.id" class="record-card card">
					<view class="record-head">
						<view class="record-amount-wrap">
							<text class="record-amount">¥{{ item.amount }}</text>
							<text class="record-channel">{{ item.accountTypeLabel }}</text>
						</view>
						<text class="status-tag" :class="statusClass(item.status)">{{ item.statusLabel }}</text>
					</view>

					<view class="record-meta-row">
						<text class="meta-label">申请时间</text>
						<text class="meta-value">{{ item.createTime }}</text>
					</view>
					<view v-if="item.status === successStatus && item.withdrawTime" class="record-meta-row">
						<text class="meta-label">到账时间</text>
						<text class="meta-value">{{ item.withdrawTime }}</text>
					</view>
					<view v-if="item.actualAmount && item.actualAmount !== '0.00'" class="record-meta-row">
						<text class="meta-label">实际到账</text>
						<text class="meta-value">¥{{ item.actualAmount }}</text>
					</view>
					<view v-if="item.status === rejectedStatus && item.reason" class="reason-box">
						<text class="reason-label">拒绝原因</text>
						<text class="reason-text">{{ item.reason }}</text>
					</view>
				</view>

				<view
					v-if="hasMore && fromApi"
					class="load-more-btn"
					:class="{ disabled: loadingMore }"
					@tap="loadMore"
				>
					<text>{{ loadingMore ? '加载中…' : '加载更多' }}</text>
				</view>
			</view>

			<view v-else class="record-empty card">
				<text class="record-empty-text">暂无符合条件的提现记录</text>
				<text class="record-empty-sub">提交提现申请后，审核进度将显示在这里</text>
			</view>
		</view>
	</view>
</template>

<script setup>
	import { computed, ref } from 'vue';
	import { onShow } from '@dcloudio/uni-app';
	import {
		WITHDRAW_RECORD_STATUS,
		WITHDRAW_RECORD_STATUS_FILTERS,
		fetchWithdrawRecords
	} from '@/common/promo-api';

	const statusFilters = WITHDRAW_RECORD_STATUS_FILTERS;
	const successStatus = WITHDRAW_RECORD_STATUS.SUCCESS;
	const rejectedStatus = WITHDRAW_RECORD_STATUS.REJECTED;
	const pageLimit = 10;

	const records = ref([]);
	const selectedStatus = ref('');
	const loading = ref(false);
	const loadingMore = ref(false);
	const loadHint = ref('');
	const fromApi = ref(false);
	const currentPage = ref(1);
	const totalPages = ref(1);

	const displayList = computed(() => {
		if (selectedStatus.value === '') {
			return records.value;
		}
		return records.value.filter((item) => item.status === selectedStatus.value);
	});

	const hasMore = computed(() => fromApi.value && currentPage.value < totalPages.value);

	const statusClass = (status) => {
		if (status === WITHDRAW_RECORD_STATUS.SUCCESS) {
			return 'success';
		}
		if (status === WITHDRAW_RECORD_STATUS.REJECTED) {
			return 'rejected';
		}
		return 'pending';
	};

	const selectStatus = (value) => {
		selectedStatus.value = value;
	};

	const mergeRecords = (prevList, nextList, reset) => {
		if (reset) {
			return nextList;
		}
		const seen = new Set(prevList.map((item) => item.id));
		const merged = [...prevList];
		nextList.forEach((item) => {
			if (!seen.has(item.id)) {
				merged.push(item);
				seen.add(item.id);
			}
		});
		return merged;
	};

	const loadRecords = async ({ page = 1, append = false } = {}) => {
		if (append) {
			loadingMore.value = true;
		} else {
			loading.value = true;
		}

		try {
			const result = await fetchWithdrawRecords({ page, limit: pageLimit });
			records.value = mergeRecords(records.value, result.list || [], !append);
			fromApi.value = result.fromApi;
			currentPage.value = result.current || page;
			totalPages.value = result.pages || 1;
			loadHint.value = !result.fromApi && result.message ? result.message : '';
		} finally {
			loading.value = false;
			loadingMore.value = false;
		}
	};

	const reloadRecords = async () => {
		currentPage.value = 1;
		totalPages.value = 1;
		records.value = [];
		await loadRecords({ page: 1, append: false });
	};

	const loadMore = async () => {
		if (!hasMore.value || loadingMore.value || loading.value) {
			return;
		}
		await loadRecords({ page: currentPage.value + 1, append: true });
	};

	onShow(() => {
		reloadRecords();
	});
</script>

<style lang="scss" scoped>
	.records-page {
		background: linear-gradient(180deg, #F3F8FF 0, #F7FAFF 260rpx, #F7FAFE 620rpx, #F7FAFE 100%);
	}

	.records-screen {
		padding-top: 8rpx;
	}

	.filter-card {
		margin-top: 16rpx;
		padding: 24rpx 28rpx;
		background: linear-gradient(180deg, rgba(255, 255, 255, 0.98) 0%, rgba(248, 251, 255, 0.96) 100%);
		border: 1rpx solid rgba(121, 159, 229, 0.12);
		box-shadow: 0 14rpx 30rpx rgba(74, 108, 173, 0.08);
	}

	.filter-label {
		display: block;
		font-size: 30rpx;
		font-weight: 400;
		color: #5d6777;
	}

	.chip-row {
		margin-top: 16rpx;
		display: flex;
		flex-wrap: wrap;
		gap: 14rpx;
	}

	.filter-chip {
		padding: 10rpx 22rpx;
		border-radius: 999rpx;
		background: #f4f6fa;
		font-size: 24rpx;
		color: #5d6777;
	}

	.filter-chip.active {
		background: var(--primary-light);
		color: var(--primary);
		font-weight: 400;
	}

	.records-toolbar {
		margin-top: 20rpx;
		display: flex;
		align-items: center;
		justify-content: space-between;
		gap: 16rpx;
	}

	.subtle-text {
		font-size: 24rpx;
		color: #97a0af;
	}

	.toolbar-hint {
		font-size: 24rpx;
		color: #97a0af;
	}

	.toolbar-hint.warn {
		color: #f59e0b;
	}

	.records-loading,
	.record-empty {
		margin-top: 16rpx;
		padding: 48rpx 32rpx;
		text-align: center;
	}

	.records-loading-text,
	.record-empty-text {
		display: block;
		font-size: 30rpx;
		font-weight: 400;
		color: #253143;
	}

	.record-empty-sub {
		display: block;
		margin-top: 12rpx;
		font-size: 24rpx;
		color: #97a0af;
		line-height: 1.5;
	}

	.record-list {
		margin-top: 16rpx;
		display: flex;
		flex-direction: column;
		gap: 16rpx;
	}

	.record-card {
		padding: 28rpx;
		background: linear-gradient(180deg, rgba(255, 255, 255, 0.98) 0%, rgba(248, 251, 255, 0.96) 100%);
		border: 1rpx solid rgba(121, 159, 229, 0.1);
		box-shadow: 0 14rpx 30rpx rgba(74, 108, 173, 0.08);
	}

	.record-head {
		display: flex;
		align-items: flex-start;
		justify-content: space-between;
		gap: 16rpx;
	}

	.record-amount-wrap {
		display: flex;
		flex-direction: column;
		gap: 8rpx;
	}

	.record-amount {
		font-size: 40rpx;
		font-weight: 800;
		color: #253143;
	}

	.record-channel {
		font-size: 24rpx;
		color: #97a0af;
	}

	.status-tag {
		padding: 8rpx 14rpx;
		border-radius: var(--radius-sm);
		font-size: 24rpx;
		font-weight: 400;
		flex-shrink: 0;
	}

	.status-tag.pending {
		background: #fff7ed;
		color: #f59e0b;
	}

	.status-tag.success {
		background: var(--success-light);
		color: var(--success);
	}

	.status-tag.rejected {
		background: #fef2f2;
		color: #ef4444;
	}

	.record-meta-row {
		margin-top: 16rpx;
		display: flex;
		align-items: center;
		justify-content: space-between;
		gap: 16rpx;
	}

	.meta-label {
		font-size: 24rpx;
		color: #97a0af;
		flex-shrink: 0;
	}

	.meta-value {
		font-size: 24rpx;
		color: #253143;
		text-align: right;
	}

	.reason-box {
		margin-top: 18rpx;
		padding: 18rpx 20rpx;
		border-radius: var(--radius-lg);
		background: #fef2f2;
	}

	.reason-label {
		display: block;
		font-size: 24rpx;
		color: #ef4444;
		font-weight: 400;
	}

	.reason-text {
		display: block;
		margin-top: 8rpx;
		font-size: 24rpx;
		color: #7f1d1d;
		line-height: 1.6;
	}

	.load-more-btn {
		padding: 24rpx;
		text-align: center;
		font-size: 30rpx;
		color: var(--primary);
		font-weight: 400;
	}

	.load-more-btn.disabled {
		opacity: 0.6;
	}
</style>
