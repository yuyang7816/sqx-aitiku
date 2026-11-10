<template>
	<view class="app-page">
		<view class="screen month-screen">
			<view class="report-hero">
				<view class="hero-topbar">
					<view class="hero-copy">
						<text class="hero-eyebrow">月度学习报告</text>
					</view>
					<view class="nav-history" @tap="openHistory">
						<up-icon name="list" size="16" color="#386df5"></up-icon>
						<text>历史</text>
					</view>
				</view>
				<view class="hero-copy">
					<text class="hero-title">{{ rankTitle }}</text>
					<text class="hero-sub">{{ rankSubtitle }}</text>
				</view>
				<view class="hero-stats">
					<view class="stat-card">
						<view class="stat-main">
							<text class="stat-label">本月正确率</text>
							<text class="stat-sub">{{ monthTitle }}</text>
						</view>
						<text class="stat-value blue">{{ correctRateText }}</text>
					</view>
					<view class="stat-card stat-card--soft">
						<view class="stat-main">
							<text class="stat-label">本月做题</text>
							<text class="stat-sub">{{ monthTitle }}</text>
						</view>
						<text class="stat-value orange">{{ monthQuestionCountText }}</text>
					</view>
				</view>
			</view>

			<view class="chart-section">
				<view class="section-head">
					<view>
						<text class="section-title">各学科正确率对比</text>
						<text class="section-sub">按学科横向查看当月掌握情况，短板会更直观。</text>
					</view>
					<view class="section-badge">
						<up-icon name="column-line" size="15" color="#386df5"></up-icon>
						<text>{{ bars.length }} 科</text>
					</view>
				</view>
				<view v-if="bars.length" class="chart-panel">
					<qiun-data-charts
						type="column"
						class="subject-chart"
						:chartData="chartData"
						:opts="chartOpts"
						:ontap="false"
					/>
				</view>
				<view v-else class="bars-empty">
					<text class="bars-empty-text">暂无学科练习数据</text>
				</view>
			</view>

			<view class="advice-section">
				<view class="section-head advice-head">
					<view>
						<text class="section-title">AI 学习建议</text>
						<text class="section-sub">结合本月数据整理的优先动作，先做高收益项。</text>
					</view>
					<view class="section-badge section-badge--violet">
						<up-icon name="kefu-ermai" size="15" color="#6d4aff"></up-icon>
						<text>{{ aiAdvices.length }} 条</text>
					</view>
				</view>
				<view v-if="adviceLoading" class="advice-loading">
					<text class="advice-loading-text">AI 正在分析你的学习数据…</text>
				</view>
				<view v-else class="advice-list">
					<view v-for="item in aiAdvices" :key="item.order" class="advice-item">
						<view class="advice-index">
							<text>{{ item.order }}</text>
						</view>
						<text class="advice-copy">{{ item.content }}</text>
					</view>
				</view>
				<view class="advice-actions">
					<view class="action-button action-button--primary" @tap="redoWrong">
						<text>今晚先重做错题</text>
					</view>
					<view class="action-button action-button--ghost" @tap="askPlan">
						<text>让 AI 帮我拆计划</text>
					</view>
				</view>
			</view>

			<view class="bottom-actions">
				<view class="ghost-button report-share" @tap="openCalendar">
					<up-icon name="calendar" size="16" color="#5d6777"></up-icon>
					<text>学习日历</text>
				</view>
				<view class="ghost-button report-share" @tap="share">
					<up-icon name="share" size="16" color="#5d6777"></up-icon>
					<text>分享海报</text>
				</view>
				<view class="solid-button report-home" @tap="home">
					<text>返回首页</text>
				</view>
			</view>
		</view>
	</view>
</template>

