<template>
	<view class="app-page">
		<view class="screen calendar-screen">
			<view class="calendar-hero">
				<view class="hero-topbar">
					<view class="month-switch" @tap="prevMonth">
						<up-icon name="arrow-left" size="15" color="#386df5"></up-icon>
					</view>
					<view class="month-trigger" @tap="openMonthPicker">
						<text class="month-title">{{ monthTitle }}</text>
						<up-icon name="arrow-down" size="14" color="#5d6f8f"></up-icon>
					</view>
					<view class="month-switch" :class="{ disabled: isCurrentMonth }" @tap="nextMonth">
						<up-icon name="arrow-right" size="15" :color="isCurrentMonth ? '#c0c8d8' : '#386df5'"></up-icon>
					</view>
				</view>
				<text class="hero-sub">点按年月可快速切换月份，蓝色日期代表该日有学习或打卡记录。</text>
				<view class="hero-stats">
					<view class="stat-card">
						<view class="stat-main">
							<text class="stat-label">连续打卡</text>
						</view>
						<text class="stat-value blue">{{ calendar.streakDays }}<text class="stat-unit">天</text></text>
					</view>
					<view class="stat-card stat-card--soft">
						<view class="stat-main">
							<text class="stat-label">本月学习</text>
						</view>
						<text class="stat-value orange">{{ calendar.monthStudyDays }}<text class="stat-unit">天</text></text>
					</view>
					<view class="stat-card stat-card--status">
						<view class="stat-main">
							<text class="stat-label">今日状态</text>
						</view>
						<text class="stat-value"
							:class="calendar.todayChecked ? 'green' : 'slate'">{{ calendar.todayChecked ? '已打卡' : '未打卡' }}</text>
					</view>
				</view>
			</view>

			<view v-if="loadError" class="notice-banner notice-banner--warn">
				<text>{{ loadError }}</text>
				<text class="notice-action" @tap="refreshCalendar">重试</text>
			</view>

			<view class="calendar-panel">
				<view class="panel-head">
					<view>
						<text class="panel-title">学习日历</text>
						<text class="panel-sub">点击日期可查看当日学习状态，边框标记为今天。</text>
					</view>
					<view class="panel-badge">
						<view class="badge-dot"></view>
						<text>{{ calendar.monthStudyDays }} 天</text>
					</view>
				</view>

				<view v-if="loading" class="panel-loading">
					<text>正在加载日历…</text>
				</view>
				<template v-else>
					<view class="week-row">
						<text v-for="label in weekLabels" :key="label" class="week-label">{{ label }}</text>
					</view>
					<view class="day-grid">
						<view
							v-for="(cell, index) in calendarCells"
							:key="`${cell.dateKey || 'empty'}-${index}`"
							class="day-cell"
							:class="{
								empty: cell.empty,
								active: cell.active,
								today: cell.isToday,
								selected: cell.dateKey && cell.dateKey === selectedDateKey
							}"
							@tap="onDayTap(cell)"
						>
							<text class="day-text">{{ cell.day }}</text>
						</view>
					</view>
					<view v-if="selectedDetailText" class="day-detail">
						<text>{{ selectedDetailText }}</text>
					</view>
				</template>
			</view>

			<view class="insight-panel">
				<view class="insight-card">
					<text class="insight-title">打卡说明</text>
					<text class="insight-copy">完成练习或手动打卡都会计入学习日历，连续记录越稳定，越容易看到长期学习轨迹。</text>
				</view>
				<view class="insight-card insight-card--accent">
					<text class="insight-title">本月建议</text>
					<text class="insight-copy">{{ monthAdviceText }}</text>
				</view>
			</view>

			<view class="quick-actions">
				<view class="quick-btn quick-btn--report" @tap="openReport">
					<up-icon name="file-text" size="16" color="#5d6777"></up-icon>
					<text>月度报告</text>
				</view>
				<view class="quick-btn quick-btn--practice" @tap="openPractice">
					<up-icon name="edit-pen" size="16" color="#18A058"></up-icon>
					<text>去刷题</text>
				</view>
			</view>

			<view
				class="solid-button check-btn"
				:class="{ disabled: calendar.todayChecked || checking || loading }"
				@tap="handleCheckIn"
			>
				<text>{{ checkButtonText }}</text>
			</view>
		</view>

		<view class="light-picker-theme" data-up-theme="light">
			<up-datetime-picker
				:show="showMonthPicker"
				mode="year-month"
				:title="'选择月份'"
				:model-value="monthPickerValue"
				:max-date="monthPickerMaxDate"
				@close="closeMonthPicker"
				@cancel="closeMonthPicker"
				@confirm="onMonthConfirm"
			></up-datetime-picker>
		</view>
	</view>
</template>

