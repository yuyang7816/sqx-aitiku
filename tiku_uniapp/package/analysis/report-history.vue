<template>
	<view class="app-page">
		<view class="page-scroll">
			<view class="screen history-screen">
				<view class="history-hero">
					<view class="hero-head">
						<view class="hero-copy">
							<text class="hero-eyebrow">学习报告历史</text>
							<text class="hero-title">按月份回看阶段表现</text>
						</view>
						<view class="hero-filter-action" @tap="openFilterPopup">
							<up-icon name="calendar" size="15" color="#386df5"></up-icon>
							<text>{{ hasDateFilter ? '调整日期' : '日期筛选' }}</text>
						</view>
					</view>
					<view class="hero-meta-row">
						<view class="hero-meta-chip">
							<text>当前 {{ filteredHistoryList.length }} 条</text>
						</view>
						<view class="hero-meta-chip hero-meta-chip--soft">
							<text>累计 {{ historyList.length }} 个月</text>
						</view>
						<view v-if="hasDateFilter" class="hero-meta-chip hero-meta-chip--ghost" @tap="clearDateFilter">
							<text>清空筛选</text>
						</view>
					</view>
				</view>

				<view v-if="loading" class="history-loading">
					<view class="loading-mark">
						<up-icon name="reload" size="18" color="#386df5"></up-icon>
					</view>
					<text class="history-loading-text">正在汇总各月练习报告…</text>
					<text class="history-loading-sub">请稍候，系统正在按月份整理历史数据。</text>
				</view>

				<view v-else-if="groupedHistory.length" class="history-groups">
					<view v-for="group in groupedHistory" :key="group.year" class="history-group">
						<view class="group-head">
							<text class="group-year">{{ group.year }}</text>
							<text class="group-count">{{ group.items.length }} 个月报</text>
						</view>
						<view class="group-list">
							<view
								v-for="item in group.items"
								:key="item.month"
								class="history-card"
								@tap="openReport(item)"
							>
								<view class="month-badge">
									<text class="month-badge-num">{{ formatMonthNumber(item.month) }}</text>
									<text class="month-badge-unit">月报</text>
								</view>
								<view class="history-main">
									<view class="history-head">
										<text class="history-title">{{ item.monthTitle }}</text>
									</view>
									<text class="history-rank">{{ item.rankTitle }}</text>
									<view class="history-meta-row">
										<view class="rate-pill">
											<text>{{ item.correctRate }}%</text>
										</view>
										<view class="meta-chip">
											<up-icon name="edit-pen" size="13" color="#6c7a8f"></up-icon>
											<text>做题 {{ item.monthQuestionCount }} 道</text>
										</view>
										<view class="meta-chip meta-chip--muted">
											<up-icon name="clock" size="13" color="#90a0b7"></up-icon>
											<text>{{ item.month }}</text>
										</view>
									</view>
								</view>
								<view class="card-arrow">
									<up-icon name="arrow-right" size="14" color="#b1bccb"></up-icon>
								</view>
							</view>
						</view>
					</view>
				</view>

				<view v-else class="history-empty">
					<view class="empty-mark">
						<up-icon :name="hasDateFilter ? 'search' : 'file-text'" size="22" color="#7f95c4"></up-icon>
					</view>
					<text class="history-empty-text">{{ hasDateFilter ? '当前日期范围内暂无报告' : '暂无历史报告' }}</text>
					<text class="history-empty-sub">{{ hasDateFilter ? '试试放宽起止日期，或者清空筛选后查看全部月份。' : '完成练习后系统将按月生成学习报告。' }}</text>
					<view v-if="hasDateFilter" class="empty-action" @tap="clearDateFilter">
						<text>清空日期筛选</text>
					</view>
				</view>
			</view>
		</view>

		<up-popup :show="showFilterPopup" mode="bottom" round="18" @close="closeFilterPopup">
			<view class="filter-sheet">
				<view class="sheet-head">
					<text class="sheet-title">日期筛选</text>
					<text class="sheet-sub">按月报时间范围筛选历史记录</text>
				</view>
				<view class="sheet-date-row">
					<view class="sheet-date-trigger" @tap="openStartDatePicker">
						<text class="sheet-date-label">开始日期</text>
						<text class="sheet-date-value">{{ draftStartDate || '请选择' }}</text>
					</view>
					<view class="sheet-date-trigger" @tap="openEndDatePicker">
						<text class="sheet-date-label">结束日期</text>
						<text class="sheet-date-value">{{ draftEndDate || '请选择' }}</text>
					</view>
				</view>
				<view class="sheet-actions">
					<view class="sheet-action sheet-action--ghost" @tap="resetDraftDateFilter">
						<text>清空日期</text>
					</view>
					<view class="sheet-action sheet-action--primary" @tap="applyDateFilter">
						<text>应用筛选</text>
					</view>
				</view>
			</view>
		</up-popup>

		<view class="light-picker-theme" data-up-theme="light">
			<up-datetime-picker
				:show="showStartDatePicker"
				mode="date"
				:title="'选择开始日期'"
				:model-value="startDateValue"
				@close="closeStartDatePicker"
				@cancel="closeStartDatePicker"
				@confirm="onStartDateConfirm"
			></up-datetime-picker>
			<up-datetime-picker
				:show="showEndDatePicker"
				mode="date"
				:title="'选择结束日期'"
				:model-value="endDateValue"
				@close="closeEndDatePicker"
				@cancel="closeEndDatePicker"
				@confirm="onEndDateConfirm"
			></up-datetime-picker>
		</view>
	</view>