<script setup>
	import { computed, ref } from 'vue';
	import { onLoad, onShow } from '@dcloudio/uni-app';
	import {
		getAuthState,
		openReportAiPlan,
		openReportHistory,
		openReportHome,
		openReportSharePoster,
		openReportWrongPractice
	} from '@/common/app-data';
	import { usePageNavigationTitle } from '@/common/navigation-bar';
	import {
		fetchReportRank,
		REPORT_ADVICES_FALLBACK,
		REPORT_BARS_FALLBACK,
		REPORT_MONTH_TITLE_FALLBACK,
		REPORT_RANK_SUBTITLE_FALLBACK,
		REPORT_RANK_TITLE_FALLBACK
	} from '@/common/analysis-report';
	import { STUDY_CALENDAR_PAGE } from '@/common/routes';

	const monthTitle = ref(REPORT_MONTH_TITLE_FALLBACK);
	usePageNavigationTitle(monthTitle);
	const rankTitle = ref(REPORT_RANK_TITLE_FALLBACK);
	const rankSubtitle = ref(REPORT_RANK_SUBTITLE_FALLBACK);
	const selectedMonth = ref('');
	const monthQuestionCount = ref(0);
	const correctRate = ref(0);

	const bars = ref(REPORT_BARS_FALLBACK.map((item) => ({ ...item })));
	const aiAdvices = ref(REPORT_ADVICES_FALLBACK.map((item) => ({ ...item })));
	const adviceLoading = ref(false);

	const monthQuestionCountText = computed(() => `${monthQuestionCount.value} 道`);
	const correctRateText = computed(() => `${correctRate.value}%`);
	const chartData = computed(() => ({
		categories: bars.value.map((item) => item.label),
		series: [{
			name: '正确率',
			data: bars.value.map((item) => item.correctRate)
		}]
	}));
	const chartOpts = computed(() => ({
		color: ['#4C6FFF'],
		padding: [18, 12, 10, 6],
		fontSize: 11,
		dataLabel: true,
		legend: {
			show: false
		},
		extra: {
			column: {
				width: 18,
				borderRadius: [8, 8, 0, 0],
				linearType: 'custom',
				seriesGap: 10,
				categoryGap: 20
			}
		},
		xAxis: {
			disableGrid: true,
			fontColor: '#8b96aa',
			fontSize: 11,
			lineColor: '#e9eef6',
			marginTop: 8,
			rotateLabel: false
		},
		yAxis: {
			disabled: false,
			splitNumber: 3,
			gridType: 'solid',
			gridColor: '#e9eef6',
			data: [{
				min: 0,
				max: 100,
				fontColor: '#9aa4b3'
			}]
		}
	}));

	const loadReportRank = async () => {
		const auth = getAuthState();
		adviceLoading.value = auth.loggedIn;
		const month = String(selectedMonth.value || '').trim();
		const report = await fetchReportRank(month ? { month } : {});
		monthTitle.value = report.monthTitle || REPORT_MONTH_TITLE_FALLBACK;
		rankTitle.value = report.rankTitle || REPORT_RANK_TITLE_FALLBACK;
		rankSubtitle.value = report.rankSubtitle || REPORT_RANK_SUBTITLE_FALLBACK;
		monthQuestionCount.value = Number(report.monthQuestionCount) || 0;
		correctRate.value = Number(report.correctRate) || 0;
		bars.value = (report.subjectBars || REPORT_BARS_FALLBACK).map((item) => ({ ...item }));
		aiAdvices.value = (report.aiAdvices || REPORT_ADVICES_FALLBACK).map((item) => ({ ...item }));
		adviceLoading.value = false;
	};

	onLoad((options) => {
		selectedMonth.value = String(options?.month || '').trim();
	});

	onShow(() => {
		loadReportRank();
	});

	const home = () => openReportHome();
	const share = () => openReportSharePoster({ month: selectedMonth.value });
	const openCalendar = () => uni.navigateTo({ url: STUDY_CALENDAR_PAGE });
	const redoWrong = () => openReportWrongPractice();
	const openHistory = () => openReportHistory();
	const askPlan = () => openReportAiPlan({ monthTitle: monthTitle.value });
</script>