<script setup>
	import {
		computed,
		ref
	} from 'vue';
	import {
		onShow
	} from '@dcloudio/uni-app';
	import { usePageNavigationTitle } from '@/common/navigation-bar';
	import { PRACTICE_TAB_PAGE, REPORT_PAGE } from '@/common/routes';
	import {
		buildCalendarCells,
		buildDateDetailText,
		fetchStudyCalendar,
		formatMonthTitle,
		formatTodayKey,
		getCurrentMonthKey,
		isCurrentMonthKey,
		shiftMonthKey,
		submitStudyCheckIn
	} from '@/common/study-calendar';

	const weekLabels = ['日', '一', '二', '三', '四', '五', '六'];
	const monthKey = ref(getCurrentMonthKey());
	const showMonthPicker = ref(false);
	const loading = ref(false);
	const loadError = ref('');
	const selectedDateKey = ref(formatTodayKey());
	const calendar = ref({
		month: monthKey.value,
		activeDates: [],
		streakDays: 0,
		todayChecked: false,
		monthStudyDays: 0
	});
	const checking = ref(false);

	const monthTitle = computed(() => formatMonthTitle(monthKey.value));
	usePageNavigationTitle(monthTitle);
	const calendarCells = computed(() => buildCalendarCells(monthKey.value, calendar.value.activeDates));
	const monthPickerValue = computed(() => new Date(`${monthKey.value}-01T00:00:00`).getTime());
	const monthPickerMaxDate = computed(() => Date.now());
	const isCurrentMonth = computed(() => isCurrentMonthKey(monthKey.value));
	const selectedDetailText = computed(() => {
		const key = String(selectedDateKey.value || '').trim();
		if (!key) {
			return '';
		}
		const active = calendar.value.activeDates.includes(key);
		return buildDateDetailText(key, active);
	});
	const monthAdviceText = computed(() => {
		const days = calendar.value.monthStudyDays || 0;
		if (days >= 20) {
			return '本月学习节奏很好，继续保持每周 4～5 天的稳定频率即可。';
		}
		if (days >= 10) {
			return '已有不错的基础，再提高 2～3 天活跃天数，连续打卡会更容易坚持。';
		}
		return '优先保持连续性，再逐步提高每周活跃天数，比偶尔集中学习更容易坚持。';
	});
	const checkButtonText = computed(() => {
		if (checking.value) {
			return '打卡中...';
		}
		return calendar.value.todayChecked ? '今日已打卡' : '立即打卡';
	});

	const buildEmptyCalendar = () => ({
		month: monthKey.value,
		activeDates: [],
		streakDays: 0,
		todayChecked: false,
		monthStudyDays: 0
	});

	const refreshCalendar = async () => {
		loading.value = true;
		loadError.value = '';
		try {
			const result = await fetchStudyCalendar(monthKey.value);
			calendar.value = {
				...buildEmptyCalendar(),
				...result.calendar,
				month: result.calendar.month || monthKey.value
			};
			if (result.message) {
				loadError.value = result.message;
			}
		} catch (e) {
			calendar.value = buildEmptyCalendar();
			loadError.value = '日历加载异常，请稍后重试';
		} finally {
			loading.value = false;
		}
	};

	const prevMonth = () => {
		monthKey.value = shiftMonthKey(monthKey.value, -1);
		selectedDateKey.value = '';
		refreshCalendar();
	};

	const nextMonth = () => {
		if (isCurrentMonth.value) {
			return;
		}
		monthKey.value = shiftMonthKey(monthKey.value, 1);
		selectedDateKey.value = '';
		refreshCalendar();
	};

	const openMonthPicker = () => {
		showMonthPicker.value = true;
	};

	const closeMonthPicker = () => {
		showMonthPicker.value = false;
	};

	const onMonthConfirm = async (event = {}) => {
		const value = new Date(event.value || Date.now());
		if (Number.isNaN(value.getTime())) {
			closeMonthPicker();
			return;
		}
		monthKey.value = `${value.getFullYear()}-${String(value.getMonth() + 1).padStart(2, '0')}`;
		selectedDateKey.value = '';
		closeMonthPicker();
		await refreshCalendar();
	};

	const onDayTap = (cell = {}) => {
		if (cell.empty || !cell.dateKey) {
			return;
		}
		selectedDateKey.value = cell.dateKey;
	};

	const handleCheckIn = async () => {
		if (checking.value || calendar.value.todayChecked || loading.value) {
			return;
		}
		checking.value = true;
		try {
			const result = await submitStudyCheckIn();
			if (result.ok) {
				uni.showToast({
					title: result.newlyChecked ? '打卡成功' : '今日已打卡',
					icon: 'none'
				});
				selectedDateKey.value = formatTodayKey();
				await refreshCalendar();
				return;
			}
			uni.showToast({
				title: '打卡失败，请稍后重试',
				icon: 'none'
			});
		} finally {
			checking.value = false;
		}
	};

	const openReport = () => {
		uni.navigateTo({ url: REPORT_PAGE });
	};

	const openPractice = () => {
		uni.switchTab({ url: PRACTICE_TAB_PAGE });
	};

	onShow(() => {
		selectedDateKey.value = formatTodayKey();
		refreshCalendar();
	});