</template>

<script setup>
	import { computed, ref } from 'vue';
	import { onShow } from '@dcloudio/uni-app';
	import { openReportByMonth } from '@/common/app-data';
	import { buildReportHistoryFallback, fetchReportHistory } from '@/common/analysis-report';

	const historyList = ref(buildReportHistoryFallback());
	const loading = ref(false);
	const showFilterPopup = ref(false);
	const showStartDatePicker = ref(false);
	const showEndDatePicker = ref(false);
	const filterStartDate = ref('');
	const filterEndDate = ref('');
	const draftStartDate = ref('');
	const draftEndDate = ref('');

	const normalizeDateOnly = (value = '') => String(value || '').trim().slice(0, 10);
	const getMonthKeyFromDate = (value = '') => normalizeDateOnly(value).slice(0, 7);
	const formatPickerDate = (value) => {
		const date = new Date(value || Date.now());
		if (Number.isNaN(date.getTime())) {
			return '';
		}
		const pad = (num) => String(num).padStart(2, '0');
		return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`;
	};
	const formatMonthNumber = (month = '') => String(month || '').slice(5, 7) || '--';

	const startDateValue = computed(() => (
		draftStartDate.value ? new Date(`${draftStartDate.value}T00:00:00`).getTime() : Date.now()
	));
	const endDateValue = computed(() => (
		draftEndDate.value ? new Date(`${draftEndDate.value}T00:00:00`).getTime() : Date.now()
	));
	const hasDateFilter = computed(() => Boolean(filterStartDate.value || filterEndDate.value));
	const activeRangeLabel = computed(() => {
		if (!filterStartDate.value && !filterEndDate.value) {
			return '全部月份';
		}
		if (filterStartDate.value && filterEndDate.value) {
			return `${getMonthKeyFromDate(filterStartDate.value)} 至 ${getMonthKeyFromDate(filterEndDate.value)}`;
		}
		return filterStartDate.value
			? `${getMonthKeyFromDate(filterStartDate.value)} 之后`
			: `${getMonthKeyFromDate(filterEndDate.value)} 之前`;
	});

	const filteredHistoryList = computed(() => {
		const startMonth = getMonthKeyFromDate(filterStartDate.value);
		const endMonth = getMonthKeyFromDate(filterEndDate.value);
		if (!startMonth && !endMonth) {
			return historyList.value;
		}
		return historyList.value.filter((item) => {
			const month = String(item.month || '').trim();
			if (!month) {
				return false;
			}
			if (startMonth && month < startMonth) {
				return false;
			}
			if (endMonth && month > endMonth) {
				return false;
			}
			return true;
		});
	});

	const groupedHistory = computed(() => {
		const groupMap = new Map();
		filteredHistoryList.value.forEach((item) => {
			const year = String(item.month || '').slice(0, 4) || '未知年份';
			if (!groupMap.has(year)) {
				groupMap.set(year, []);
			}
			groupMap.get(year).push(item);
		});
		return Array.from(groupMap.entries()).map(([year, items]) => ({ year, items }));
	});

	const loadHistory = async () => {
		loading.value = true;
		try {
			const result = await fetchReportHistory({ limit: 12 });
			historyList.value = result.list || buildReportHistoryFallback();
		} finally {
			loading.value = false;
		}
	};

	onShow(() => {
		loadHistory();
	});

	const openReport = (item = {}) => {
		openReportByMonth(item.month);
	};

	const openFilterPopup = () => {
		draftStartDate.value = filterStartDate.value;
		draftEndDate.value = filterEndDate.value;
		showFilterPopup.value = true;
	};
	const closeFilterPopup = () => {
		showFilterPopup.value = false;
	};
	const openStartDatePicker = () => {
		showStartDatePicker.value = true;
	};
	const closeStartDatePicker = () => {
		showStartDatePicker.value = false;
	};
	const openEndDatePicker = () => {
		showEndDatePicker.value = true;
	};
	const closeEndDatePicker = () => {
		showEndDatePicker.value = false;
	};
	const onStartDateConfirm = (event = {}) => {
		draftStartDate.value = formatPickerDate(event.value);
		closeStartDatePicker();
	};
	const onEndDateConfirm = (event = {}) => {
		draftEndDate.value = formatPickerDate(event.value);
		closeEndDatePicker();
	};
	const resetDraftDateFilter = () => {
		draftStartDate.value = '';
		draftEndDate.value = '';
	};
	const clearDateFilter = () => {
		filterStartDate.value = '';
		filterEndDate.value = '';
		draftStartDate.value = '';
		draftEndDate.value = '';
		uni.showToast({ title: '已清空日期筛选', icon: 'none' });
	};
	const applyDateFilter = () => {
		if (draftStartDate.value && draftEndDate.value && draftStartDate.value > draftEndDate.value) {
			uni.showToast({ title: '开始日期不能晚于结束日期', icon: 'none' });
			return;
		}
		filterStartDate.value = draftStartDate.value;
		filterEndDate.value = draftEndDate.value;
		closeFilterPopup();
		uni.showToast({
			title: filterStartDate.value || filterEndDate.value ? '已应用日期筛选' : '已清空日期筛选',
			icon: 'none'
		});
	};
</script>

<style lang="scss" scoped>
	.history-screen {
		padding: 18rpx 24rpx 32rpx;
		background:
			radial-gradient(circle at top right, rgba(56, 109, 245, 0.05), transparent 30%),
			linear-gradient(180deg, #f8fafd 0%, #f2f5fa 100%);
		box-sizing: border-box;
	}

	.history-hero {
		padding: 20rpx 20rpx 18rpx;
		border-radius: 24rpx;
		background: linear-gradient(145deg, rgba(255, 255, 255, 0.98) 0%, rgba(247, 250, 255, 0.96) 100%);
		box-shadow: 0 8rpx 22rpx rgba(39, 60, 98, 0.03);
	}

	.hero-head {
		display: flex;
		align-items: flex-start;
		justify-content: space-between;
		gap: 16rpx;
	}

	.hero-filter-action {
		display: inline-flex;
		align-items: center;
		justify-content: center;
		gap: 8rpx;
		height: 64rpx;
		padding: 0 18rpx;
		border-radius: 18rpx;
		background: rgba(56, 109, 245, 0.08);
		font-size: 18rpx;
		font-weight: 700;
		color: #386df5;
		flex-shrink: 0;
	}

	.hero-eyebrow {
		display: block;
		font-size: 18rpx;
		font-weight: 700;
		line-height: 1.3;
		color: #386df5;
	}

	.hero-title {
		display: block;
		margin-top: 8rpx;
		font-size: 30rpx;
		font-weight: 800;
		line-height: 1.35;
		color: #1f2d3d;
	}

	.hero-meta-row {
		margin-top: 12rpx;
		display: flex;
		align-items: center;
		gap: 10rpx;
		flex-wrap: wrap;
	}

	.hero-meta-chip {
		display: inline-flex;
		align-items: center;
		padding: 8rpx 14rpx;
		border-radius: 999rpx;
		background: rgba(237, 244, 255, 0.75);
		font-size: 17rpx;
		line-height: 1.3;
		color: #5471aa;
	}

	.hero-meta-chip--soft {
		background: rgba(255, 255, 255, 0.9);
	}

	.hero-meta-chip--ghost {
		background: rgba(245, 248, 253, 0.96);
		color: #6f7d92;
	}

	.filter-panel,
	.history-loading,
	.history-empty,
	.history-group {
		margin-top: 16rpx;
		padding: 20rpx 18rpx;
		border-radius: 22rpx;
		background: rgba(255, 255, 255, 0.95);
		box-shadow: 0 6rpx 18rpx rgba(32, 49, 84, 0.025);
	}

	.filter-panel {
		padding: 18rpx 16rpx;
	}

	.filter-summary {
		min-width: 0;
	}

	.filter-badge {
		display: inline-flex;
		align-items: center;
		gap: 8rpx;
		padding: 10rpx 14rpx;
		border-radius: 999rpx;
		background: rgba(237, 244, 255, 0.8);
		font-size: 18rpx;
		font-weight: 700;
		line-height: 1.3;
		color: #386df5;
		max-width: 100%;
	}

	.filter-meta,
	.filter-loading {
		display: block;
		margin-top: 10rpx;
		font-size: 18rpx;
		line-height: 1.4;
		color: #8a96ab;
	}

	.filter-loading {
		color: #386df5;
	}

	.history-loading,
	.history-empty {
		align-items: center;
		text-align: center;
	}

	.loading-mark,
	.empty-mark {
		width: 72rpx;
		height: 72rpx;
		border-radius: 22rpx;
		background: rgba(237, 244, 255, 0.82);
		display: flex;
		align-items: center;
		justify-content: center;
		margin: 0 auto 14rpx;
	}

	.history-loading-text,
	.history-empty-text {
		font-size: 28rpx;
		font-weight: 700;
		line-height: 1.4;
		color: #233247;
	}

	.history-loading-sub,
	.history-empty-sub {
		margin-top: 10rpx;
		font-size: 20rpx;
		line-height: 1.6;
		color: #8592a7;
	}

	.empty-action {
		margin: 18rpx auto 0;
		padding: 0 24rpx;
		height: 68rpx;
		border-radius: 18rpx;
		background: rgba(56, 109, 245, 0.08);
		display: inline-flex;
		align-items: center;
		justify-content: center;
		font-size: 20rpx;
		font-weight: 700;
		color: #386df5;
	}

	.history-groups {
		display: flex;
		flex-direction: column;
		gap: 16rpx;
	}

	.group-head {
		display: flex;
		align-items: center;
		justify-content: space-between;
		gap: 12rpx;
	}

	.group-year {
		font-size: 26rpx;
		font-weight: 800;
		line-height: 1.3;
		color: #22324a;
	}

	.group-count {
		font-size: 18rpx;
		line-height: 1.4;
		color: #8a96ab;
	}

	.group-list {
		margin-top: 14rpx;
		display: flex;
		flex-direction: column;
		gap: 12rpx;
	}

	.history-card {
		padding: 16rpx;
		border-radius: 20rpx;
		background: linear-gradient(180deg, rgba(255, 255, 255, 0.98) 0%, rgba(248, 250, 254, 0.96) 100%);
		display: flex;
		align-items: center;
		gap: 16rpx;
		transition: transform 0.15s ease;
	}

	.history-card:active {
		transform: scale(0.992);
	}

	.month-badge {
		width: 100rpx;
		height: 100rpx;
		border-radius: 24rpx;
		background: rgba(56, 109, 245, 0.08);
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: center;
		flex-shrink: 0;
		box-sizing: border-box;
	}

	.month-badge-num {
		font-size: 34rpx;
		font-weight: 800;
		line-height: 1;
		color: #31539c;
	}

	.month-badge-unit {
		margin-top: 8rpx;
		font-size: 16rpx;
		line-height: 1.2;
		color: #6f80a1;
	}

	.history-main {
		flex: 1;
		min-width: 0;
	}

	.history-head {
		display: flex;
		align-items: center;
		justify-content: space-between;
		gap: 12rpx;
	}

	.history-title {
		flex: 1;
		min-width: 0;
		font-size: 24rpx;
		font-weight: 700;
		line-height: 1.45;
		color: #22324a;
	}

	.rate-pill {
		padding: 8rpx 12rpx;
		border-radius: 999rpx;
		background: rgba(255, 245, 232, 0.95);
		font-size: 18rpx;
		font-weight: 700;
		line-height: 1.2;
		color: #d97706;
		flex-shrink: 0;
	}

	.history-rank {
		display: block;
		margin-top: 8rpx;
		font-size: 20rpx;
		line-height: 1.55;
		color: #5f6d82;
	}

	.history-meta-row {
		margin-top: 12rpx;
		display: flex;
		align-items: center;
		gap: 10rpx;
		flex-wrap: wrap;
	}

	.meta-chip {
		display: inline-flex;
		align-items: center;
		justify-content: center;
		gap: 6rpx;
		padding: 8rpx 12rpx;
		border-radius: 999rpx;
		background: rgba(243, 247, 252, 0.95);
		font-size: 17rpx;
		line-height: 1.25;
		color: #667489;
	}

	.meta-chip :deep(.up-icon) {
		display: inline-flex;
		align-items: center;
		justify-content: center;
		line-height: 1;
		align-self: center;
	}

	.meta-chip--muted {
		color: #8a96ab;
	}

	.card-arrow {
		width: 40rpx;
		height: 40rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		flex-shrink: 0;
	}

	.filter-sheet {
		padding: 28rpx 24rpx 34rpx;
		background: #ffffff;
	}

	.sheet-head {
		text-align: left;
	}

	.sheet-title {
		display: block;
		font-size: 30rpx;
		font-weight: 800;
		line-height: 1.35;
		color: #1f2d3d;
	}

	.sheet-sub {
		display: block;
		margin-top: 8rpx;
		font-size: 20rpx;
		line-height: 1.55;
		color: #7d889d;
	}

	.sheet-date-row {
		margin-top: 22rpx;
		display: flex;
		flex-direction: column;
		gap: 12rpx;
	}

	.sheet-date-trigger {
		padding: 20rpx 18rpx;
		border-radius: 18rpx;
		background: #f7f9fc;
	}

	.sheet-date-label {
		display: block;
		font-size: 18rpx;
		line-height: 1.3;
		color: #8692a7;
	}

	.sheet-date-value {
		display: block;
		margin-top: 8rpx;
		font-size: 24rpx;
		font-weight: 700;
		line-height: 1.4;
		color: #213249;
	}

	.sheet-actions {
		margin-top: 22rpx;
		display: grid;
		grid-template-columns: repeat(2, minmax(0, 1fr));
		gap: 12rpx;
	}

	.sheet-action {
		height: 78rpx;
		border-radius: 18rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		font-size: 22rpx;
		font-weight: 700;
	}

	.sheet-action--ghost {
		background: #f3f6fb;
		color: #6f7d92;
	}

	.sheet-action--primary {
		background: linear-gradient(135deg, #4f7df7 0%, #386df5 100%);
		color: #ffffff;
		box-shadow: 0 8rpx 18rpx rgba(56, 109, 245, 0.12);
	}

	@media screen and (max-width: 420px) {
		.history-screen {
			padding: 16rpx 20rpx 28rpx;
		}

		.hero-title {
			font-size: 28rpx;
		}

		.history-loading-sub,
		.history-empty-sub,
		.sheet-sub {
			font-size: 18rpx;
		}

		.filter-panel {
			padding: 16rpx;
		}

		.hero-head {
			align-items: flex-start;
		}

		.hero-filter-action {
			height: 60rpx;
			padding: 0 16rpx;
		}

		.history-card {
			align-items: center;
		}

		.month-badge {
			width: 88rpx;
			height: 88rpx;
			border-radius: 22rpx;
		}

		.month-badge-num {
			font-size: 30rpx;
		}

		.history-head {
			align-items: flex-start;
			flex-direction: column;
		}

		.rate-pill {
			align-self: flex-start;
		}

		.sheet-actions {
			grid-template-columns: 1fr;
		}
	}

	/* #ifdef MP-WEIXIN */
	.sheet-actions {
		display: flex;
		flex-wrap: wrap;
		gap: 0;
		margin-left: -6rpx;
		margin-right: -6rpx;
	}

	.sheet-action {
		width: calc(50% - 12rpx);
		margin: 0 6rpx 12rpx;
	}

	@media screen and (max-width: 420px) {
		.sheet-action {
			width: calc(100% - 12rpx);
		}
	}
	/* #endif */
</style>
