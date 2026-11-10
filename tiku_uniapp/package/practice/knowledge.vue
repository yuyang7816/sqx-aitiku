<template>
	<view class="app-page">
		<view class="page-scroll">
			<view class="screen knowledge-screen">
				<text class="section-sub">选择薄弱知识点，进入针对性刷题</text>
				<view v-if="loading" class="knowledge-loading">
					<up-loading-icon mode="circle" size="20"></up-loading-icon>
					<text>加载知识点中...</text>
				</view>
				<view v-else-if="!knowledgePoints.length" class="knowledge-empty">
					<up-icon name="empty-list" size="48" color="#C0C6D4"></up-icon>
					<text>暂无知识点，请先在管理端维护或完善学习计划</text>
				</view>
				<view v-else class="knowledge-list">
					<view v-for="item in knowledgePoints" :key="item.code || item.title" class="knowledge-card card" @tap="startKnowledgePractice(item)">
						<view class="knowledge-left">
							<view class="knowledge-icon">
								<up-icon name="star-fill" size="18" color="#2AAA64"></up-icon>
							</view>
							<view class="knowledge-info">
								<view class="knowledge-title">{{ item.title }}</view>
								<view class="knowledge-meta">{{ item.meta }}</view>
							</view>
						</view>
						<view class="knowledge-arrow">
							<up-icon name="arrow-right" size="14" color="#C0C6D4"></up-icon>
						</view>
					</view>
				</view>
			</view>
		</view>
	</view>
</template>

<script setup>
	import { ref } from 'vue';
	import { onLoad, onShow } from '@dcloudio/uni-app';
	import { setPracticeSource, getUserProfile } from '@/common/app-data';
	import { fetchKnowledgePointList } from '@/common/knowledge-list';
	import {
		getPracticeActiveChip,
		resolvePracticeChipFromQuery,
		resolvePracticeSubjectFromChip,
		setPracticeActiveChip
	} from '@/common/practice-chip-context';
	import { QUESTION_ANSWER_PAGE } from '@/common/routes';

	const profile = ref(getUserProfile());
	const activeChip = ref(getPracticeActiveChip());
	const knowledgePoints = ref([]);
	const loading = ref(false);

	const refreshKnowledgePoints = async () => {
		loading.value = true;
		try {
			const subject = resolvePracticeSubjectFromChip(activeChip.value, profile.value);
			const result = await fetchKnowledgePointList(subject ? { subject } : {});
			knowledgePoints.value = result.list;
		} finally {
			loading.value = false;
		}
	};

	onLoad((query) => {
		activeChip.value = resolvePracticeChipFromQuery(query);
		setPracticeActiveChip(activeChip.value);
	});

	onShow(() => {
		profile.value = getUserProfile();
		refreshKnowledgePoints();
	});

	const startKnowledgePractice = (item) => {
		const code = String(item.code || '').trim();
		const title = String(item.title || '').trim();
		setPracticeSource({
			mode: 'all',
			knowledgePoint: code || title,
			title: title ? `知识点：${title}` : '知识点练习'
		});
		uni.navigateTo({ url: QUESTION_ANSWER_PAGE });
	};
</script>

<style lang="scss" scoped>
	.knowledge-screen {
		padding-top: 12rpx;
	}

	.section-sub {
		display: block;
		margin-top: 16rpx;
		font-size: var(--text-base);
		color: #97a0af;
		line-height: 1.5;
	}

	.knowledge-loading,
	.knowledge-empty {
		margin-top: 48rpx;
		display: flex;
		flex-direction: column;
		align-items: center;
		gap: 16rpx;
		color: #97a0af;
		font-size: var(--text-base);
		text-align: center;
		padding: 0 32rpx;
	}

	.knowledge-list {
		margin-top: 24rpx;
		display: flex;
		flex-direction: column;
		gap: 20rpx;
	}

	.knowledge-card {
		padding: 28rpx 26rpx;
		display: flex;
		align-items: center;
		justify-content: space-between;
		min-height: 116rpx;
		transition: transform 0.15s ease;
	}

	.knowledge-card:active {
		transform: scale(0.99);
	}

	.knowledge-left {
		display: flex;
		align-items: center;
		gap: 20rpx;
	}

	.knowledge-icon {
		width: 64rpx;
		height: 64rpx;
		border-radius: var(--radius-lg);
		background: #eefcf2;
		display: flex;
		align-items: center;
		justify-content: center;
	}

	.knowledge-info {
		display: flex;
		flex-direction: column;
	}

	.knowledge-title {
		font-size: var(--text-xl);
		font-weight: 700;
		color: var(--text-main);
		line-height: 1.25;
	}

	.knowledge-meta {
		margin-top: 8rpx;
		font-size: var(--text-sm);
		color: #97a0af;
	}

	.knowledge-arrow {
		width: 40rpx;
		height: 40rpx;
		display: flex;
		align-items: center;
		justify-content: center;
	}
</style>
