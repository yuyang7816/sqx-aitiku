<template>
	<view class="note-card card">
		<view class="note-head">
			<view class="note-head-left">
				<text class="subject-tag">{{ subjectText }}</text>
				<text class="status-tag" :class="statusClass">{{ statusText }}</text>
				<text class="wrong-count-tag">错 {{ wrongCountText }} 次</text>
			</view>
			<text class="note-date">{{ lastPracticeText }}</text>
		</view>
		<view class="note-question">{{ questionText }}</view>
		<view class="answer-row">
			<text class="answer-pill wrong">错误答案: {{ wrongAnswerText }}</text>
			<text class="answer-pill right">正确答案: {{ rightAnswerText }}</text>
		</view>
		<slot name="actions"></slot>
	</view>
</template>

<script setup>
	import { computed } from 'vue';
	import {
		formatNotebookAnswer,
		formatNotebookItemSubject,
		formatNotebookLastPracticeAt,
		formatNotebookWrongCount,
		getNotebookItemStatusClass,
		getNotebookItemStatusText
	} from '@/common/app-data';

	const props = defineProps({
		item: {
			type: Object,
			default: () => ({})
		}
	});

	const subjectText = computed(() => formatNotebookItemSubject(props.item));
	const statusText = computed(() => getNotebookItemStatusText(props.item));
	const wrongCountText = computed(() => formatNotebookWrongCount(props.item));
	const statusClass = computed(() => getNotebookItemStatusClass(props.item));
	const questionText = computed(() => String(props.item?.question || '未命名题目').trim() || '未命名题目');
	const lastPracticeText = computed(() => formatNotebookLastPracticeAt(props.item));
	const wrongAnswerText = computed(() => formatNotebookAnswer(props.item?.wrong));
	const rightAnswerText = computed(() => formatNotebookAnswer(props.item?.right));
</script>

<style lang="scss" scoped>
	.note-card {
		padding: 30rpx 26rpx 28rpx;
		border-radius: 28rpx;
		background: linear-gradient(180deg, #FFFFFF 0%, #FBFCFF 100%);
		box-shadow: 0 12rpx 28rpx rgba(33, 70, 136, 0.06);
		border: 1rpx solid rgba(102, 144, 214, 0.08);
		transition: transform 0.15s ease;
	}

	.note-card:active {
		transform: scale(0.99);
	}

	.note-head {
		display: flex;
		align-items: flex-start;
		justify-content: space-between;
		gap: 14rpx;
	}

	.note-head-left {
		display: flex;
		align-items: center;
		gap: 12rpx;
		flex-wrap: wrap;
	}

	.subject-tag {
		padding: 8rpx 14rpx;
		border-radius: 999rpx;
		font-size: 18rpx;
		line-height: 1.2;
		color: #F26452;
		background: #FFF0EC;
	}

	.note-date {
		font-size: 20rpx;
		line-height: 1.3;
		color: #97a0af;
		flex-shrink: 0;
	}

	.note-question {
		margin-top: 20rpx;
		font-size: 30rpx;
		font-weight: 600;
		color: #22324B;
		line-height: 1.45;
	}

	.status-tag {
		padding: 8rpx 14rpx;
		border-radius: 999rpx;
		font-size: 18rpx;
		line-height: 1.2;
	}

	.status-tag.pending {
		background: #eef4ff;
		color: #386df5;
	}

	.status-tag.high {
		background: #fff1f1;
		color: #ef4444;
	}

	.status-tag.mastered {
		background: #eaf8ef;
		color: #16a34a;
	}

	.wrong-count-tag {
		padding: 8rpx 14rpx;
		border-radius: 999rpx;
		font-size: 18rpx;
		line-height: 1.2;
		color: #ef4444;
		background: #fff1f1;
	}

	.answer-row {
		margin-top: 18rpx;
		display: flex;
		gap: 12rpx;
		flex-wrap: wrap;
	}

	.answer-pill {
		padding: 10rpx 16rpx;
		border-radius: 16rpx;
		font-size: 20rpx;
		line-height: 1.4;
	}

	.answer-pill.wrong {
		color: #f05963;
		background: #ffefef;
	}

	.answer-pill.right {
		color: #2ca65f;
		background: #eaf8ef;
	}
</style>
