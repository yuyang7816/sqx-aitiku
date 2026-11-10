<template>
	<view class="app-page answer-sheet-page">
		<view v-if="!questionList.length" class="sheet-empty">
			<up-icon name="empty-page" size="64" color="#C0C6D4"></up-icon>
			<text class="sheet-empty-text">暂无答题数据</text>
			<view class="sheet-empty-btn" @tap="back">返回答题</view>
		</view>
		<scroll-view v-else class="sheet-scroll" scroll-y :show-scrollbar="false">
			<view class="sheet-card card">
				<view class="sheet-head">
					<text class="sheet-title">答题卡</text>
					<text class="sheet-copy">已答 {{ answeredCount }} · 未答 {{ remainingCount }} / {{ questionList.length }}</text>
				</view>
				<view class="sheet-legend">
					<view v-for="item in sheetLegendList" :key="item.key" class="legend-item">
						<view class="legend-dot" :class="`legend-dot--${item.key}`"></view>
						<text class="legend-text">{{ item.label }}</text>
					</view>
				</view>
				<view class="sheet-grid">
					<view
						v-for="item in questionNavList"
						:key="item.index"
						class="sheet-item"
						:class="`sheet-item--${item.status}`"
						@tap="jumpToQuestion(item.index)"
					>{{ item.order }}</view>
				</view>
			</view>
		</scroll-view>
		<view v-if="questionList.length" class="sheet-footer">
			<view class="footer-action neutral" :class="{ disabled: isFirstQuestion }" @tap="prevQuestion">
				<text>上一题</text>
			</view>
			<view class="footer-action neutral" :class="{ disabled: isLastQuestion }" @tap="nextQuestion">
				<text>下一题</text>
			</view>
			<view class="footer-action warn" @tap="completePractice">
				<text>完成练习</text>
			</view>
			<view class="footer-action primary" @tap="continueAnswer">
				<text>继续答题</text>
			</view>
		</view>
	</view>
</template>

<script setup>
	import { computed, ref } from 'vue';
	import { onShow } from '@dcloudio/uni-app';
	import { hasQuestionAnswer } from '@/common/question-answer';
	import {
		getAnswerSheetState,
		markAnswerSheetJump,
		markAnswerSheetReturn
	} from '@/common/answer-sheet-bridge';

	const questionList = ref([]);
	const selectedMap = ref({});
	const currentIndex = ref(0);

	const sheetLegendList = [
		{ key: 'answered', label: '已答' },
		{ key: 'current', label: '当前未答' },
		{ key: 'current-answered', label: '当前已答' },
		{ key: 'unanswered', label: '未答' }
	];

	const answeredCount = computed(() => questionList.value.filter((item, index) => (
		hasQuestionAnswer(selectedMap.value[index], item)
	)).length);
	const remainingCount = computed(() => Math.max(questionList.value.length - answeredCount.value, 0));
	const isFirstQuestion = computed(() => currentIndex.value === 0);
	const isLastQuestion = computed(() => (
		questionList.value.length > 0 && currentIndex.value >= questionList.value.length - 1
	));

	const questionNavList = computed(() => questionList.value.map((item, index) => {
		const isCurrent = currentIndex.value === index;
		const isAnswered = hasQuestionAnswer(selectedMap.value[index], item);
		let status = 'unanswered';
		if (isCurrent) {
			status = isAnswered ? 'current-answered' : 'current';
		} else if (isAnswered) {
			status = 'answered';
		}
		return {
			index,
			order: index + 1,
			status
		};
	}));

	const loadSheetState = () => {
		const state = getAnswerSheetState();
		if (!state) {
			questionList.value = [];
			selectedMap.value = {};
			currentIndex.value = 0;
			return;
		}
		questionList.value = Array.isArray(state.questionList) ? state.questionList : [];
		selectedMap.value = state.selectedMap && typeof state.selectedMap === 'object'
			? { ...state.selectedMap }
			: {};
		currentIndex.value = Math.max(0, Number(state.currentIndex) || 0);
	};

	onShow(() => {
		loadSheetState();
	});

	const back = () => {
		uni.navigateBack();
	};

	const jumpToQuestion = (index) => {
		currentIndex.value = Math.max(0, Number(index) || 0);
		if (!markAnswerSheetJump(currentIndex.value)) {
			return;
		}
		uni.navigateBack();
	};

	const prevQuestion = () => {
		if (isFirstQuestion.value) {
			return;
		}
		currentIndex.value -= 1;
	};

	const nextQuestion = () => {
		if (isLastQuestion.value) {
			return;
		}
		currentIndex.value += 1;
	};

	const continueAnswer = () => {
		if (!markAnswerSheetReturn({ index: currentIndex.value })) {
			return;
		}
		uni.navigateBack();
	};

	const completePractice = () => {
		if (!markAnswerSheetReturn({ index: currentIndex.value, complete: true })) {
			return;
		}
		uni.navigateBack();
	};
</script>

