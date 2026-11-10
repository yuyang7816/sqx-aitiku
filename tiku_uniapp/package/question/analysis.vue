<template>
	<view class="app-page">
		<view class="screen detail-screen">
			<view v-if="!hasReviewItem" class="empty-review card">
				<view class="empty-icon-wrap">
					<up-icon name="empty-page" size="48" color="#97a0af"></up-icon>
				</view>
				<text class="empty-title">暂无题目解析</text>
				<text class="empty-copy">请从练习、结果页、错题本或收藏中进入解析页查看。</text>
			</view>
			<template v-else>
				<question-analysis-panel
					:item="reviewItem"
					show-question-title
					player-id="analysisPageVideo"
				/>
				<view class="bottom-row">
					<view class="solid-button green" @tap="askAi">
						<up-icon name="chat" size="18" color="#FFFFFF"></up-icon>
						<text>追问AI</text>
					</view>
					<view class="ghost-button small" @tap="collect">
						<up-icon :name="isCollected ? 'star-fill' : 'star'" size="16" :color="isCollected ? '#FF9A48' : '#5d6777'"></up-icon>
						<text>{{ isCollected ? '取消收藏' : '收藏' }}</text>
					</view>
				</view>
			</template>
		</view>
	</view>
</template>

<script setup>
	import {
		ref
	} from 'vue';
	import {
		checkAiChatQuota,
		hasCollectionItem,
		resolveCollectionSubject,
		showAiQuotaExhaustedModal,
		syncCollectionListFromApi,
		toggleCollectionItem
	} from '@/common/app-data';
	import { QUESTION_REVIEW_KEY } from '@/common/storage-keys';
	import {
		onLoad,
		onShow
	} from '@dcloudio/uni-app';

	/** 解析页展示结构，统一兼容结果页、错题本、收藏写入的 reviewItem 字段 */
	const createEmptyReviewItem = () => ({
		title: '',
		options: [],
		myAnswer: '',
		rightAnswer: '',
		analysis: '',
		aiSummary: '',
		tags: [],
		isCorrect: false
	});

	/** 规范化 Storage 中的 reviewItem，兼容 title/question、myAnswer/wrong、rightAnswer/right */
	const normalizeReviewItem = (raw = {}) => {
		if (!raw || typeof raw !== 'object') {
			return createEmptyReviewItem();
		}
		const title = String(raw.title || raw.question || '').trim();
		const myAnswer = String(raw.myAnswer || raw.wrong || '').trim();
		const rightAnswer = String(raw.rightAnswer || raw.right || '').trim();
		const analysis = String(raw.analysis || '').trim();
		const aiSummary = String(raw.aiSummary || '').trim();
		const options = Array.isArray(raw.options) ? raw.options : [];
		const tags = Array.isArray(raw.tags) ? raw.tags.filter(Boolean) : [];
		return {
			...raw,
			title,
			myAnswer,
			rightAnswer,
			analysis,
			aiSummary,
			options,
			tags,
			isCorrect: raw.isCorrect === true
		};
	};

	const reviewItem = ref(createEmptyReviewItem());
	const hasReviewItem = ref(false);
	const isCollected = ref(false);
	const collectLoading = ref(false);

	/** 收藏状态查询键，优先使用 questionId/questionCode，其次回退到题干 */
	const getCollectionKey = (item = {}) => String(
		item.questionId || item.questionCode || item.id || item.title || ''
	).trim();

	/** 解析页学科优先使用来源 subject，否则回退到当前资料里的真实学科 */
	const resolveAnalysisSubject = (item = {}) => {
		const fromSource = String(item.subject || '').trim();
		if (fromSource) {
			return fromSource;
		}
		return resolveCollectionSubject();
	};

	const refreshCollectedState = (item = reviewItem.value) => {
		const key = getCollectionKey(item);
		isCollected.value = Boolean(key && hasCollectionItem(key));
	};

	const loadReviewItem = async () => {
		await syncCollectionListFromApi();
		const saved = uni.getStorageSync(QUESTION_REVIEW_KEY);
		const normalized = normalizeReviewItem(saved);
		hasReviewItem.value = Boolean(normalized.title);
		reviewItem.value = normalized;
		refreshCollectedState(normalized);
	};

	onLoad(loadReviewItem);
	onShow(loadReviewItem);

	const go = (url) => uni.navigateTo({ url });

	/** 追问 AI：校验剩余次数后跳转 chat，扣减在对话页发送成功后执行 */
	const askAi = () => {
		if (!hasReviewItem.value) {
			uni.showToast({ title: '暂无题目可追问', icon: 'none' });
			return;
		}
		const quota = checkAiChatQuota();
		if (!quota.ok && !quota.notReady) {
			showAiQuotaExhaustedModal();
			return;
		}
		const topic = encodeURIComponent(reviewItem.value.title);
		const tip = encodeURIComponent(reviewItem.value.aiSummary || reviewItem.value.analysis || '');
		go(`/package/ai/chat?topic=${topic}&tip=${tip}`);
	};

	const collect = async () => {
		if (collectLoading.value || !hasReviewItem.value) {
			return;
		}
		const item = reviewItem.value;
		const questionId = getCollectionKey(item);
		if (!questionId) {
			uni.showToast({ title: '题目信息不完整', icon: 'none' });
			return;
		}
		collectLoading.value = true;
		try {
			const result = await toggleCollectionItem({
				...item,
				questionId,
				subject: resolveAnalysisSubject(item)
			});
			isCollected.value = result.collected;
			uni.showToast({ title: result.collected ? '已加入收藏' : '已取消收藏', icon: 'none' });
		} catch (e) {
			uni.showToast({ title: e.msg || '收藏操作失败', icon: 'none' });
		} finally {
			collectLoading.value = false;
		}
	};
</script>

<style lang="scss" scoped>
	.detail-screen {
		padding-top: 12rpx;
	}

	.bottom-row {
		margin-top: 28rpx;
		display: flex;
		gap: 16rpx;
	}

	.green {
		flex: 1;
		background: var(--success);
		display: flex;
		align-items: center;
		justify-content: center;
		gap: 10rpx;
		font-size: 28rpx;
		font-weight: 800;
	}

	.small {
		width: 192rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		gap: 8rpx;
		font-size: 24rpx;
		font-weight: 600;
	}

	.empty-review {
		margin-top: 16rpx;
		padding: 48rpx 32rpx;
		text-align: center;
	}

	.empty-icon-wrap {
		margin-bottom: 24rpx;
		display: flex;
		justify-content: center;
	}

	.empty-title {
		display: block;
		font-size: 34rpx;
		font-weight: 800;
		color: var(--text-main);
	}

	.empty-copy {
		margin-top: 12rpx;
		display: block;
		font-size: 26rpx;
		line-height: 1.7;
		font-weight: 500;
		color: var(--text-sub);
	}

	/* #ifdef MP-WEIXIN */
	.green {
		background: #16a34a;
	}

	.empty-title {
		color: #243041;
	}

	.empty-copy {
		color: #97a0af;
	}
	/* #endif */
</style>