</script>

<style lang="scss" scoped>
	.calendar-screen {
		padding: 18rpx 24rpx 34rpx;
		background:
			radial-gradient(circle at top right, rgba(56, 109, 245, 0.05), transparent 28%),
			linear-gradient(180deg, #f8fafd 0%, #f2f5fa 100%);
		box-sizing: border-box;
	}

	.calendar-hero,
	.calendar-panel,
	.insight-panel {
		margin-top: 16rpx;
		padding: 22rpx 20rpx;
		border-radius: 24rpx;
		background: rgba(255, 255, 255, 0.96);
		box-shadow: 0 8rpx 22rpx rgba(29, 46, 82, 0.03);
	}

	.calendar-hero {
		margin-top: 8rpx;
	}

	.hero-topbar {
		display: flex;
		align-items: center;
		justify-content: space-between;
		gap: 12rpx;
	}

	.month-switch {
		width: 60rpx;
		height: 60rpx;
		border-radius: 18rpx;
		background: rgba(237, 244, 255, 0.9);
		display: flex;
		align-items: center;
		justify-content: center;
		flex-shrink: 0;
	}

	.month-switch.disabled {
		opacity: 0.45;
	}

	.month-trigger {
		flex: 1;
		min-width: 0;
		height: 60rpx;
		padding: 0 18rpx;
		border-radius: 18rpx;
		background: rgba(245, 248, 253, 0.96);
		display: flex;
		align-items: center;
		justify-content: center;
		gap: 8rpx;
	}

	.month-title {
		font-size: 28rpx;
		font-weight: 800;
		line-height: 1.3;
		color: #233247;
	}

	.hero-sub {
		display: block;
		margin-top: 14rpx;
		font-size: 18rpx;
		line-height: 1.55;
		color: #7c889c;
	}

	.hero-stats {
		margin-top: 18rpx;
		display: grid;
		grid-template-columns: repeat(3, minmax(0, 1fr));
		gap: 12rpx;
	}

	.stat-card {
		padding: 18rpx;
		border-radius: 18rpx;
		background: rgba(242, 246, 255, 0.9);
		display: flex;
		flex-direction: column;
		align-items: flex-start;
		justify-content: space-between;
		gap: 12rpx;
	}

	.stat-card--soft {
		background: rgba(255, 247, 239, 0.92);
	}

	.stat-card--status {
		background: rgba(242, 250, 245, 0.94);
	}

	.stat-main {
		flex: 1;
		min-width: 0;
	}

	.stat-label {
		display: block;
		font-size: 26rpx;
		line-height: 1.35;
		color: #76839a;
	}

	.stat-value {
		font-size: 32rpx;
		font-weight: 800;
		line-height: 1;
		flex-shrink: 0;
	}

	.stat-unit {
		margin-left: 4rpx;
		font-size: 22rpx;
		font-weight: 600;
	}

	.stat-value.blue {
		color: #386df5;
	}

	.stat-value.orange {
		color: #ea7d24;
	}

	.stat-value.green {
		color: #18a058;
	}

	.stat-value.slate {
		color: #5f6d82;
	}

	.notice-banner {
		margin-top: 14rpx;
		padding: 16rpx 18rpx;
		border-radius: 16rpx;
		display: flex;
		align-items: center;
		justify-content: space-between;
		gap: 12rpx;
		font-size: 22rpx;
		line-height: 1.5;
	}

	.notice-banner--warn {
		background: rgba(255, 247, 237, 0.96);
		color: #b45309;
	}

	.notice-action {
		flex-shrink: 0;
		color: #386df5;
		font-weight: 700;
	}

	.panel-head {
		display: flex;
		align-items: flex-start;
		justify-content: space-between;
		gap: 16rpx;
	}

	.panel-title {
		display: block;
		font-size: 26rpx;
		font-weight: 700;
		line-height: 1.35;
		color: #22324a;
	}

	.panel-sub {
		display: block;
		margin-top: 6rpx;
		font-size: 18rpx;
		line-height: 1.55;
		color: #8792a6;
	}

	.panel-badge {
		display: inline-flex;
		align-items: center;
		gap: 8rpx;
		padding: 8rpx 14rpx;
		border-radius: 999rpx;
		background: rgba(237, 244, 255, 0.85);
		font-size: 17rpx;
		font-weight: 700;
		line-height: 1.3;
		color: #386df5;
		flex-shrink: 0;
	}

	.badge-dot {
		width: 12rpx;
		height: 12rpx;
		border-radius: 999rpx;
		background: #386df5;
	}

	.panel-loading {
		margin-top: 24rpx;
		padding: 48rpx 0;
		text-align: center;
		font-size: 24rpx;
		color: #8792a6;
	}

	.week-row,
	.day-grid {
		display: grid;
		grid-template-columns: repeat(7, minmax(0, 1fr));
	}

	.week-row {
		margin-top: 20rpx;
	}

	.week-label,
	.day-cell {
		text-align: center;
	}

	.week-label {
		padding-bottom: 14rpx;
		font-size: 26rpx;
		color: #93a0b2;
	}

	.day-grid {
		row-gap: 8rpx;
	}

	.day-cell {
		height: 80rpx;
		display: flex;
		align-items: center;
		justify-content: center;
	}

	.day-text {
		width: 56rpx;
		height: 56rpx;
		border-radius: 18rpx;
		display: inline-flex;
		align-items: center;
		justify-content: center;
		font-size: 26rpx;
		line-height: 1;
		color: #344054;
	}

	.day-cell.empty {
		visibility: hidden;
	}

	.day-cell.active .day-text {
		background: rgba(56, 109, 245, 0.1);
		color: #386df5;
		font-weight: 700;
	}

	.day-cell.today .day-text {
		box-shadow: inset 0 0 0 2rpx #386df5;
	}

	.day-cell.selected .day-text {
		background: rgba(56, 109, 245, 0.18);
	}

	.day-detail {
		margin-top: 18rpx;
		padding: 16rpx 18rpx;
		border-radius: 16rpx;
		background: rgba(245, 248, 253, 0.96);
		font-size: 22rpx;
		line-height: 1.55;
		color: #5f6d82;
	}

	.insight-panel {
		display: grid;
		grid-template-columns: repeat(2, minmax(0, 1fr));
		gap: 12rpx;
	}

	.insight-card {
		padding: 18rpx;
		border-radius: 18rpx;
		background: rgba(247, 249, 253, 0.95);
	}

	.insight-card--accent {
		background: rgba(244, 240, 255, 0.92);
	}

	.insight-title {
		display: block;
		font-size: 26rpx;
		font-weight: 700;
		line-height: 1.35;
		color: #243349;
	}

	.insight-copy {
		display: block;
		margin-top: 10rpx;
		font-size: 26rpx;
		line-height: 1.6;
		color: #6f7d92;
	}

	.quick-actions {
		margin-top: 16rpx;
		display: grid;
		grid-template-columns: repeat(2, minmax(0, 1fr));
		gap: 12rpx;
	}

	.quick-btn {
		height: 76rpx;
		border-radius: 18rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		gap: 8rpx;
		font-size: 24rpx;
		font-weight: 600;
	}

	.quick-btn--report {
		background: #F6F8FC;
		border: 2rpx solid #E3E8F0;
		color: #5D6777;
		box-shadow: none;
	}

	.quick-btn--practice {
		background: #ECF9F1;
		border: 2rpx solid #B9E8CC;
		color: #18A058;
		box-shadow: 0 6rpx 14rpx rgba(24, 160, 88, 0.08);
	}

	.check-btn {
		margin-top: 18rpx;
		height: 84rpx;
		border-radius: 20rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		font-size: 26rpx;
		font-weight: 700;
		box-shadow: 0 10rpx 22rpx rgba(56, 109, 245, 0.12);
	}

	.check-btn.disabled {
		opacity: 0.58;
	}

	@media screen and (max-width: 420px) {
		.calendar-screen {
			padding: 16rpx 20rpx 28rpx;
		}

		.month-title {
			font-size: 26rpx;
		}

		.hero-stats {
			gap: 10rpx;
		}

		.stat-card {
			padding: 16rpx 14rpx;
		}

		.stat-value {
			font-size: 26rpx;
		}

		.panel-head {
			flex-direction: column;
		}

		.panel-badge {
			align-self: flex-start;
		}

		.insight-panel,
		.quick-actions {
			grid-template-columns: 1fr;
		}

		.day-cell {
			height: 72rpx;
		}

		.day-text {
			width: 52rpx;
			height: 52rpx;
			font-size: 26rpx;
		}
	}

	/* #ifdef MP-WEIXIN */
	.hero-stats,
	.insight-panel,
	.quick-actions {
		display: flex;
		flex-wrap: wrap;
		gap: 0;
		margin-left: -6rpx;
		margin-right: -6rpx;
	}

	.stat-card {
		width: calc(33.3333% - 12rpx);
		margin: 0 6rpx 12rpx;
	}

	.insight-card,
	.quick-btn {
		width: calc(50% - 12rpx);
		margin: 0 6rpx 12rpx;
	}
	/* #endif */
</style>