<style lang="scss" scoped>
	.answer-sheet-page {
		min-height: 100vh;
		background:
			radial-gradient(circle at top right, rgba(73, 127, 255, 0.14) 0, rgba(73, 127, 255, 0) 22%),
			linear-gradient(180deg, #eef4ff 0, #eaf1fd 180rpx, #f7f9fc 100%);
		display: flex;
		flex-direction: column;
	}

	.sheet-scroll {
		flex: 1;
		height: 0;
		min-height: 0;
		padding: 18rpx 28rpx 24rpx;
		box-sizing: border-box;
	}

	.sheet-card {
		padding: 28rpx;
		border-radius: 32rpx;
		background: rgba(255, 255, 255, 0.95);
		box-shadow: 0 18rpx 44rpx rgba(15, 23, 42, 0.08);
		border: 2rpx solid rgba(228, 234, 244, 0.92);
		position: relative;
		overflow: hidden;
	}

	.sheet-card::before {
		content: '';
		position: absolute;
		left: 0;
		top: 0;
		width: 100%;
		height: 8rpx;
		background: linear-gradient(90deg, #386df5 0%, #7aa1ff 100%);
	}

	.sheet-head {
		display: flex;
		align-items: center;
		justify-content: space-between;
		gap: 16rpx;
	}

	.sheet-title {
		font-size: 30rpx;
		font-weight: 800;
		color: #182230;
	}

	.sheet-copy {
		font-size: 20rpx;
		color: #98a2b3;
	}

	.sheet-legend {
		margin-top: 18rpx;
		display: flex;
		flex-wrap: wrap;
		gap: 22rpx;
	}

	.legend-item {
		display: flex;
		align-items: center;
		gap: 10rpx;
	}

	.legend-dot {
		width: 18rpx;
		height: 18rpx;
		border-radius: 50%;
	}

	.legend-dot--answered {
		background: #b7ceff;
	}

	.legend-dot--current {
		background: #386df5;
	}

	.legend-dot--current-answered {
		background: #12945a;
	}

	.legend-dot--unanswered {
		background: #d8dee9;
	}

	.legend-text {
		font-size: 22rpx;
		color: #667085;
	}

	.sheet-grid {
		margin-top: 22rpx;
		display: grid;
		grid-template-columns: repeat(5, 1fr);
		gap: 14rpx;
	}

	.sheet-item {
		height: 78rpx;
		border-radius: 22rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		font-size: 22rpx;
		font-weight: 700;
	}

	.sheet-item--unanswered {
		background: #f4f6fa;
		color: #98a2b3;
	}

	.sheet-item--answered {
		background: #edf4ff;
		color: #386df5;
	}

	.sheet-item--current {
		background: linear-gradient(135deg, #386df5 0%, #618eff 100%);
		color: #ffffff;
		box-shadow: 0 12rpx 24rpx rgba(56, 109, 245, 0.24);
	}

	.sheet-item--current-answered {
		background: linear-gradient(135deg, #12945a 0%, #34c77b 100%);
		color: #ffffff;
		box-shadow: 0 12rpx 24rpx rgba(18, 148, 90, 0.22);
	}

	.sheet-footer {
		flex-shrink: 0;
		padding: 20rpx 24rpx calc(20rpx + env(safe-area-inset-bottom));
		background: rgba(248, 250, 253, 0.96);
		backdrop-filter: blur(18rpx);
		display: grid;
		grid-template-columns: repeat(4, minmax(0, 1fr));
		gap: 12rpx;
		align-items: center;
		border-top: 1rpx solid rgba(212, 218, 229, 0.8);
		box-shadow: 0 -14rpx 34rpx rgba(15, 23, 42, 0.08);
	}

	.footer-action {
		height: 88rpx;
		border-radius: 24rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		font-size: 22rpx;
		font-weight: 700;
		white-space: nowrap;
	}

	.footer-action.disabled {
		opacity: 0.42;
		pointer-events: none;
	}

	.footer-action.neutral {
		background: #eef2f7;
		color: #4b5565;
		box-shadow: inset 0 0 0 2rpx rgba(222, 227, 236, 0.82);
	}

	.footer-action.warn {
		background: #fff5e8;
		color: #d97706;
		box-shadow: inset 0 0 0 2rpx rgba(245, 158, 11, 0.18);
	}

	.footer-action.primary {
		background: linear-gradient(135deg, #386df5 0%, #5d8eff 100%);
		color: #ffffff;
		box-shadow: 0 18rpx 30rpx rgba(56, 109, 245, 0.24);
	}

	.sheet-empty {
		flex: 1;
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: center;
		gap: 20rpx;
		padding: 28rpx;
	}

	.sheet-empty-text {
		font-size: 26rpx;
		color: #7b8698;
	}

	.sheet-empty-btn {
		margin-top: 14rpx;
		height: 88rpx;
		padding: 0 48rpx;
		border-radius: 999rpx;
		background: linear-gradient(135deg, #386df5 0%, #5f8fff 100%);
		color: #ffffff;
		font-size: 26rpx;
		font-weight: 700;
		display: flex;
		align-items: center;
		justify-content: center;
		box-shadow: 0 16rpx 28rpx rgba(56, 109, 245, 0.2);
	}

	@media screen and (max-width: 375px) {
		.sheet-scroll {
			padding-left: 22rpx;
			padding-right: 22rpx;
		}

		.sheet-grid {
			gap: 10rpx;
		}

		.sheet-item {
			height: 70rpx;
			font-size: 22rpx;
		}
	}

	/* #ifdef MP-WEIXIN */
	.sheet-grid {
		display: flex;
		flex-wrap: wrap;
		gap: 0;
		margin-left: -7rpx;
		margin-right: -7rpx;
	}

	.sheet-item {
		width: calc(20% - 14rpx);
		margin: 0 7rpx 14rpx;
	}

	.sheet-footer {
		display: flex;
		flex-wrap: wrap;
		gap: 12rpx;
		background: rgba(248, 250, 253, 1);
		backdrop-filter: none;
	}

	.footer-action {
		width: calc(50% - 6rpx);
	}
	/* #endif */
</style>