<style lang="scss" scoped>
	.nav-history {
		display: inline-flex;
		align-items: center;
		gap: 8rpx;
		height: 62rpx;
		padding: 0 18rpx;
		border-radius: 18rpx;
		background: rgba(239, 245, 255, 0.92);
		font-size: 18rpx;
		font-weight: 700;
		color: #386df5;
	}

	.month-screen {
		padding: 18rpx 24rpx 34rpx;
		background:
			radial-gradient(circle at top right, rgba(76, 111, 255, 0.06), transparent 28%),
			linear-gradient(180deg, #f8fafd 0%, #f2f5fa 100%);
		box-sizing: border-box;
	}

	.report-hero,
	.chart-section,
	.advice-section,
	.bottom-actions {
		margin-top: 16rpx;
		padding: 22rpx 20rpx;
		border-radius: 24rpx;
		background: rgba(255, 255, 255, 0.96);
		box-shadow: 0 8rpx 22rpx rgba(30, 46, 82, 0.028);
	}

	.report-hero {
		margin-top: 8rpx;
	}

	.hero-topbar {
		display: flex;
		align-items: flex-start;
		justify-content: space-between;
		gap: 16rpx;
	}

	.hero-eyebrow {
		display: block;
		font-size: 18rpx;
		font-weight: 700;
		line-height: 1.3;
		color: #4c6fff;
	}

	.hero-title {
		display: block;
		margin-top: 8rpx;
		font-size: 32rpx;
		font-weight: 800;
		line-height: 1.35;
		color: #1f2d3d;
	}

	.hero-sub {
		display: block;
		margin-top: 10rpx;
		font-size: 20rpx;
		line-height: 1.6;
		color: #76849a;
	}

	.hero-stats {
		margin-top: 18rpx;
		display: grid;
		grid-template-columns: repeat(2, minmax(0, 1fr));
		gap: 12rpx;
	}

	.stat-card {
		padding: 18rpx;
		border-radius: 18rpx;
		background: rgba(242, 246, 255, 0.9);
		display: flex;
		align-items: center;
		justify-content: space-between;
		gap: 14rpx;
	}

	.stat-card--soft {
		background: rgba(255, 247, 239, 0.92);
	}

	.stat-main {
		flex: 1;
		min-width: 0;
	}

	.stat-label {
		display: block;
		font-size: 18rpx;
		line-height: 1.35;
		color: #7e8a9f;
	}

	.stat-sub {
		display: block;
		margin-top: 8rpx;
		font-size: 16rpx;
		line-height: 1.35;
		color: #9aa4b3;
	}

	.stat-value {
		display: block;
		font-size: 34rpx;
		font-weight: 800;
		line-height: 1;
		flex-shrink: 0;
	}

	.stat-value.blue {
		color: #3a67eb;
	}

	.stat-value.orange {
		color: #eb7d24;
	}

	.section-head {
		display: flex;
		align-items: flex-start;
		justify-content: space-between;
		gap: 16rpx;
	}

	.section-title {
		display: block;
		font-size: 26rpx;
		font-weight: 700;
		line-height: 1.35;
		color: #213249;
	}

	.section-sub {
		display: block;
		margin-top: 6rpx;
		font-size: 18rpx;
		line-height: 1.55;
		color: #8592a6;
	}

	.section-badge {
		display: inline-flex;
		align-items: center;
		gap: 8rpx;
		padding: 8rpx 14rpx;
		border-radius: 999rpx;
		background: rgba(237, 244, 255, 0.84);
		font-size: 17rpx;
		font-weight: 700;
		line-height: 1.3;
		color: #386df5;
		flex-shrink: 0;
	}

	.section-badge--violet {
		background: rgba(244, 239, 255, 0.9);
		color: #6d4aff;
	}

	.chart-panel {
		margin-top: 18rpx;
	}

	.subject-chart {
		width: 100%;
		height: 360rpx;
		display: block;
	}

	.bars-empty {
		margin-top: 18rpx;
		height: 360rpx;
		display: flex;
		align-items: center;
		justify-content: center;
	}

	.bars-empty-text {
		font-size: 18rpx;
		color: #97a0af;
	}

	.advice-head {
		margin-bottom: 4rpx;
	}

	.advice-loading {
		margin-top: 20rpx;
		padding: 12rpx 0;
	}

	.advice-loading-text {
		font-size: 20rpx;
		line-height: 1.7;
		color: #7d889d;
	}

	.advice-list {
		margin-top: 18rpx;
		display: flex;
		flex-direction: column;
		gap: 12rpx;
	}

	.advice-item {
		padding: 16rpx;
		border-radius: 18rpx;
		background: linear-gradient(180deg, rgba(246, 242, 255, 0.96) 0%, rgba(242, 237, 255, 0.92) 100%);
		display: flex;
		align-items: flex-start;
		gap: 12rpx;
	}

	.advice-index {
		width: 40rpx;
		height: 40rpx;
		border-radius: 12rpx;
		background: rgba(109, 74, 255, 0.12);
		display: flex;
		align-items: center;
		justify-content: center;
		font-size: 18rpx;
		font-weight: 800;
		color: #6d4aff;
		flex-shrink: 0;
	}

	.advice-copy {
		flex: 1;
		font-size: 20rpx;
		line-height: 1.65;
		color: #32425d;
	}

	.advice-actions {
		margin-top: 18rpx;
		display: grid;
		grid-template-columns: repeat(2, minmax(0, 1fr));
		gap: 12rpx;
	}

	.action-button {
		height: 76rpx;
		border-radius: 18rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		font-size: 21rpx;
		font-weight: 700;
	}

	.action-button--primary {
		background: #4c6fff;
		color: #fff;
		box-shadow: 0 8rpx 18rpx rgba(76, 111, 255, 0.12);
	}

	.action-button--ghost {
		background: rgba(243, 247, 255, 0.96);
		color: #47618d;
	}

	.bottom-actions {
		display: grid;
		grid-template-columns: repeat(3, minmax(0, 1fr));
		gap: 12rpx;
	}

	.report-share,
	.report-home {
		width: 100%;
		height: 76rpx;
	}

	.report-share {
		display: inline-flex;
		align-items: center;
		justify-content: center;
		gap: 8rpx;
	}

	@media screen and (max-width: 420px) {
		.month-screen {
			padding: 16rpx 20rpx 28rpx;
		}

		.hero-title {
			font-size: 28rpx;
		}

		.advice-item {
			padding: 14rpx;
		}

		.bottom-actions {
			grid-template-columns: 1fr;
		}
	}

	/* #ifdef MP-WEIXIN */
	.hero-stats,
	.advice-actions,
	.bottom-actions {
		display: flex;
		flex-wrap: wrap;
		gap: 0;
		margin-left: -6rpx;
		margin-right: -6rpx;
	}

	.stat-card,
	.action-button {
		width: calc(50% - 12rpx);
		margin: 0 6rpx 12rpx;
	}

	.report-share,
	.report-home {
		width: calc(50% - 12rpx);
		margin: 0 6rpx 12rpx;
	}

	.report-home {
		width: calc(100% - 12rpx);
		background: #386df5;
		color: #ffffff;
	}

	.report-share text,
	.report-home text {
		display: block;
		line-height: 1;
		transform: translateY(-2rpx);
	}

	.report-share text,
	.report-home text {
		white-space: nowrap;
	}

	.report-home text {
		color: #ffffff;
	}
	/* #endif */
</style>
