<template>
	<view class="app-page">
		<view class="page-scroll">
			<view class="screen report-screen">
				<view class="overview-hero">
					<view class="hero-copy">
						<text class="hero-eyebrow">学习概览</text>
						<text class="hero-title">本周练习数据</text>
					</view>
					<view class="summary-grid">
						<view class="summary-card summary-card--blue">
							<view class="summary-main">
								<view class="summary-head">
									<view class="summary-icon blue">
										<up-icon name="edit-pen" size="18" color="#386df5"></up-icon>
									</view>
									<text class="small-title">本周做题</text>
								</view>
								<text class="sub" :class="weekQuestionChangeTrend === 'down' ? 'red' : 'green'">{{ weekQuestionChangeText }}</text>
							</view>
							<text class="big blue">{{ weekQuestionCount }}</text>
						</view>
						<view class="summary-card summary-card--orange">
							<view class="summary-main">
								<view class="summary-head">
									<view class="summary-icon orange">
										<up-icon name="checkmark-circle" size="18" color="#ff7a17"></up-icon>
									</view>
									<text class="small-title">平均正确率</text>
								</view>
								<text class="sub" :class="correctRateChangeTrend === 'down' ? 'red' : 'green'">{{ correctRateChangeText }}</text>
							</view>
							<text class="big orange">{{ correctRateText }}</text>
						</view>
					</view>
				</view>

				<view class="chart-card">
					<view class="section-head">
						<view>
							<text class="section-title">能力维度雷达图</text>
							<text class="section-sub">各维度得分越接近外圈，代表当前掌握越稳定。</text>
						</view>
						<view class="section-badge">
							<up-icon name="column-line" size="16" color="#386df5"></up-icon>
							<text>{{ radarDimensions.length }} 项</text>
						</view>
					</view>
					<analysis-radar-chart :dimensions="radarDimensions" />
				</view>

				<view class="weak-section">
					<view class="section-head weak-section-head">
						<view>
							<text class="section-title">薄弱知识点</text>
							<text class="section-sub">优先处理正确率最低的知识点，短期提分通常最直接。</text>
						</view>
						<view class="section-badge section-badge--warn">
							<up-icon name="warning-fill" size="16" color="#ef4444"></up-icon>
							<text>Top {{ weakPoints.length }}</text>
						</view>
					</view>
					<view class="weak-list">
						<view v-for="item in weakPoints" :key="item.title" class="weak-card">
							<view class="weak-main">
								<view class="weak-row">
									<view class="weak-icon">
										<up-icon name="warning-fill" size="15" color="#EF4444"></up-icon>
									</view>
									<text class="weak-name">{{ item.title }}</text>
									<text class="weak-rate">{{ item.percent }} 正确率</text>
								</view>
								<view class="weak-track">
									<view class="weak-bar" :style="{ width: item.percent }"></view>
								</view>
							</view>
							<view class="weak-link" @tap.stop="openWeakAction(item)">
								<text>去强化</text>
								<up-icon name="arrow-right" size="14" color="#386df5"></up-icon>
							</view>
						</view>
					</view>
				</view>

				<view class="action-panel">
					<view class="solid-button action-primary" @tap="go('/package/analysis/report')">
						<text>生成完整学习报告</text>
					</view>
					<view class="ghost-button history-entry" @tap="go('/package/analysis/calendar')">
						<text>查看学习日历</text>
					</view>
					<view class="ghost-button history-entry" @tap="go('/package/analysis/report-history')">
						<text>查看历史报告</text>
					</view>
				</view>
			</view>
		</view>
	</view>
</template>

<script setup>
	import { ref } from 'vue';
	import { onShow } from '@dcloudio/uni-app';	import AnalysisRadarChart from '@/components/analysis-radar-chart/analysis-radar-chart.vue';
	import {
		fetchOverviewAnalysis
	} from '@/common/analysis-overview';
	import {
		openWeakPointAnalysisChat,
		openWeakPointOcrPractice,
		openWeakPointWrongPractice
	} from '@/common/app-data';

	const weakPoints = ref([]);
	const weekQuestionCount = ref(0);
	const weekQuestionChangeText = ref('与上周持平');
	const weekQuestionChangeTrend = ref('flat');
	const correctRateText = ref('0%');
	const correctRateChangeText = ref('与上周持平');
	const correctRateChangeTrend = ref('flat');
	const radarDimensions = ref([]);
	const overviewLoading = ref(false);

	const refreshOverview = async () => {
		overviewLoading.value = true;
		try {
			const overview = await fetchOverviewAnalysis();
			weekQuestionCount.value = overview.weekQuestionCount ?? 0;
			weekQuestionChangeText.value = overview.weekQuestionChangeText || '与上周持平';
			weekQuestionChangeTrend.value = overview.weekQuestionChangeTrend || 'flat';
			const rate = overview.correctRate ?? 0;
			correctRateText.value = `${rate}%`;
			correctRateChangeText.value = overview.correctRateChangeText || '与上周持平';
			correctRateChangeTrend.value = overview.correctRateChangeTrend || 'flat';
			radarDimensions.value = (overview.radarDimensions || []).map((item) => ({ ...item }));
			weakPoints.value = (overview.weakPoints || []).map((item) => ({ ...item }));
		} finally {
			overviewLoading.value = false;
		}
	};

	onShow(() => {
		refreshOverview();
	});
	const go = (url) => uni.navigateTo({ url });
	const openWeakAction = (item) => {
		if (item.action === 'wrong-practice') {
			openWeakPointWrongPractice(item);
			return;
		}
		if (item.action === 'analysis-chat') {
			openWeakPointAnalysisChat(item);
			return;
		}
		if (item.action === 'ocr-practice') {
			openWeakPointOcrPractice(item);
		}
	};
</script>

<style lang="scss" scoped>
	.report-screen {
		padding: 24rpx 24rpx 36rpx;
		background:
			radial-gradient(circle at top right, rgba(99, 136, 255, 0.05), transparent 30%),
			linear-gradient(180deg, #fafbfd 0%, #f4f7fb 100%);
		box-sizing: border-box;
	}

	.overview-hero {
		padding: 18rpx 18rpx 16rpx;
		border-radius: 22rpx;
		background: linear-gradient(145deg, rgba(255, 255, 255, 0.985) 0%, rgba(250, 251, 254, 0.97) 100%);
		box-shadow: 0 4rpx 14rpx rgba(52, 74, 118, 0.022);
		border: 1rpx solid rgba(231, 235, 241, 0.6);
	}

	.hero-eyebrow {
		display: block;
		font-size: 18rpx;
		font-weight: 700;
		color: #386df5;
		line-height: 1.3;
	}

	.hero-title {
		display: block;
		margin-top: 6rpx;
		font-size: 28rpx;
		font-weight: 800;
		line-height: 1.3;
		color: #1f2d3d;
	}

	.summary-grid {
		margin-top: 14rpx;
		display: grid;
		grid-template-columns: repeat(2, 1fr);
		gap: 12rpx;
	}

	.summary-card {
		padding: 16rpx 18rpx;
		border-radius: 18rpx;
		background: rgba(255, 255, 255, 0.9);
		box-shadow:
			inset 0 0 0 1rpx rgba(231, 236, 244, 0.68),
			0 4rpx 12rpx rgba(44, 63, 102, 0.018);
		display: flex;
		align-items: center;
		justify-content: space-between;
		gap: 16rpx;
	}

	.summary-main {
		flex: 1;
		min-width: 0;
	}

	.summary-head {
		display: flex;
		align-items: center;
		gap: 10rpx;
		min-width: 0;
	}

	.summary-card--blue {
		background: linear-gradient(180deg, rgba(255, 255, 255, 0.98) 0%, rgba(248, 250, 255, 0.95) 100%);
	}

	.summary-card--orange {
		background: linear-gradient(180deg, rgba(255, 255, 255, 0.98) 0%, rgba(255, 250, 244, 0.95) 100%);
	}

	.summary-icon {
		width: 42rpx;
		height: 42rpx;
		border-radius: 12rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		flex-shrink: 0;
	}

	.summary-icon.blue {
		background: rgba(56, 109, 245, 0.1);
	}

	.summary-icon.orange {
		background: rgba(255, 122, 23, 0.1);
	}

	.small-title {
		font-size: 20rpx;
		line-height: 1.4;
		color: #7f8a9d;
	}

	.big {
		display: block;
		font-size: 34rpx;
		font-weight: 800;
		line-height: 1;
		flex-shrink: 0;
	}

	.big.blue { color: var(--primary); }
	.big.orange { color: #ff7a17; }
	.sub { margin-top: 8rpx; font-size: 18rpx; line-height: 1.35; }
	.sub.green { color: var(--success); }
	.sub.red { color: var(--error); }

	.chart-card,
	.weak-section,
	.action-panel {
		margin-top: 16rpx;
		padding: 20rpx 18rpx;
		border-radius: 22rpx;
		background: rgba(255, 255, 255, 0.95);
		box-shadow: 0 4rpx 14rpx rgba(28, 48, 92, 0.022);
		border: 1rpx solid rgba(228, 232, 239, 0.64);
	}

	.section-head {
		display: flex;
		align-items: flex-start;
		justify-content: space-between;
		gap: 16rpx;
	}

	.section-title {
		display: block;
		font-size: 24rpx;
		font-weight: 700;
		line-height: 1.35;
		color: #1f2d3d;
	}

	.section-sub {
		display: block;
		margin-top: 6rpx;
		font-size: 18rpx;
		line-height: 1.55;
		color: #748197;
	}

	.section-badge {
		display: inline-flex;
		align-items: center;
		gap: 8rpx;
		padding: 8rpx 14rpx;
		border-radius: 999rpx;
		background: rgba(237, 244, 255, 0.7);
		font-size: 18rpx;
		font-weight: 700;
		line-height: 1.3;
		color: #386df5;
		flex-shrink: 0;
	}

	.section-badge--warn {
		background: rgba(255, 241, 241, 0.76);
		color: #ef4444;
	}

	.weak-section-head {
		margin-bottom: 14rpx;
	}

	.weak-list {
		display: flex;
		flex-direction: column;
		gap: 12rpx;
	}

	.weak-card {
		padding: 16rpx;
		border-radius: 18rpx;
		background: linear-gradient(180deg, rgba(255, 250, 250, 0.98) 0%, rgba(255, 246, 246, 0.95) 100%);
		border: 1rpx solid rgba(255, 224, 224, 0.66);
		box-shadow: 0 3rpx 8rpx rgba(175, 54, 54, 0.015);
		display: flex;
		align-items: center;
		gap: 16rpx;
		transition: transform 0.15s ease;
	}

	.weak-card:active {
		transform: scale(0.99);
	}

	.weak-main {
		flex: 1;
		min-width: 0;
	}

	.weak-row {
		display: flex;
		align-items: center;
		gap: 10rpx;
	}

	.weak-icon {
		width: 40rpx;
		height: 40rpx;
		border-radius: 12rpx;
		background: rgba(255, 255, 255, 0.9);
		display: flex;
		align-items: center;
		justify-content: center;
		flex-shrink: 0;
	}

	.weak-name {
		flex: 1;
		min-width: 0;
		font-size: 22rpx;
		font-weight: 700;
		color: #af3636;
		line-height: 1.45;
	}

	.weak-track {
		margin-top: 12rpx;
		height: 10rpx;
		border-radius: var(--radius-full);
		background: rgba(255, 208, 208, 0.72);
		overflow: hidden;
	}

	.weak-bar {
		height: 100%;
		border-radius: inherit;
		background: var(--error);
	}

	.weak-rate {
		font-size: 17rpx;
		color: var(--error);
		font-weight: 700;
		flex-shrink: 0;
	}

	.weak-link {
		display: inline-flex;
		align-items: center;
		gap: 6rpx;
		padding: 8rpx 12rpx;
		font-size: 18rpx;
		color: var(--primary);
		font-weight: 700;
		flex-shrink: 0;
		border-radius: 999rpx;
		background: rgba(56, 109, 245, 0.045);
	}

	.action-panel {
		padding: 18rpx;
		background: rgba(255, 255, 255, 0.9);
	}

	.action-primary {
		height: 76rpx;
		font-size: 24rpx;
		box-shadow: 0 4rpx 12rpx rgba(56, 109, 245, 0.09);
		opacity: 0.96;
	}

	.history-entry {
		margin-top: 12rpx;
		height: 72rpx;
		font-size: 22rpx;
		background: rgba(245, 248, 255, 0.92);
		border: 1rpx solid rgba(203, 216, 240, 0.58);
		box-shadow: 0 2rpx 8rpx rgba(56, 109, 245, 0.02);
		color: #4a5f8d;
	}

	@media screen and (max-width: 420px) {
		.section-head {
			flex-direction: column;
		}

		.section-badge {
			align-self: flex-start;
		}

		.weak-card {
			align-items: flex-start;
		}

		.weak-row {
			align-items: flex-start;
			flex-wrap: wrap;
		}

		.weak-rate {
			width: 100%;
			padding-left: 60rpx;
		}

		.summary-card {
			padding: 14rpx 16rpx;
			gap: 12rpx;
		}

		.big {
			font-size: 30rpx;
		}
	}

	/* #ifdef MP-WEIXIN */
	.summary-grid {
		display: flex;
		flex-wrap: wrap;
		gap: 0;
		margin-left: -6rpx;
		margin-right: -6rpx;
	}

	.summary-card {
		width: calc(50% - 12rpx);
		margin: 0 6rpx 12rpx;
	}

	.big.blue,
	.weak-link {
		color: #386df5;
	}

	.sub.green {
		color: #16a34a;
	}

	.sub.red,
	.weak-rate {
		color: #ef4444;
	}

	.weak-track {
		border-radius: 999rpx;
	}

	.weak-bar {
		background: #ef4444;
	}
	/* #endif */
</style>
