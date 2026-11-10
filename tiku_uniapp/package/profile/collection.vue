<template>
	<view class="app-page">
		<view class="page-scroll">
			<view class="screen collection-screen">
				<view v-if="collections.length" class="collection-list">
					<view v-for="item in collections" :key="getCollectionItemKey(item)" class="collection-card card">
						<view class="collection-head">
							<view class="subject-tag">
								<up-icon name="star-fill" size="11" color="#386df5"></up-icon>
								<text>{{ item.subject }}</text>
							</view>
							<text class="collection-date">{{ item.date }}</text>
						</view>
						<text class="collection-title">{{ item.title }}</text>
						<view class="collection-actions">
							<view class="pill-btn" @tap="openAnalysis(item)">
								<up-icon name="file-text" size="13" color="#5d6777"></up-icon>
								<text>查看解析</text>
							</view>
							<view class="pill-btn green" @tap="askAi(item)">
								<up-icon name="kefu-ermai" size="13" color="#1caa55"></up-icon>
								<text>AI讲解</text>
							</view>
							<view
								class="pill-btn danger"
								:class="{ disabled: isRemovingItem(item) }"
								@tap="removeItem(item)"
							>
								<up-icon name="trash" size="13" color="#ef4444"></up-icon>
								<text>{{ isRemovingItem(item) ? '取消中…' : '取消收藏' }}</text>
							</view>
						</view>
					</view>
				</view>
				<app-empty
					v-else-if="!loading"
					class="collection-empty card"
					content="暂无收藏"
					sub-content="你还没有收藏任何题目，刷题时点击星标即可收藏哦"
					mode="list"
					padding-top="80rpx"
					button-text="去刷题"
					button-icon="arrow-right"
					@button="goPractice"
				></app-empty>
			</view>
		</view>
	</view>
</template>

<script setup>
	import { computed, ref } from 'vue';
	import { usePageNavigationTitle } from '@/common/navigation-bar';
	import { onShow } from '@dcloudio/uni-app';
	import {
		buildReviewItemFromCollection,
		cancelCollectionItem,
		canSyncCollectionToApi,
		checkAiChatQuota,
		getCollectionItemKey,
		getCollectionList,
		setQuestionReviewItem,
		showAiQuotaExhaustedModal,
		syncCollectionListFromApi
	} from '@/common/app-data';
	import { buildChatPageUrl, buildCollectionChatEntry } from '@/common/chat-entry';
	import { PRACTICE_TAB_PAGE, QUESTION_ANALYSIS_PAGE } from '@/common/routes';
	const collections = ref(getCollectionList().map((item) => ({ ...item })));
	const loading = ref(false);
	const removingKey = ref('');
	const navbarTitle = computed(() => `我的收藏（${collections.value.length}）`);
	usePageNavigationTitle(navbarTitle);

	const isRemovingItem = (item) => removingKey.value === getCollectionItemKey(item);

	const refreshCollections = async () => {
		loading.value = true;
		try {
			const synced = await syncCollectionListFromApi();
			collections.value = synced.list.map((item) => ({ ...item }));
			if (canSyncCollectionToApi() && synced.error) {
				uni.showToast({
					title: synced.error.msg || synced.error.message || '收藏列表加载失败',
					icon: 'none'
				});
			}
		} catch (e) {
			collections.value = getCollectionList().map((item) => ({ ...item }));
		} finally {
			loading.value = false;
		}
	};

	onShow(() => {
		refreshCollections();
	});

	/** 空态引导：跳转刷题 Tab 页（主包 Tab 用 reLaunch 清空页面栈） */
	const goPractice = () => {
		uni.reLaunch({ url: PRACTICE_TAB_PAGE });
	};

	/** 收藏解析：写入 reviewItem 后跳转解析页（与错题本/结果页字段对齐） */
	const openAnalysis = (item) => {
		const reviewItem = buildReviewItemFromCollection(item);
		const questionTitle = String(reviewItem.title || '').trim();
		if (!questionTitle || questionTitle === '未命名题目') {
			uni.showToast({ title: '题目信息不完整', icon: 'none' });
			return;
		}
		setQuestionReviewItem(reviewItem);
		uni.navigateTo({ url: QUESTION_ANALYSIS_PAGE });
	};

	/** AI 讲解：校验剩余次数后跳转 chat，携带题干与 AI 摘要 tip */
	const askAi = (item) => {
		const reviewItem = buildReviewItemFromCollection(item);
		const { topic, tip } = buildCollectionChatEntry(reviewItem);
		if (!topic || topic === '未命名题目') {
			uni.showToast({ title: '题目信息不完整', icon: 'none' });
			return;
		}
		const quota = checkAiChatQuota();
		if (!quota.ok && !quota.notReady) {
			showAiQuotaExhaustedModal();
			return;
		}
		uni.navigateTo({ url: buildChatPageUrl({ topic, tip }) });
	};

	/** 取消收藏：确认后 DELETE /app/collection，filter 本地列表并即时移除卡片 */
	const removeItem = (item) => {
		if (isRemovingItem(item)) {
			return;
		}
		const title = String(item.title || '该题目').trim();
		uni.showModal({
			title: '取消收藏',
			content: `确定取消收藏「${title.slice(0, 24)}${title.length > 24 ? '…' : ''}」吗？`,
			confirmText: '取消收藏',
			confirmColor: '#ef4444',
			cancelText: '保留',
			success: async (res) => {
				if (!res.confirm) {
					return;
				}
				const itemKey = getCollectionItemKey(item);
				removingKey.value = itemKey;
				try {
					const result = await cancelCollectionItem(item, collections.value);
					collections.value = result.list.map((entry) => ({ ...entry }));
					uni.showToast({ title: '已取消收藏', icon: 'none' });
				} catch (e) {
					uni.showToast({ title: e.msg || '取消收藏失败', icon: 'none' });
				} finally {
					removingKey.value = '';
				}
			}
		});
	};
</script>

<style lang="scss" scoped>
	.collection-screen {
		padding-top: 12rpx;
	}

	.collection-empty {
		margin-top: 16rpx;
	}

	.collection-list {
		margin-top: 16rpx;
		display: flex;
		flex-direction: column;
		gap: 18rpx;
	}

	.collection-card {
		padding: 28rpx;
		transition: transform 0.15s ease;
	}

	.collection-card:active {
		transform: scale(0.99);
	}

	.collection-head {
		display: flex;
		align-items: center;
		justify-content: space-between;
		gap: 16rpx;
	}

	.subject-tag {
		padding: 8rpx 16rpx;
		border-radius: 14rpx;
		background: #eef4ff;
		color: #386df5;
		font-size: 20rpx;
		display: flex;
		align-items: center;
		gap: 6rpx;
		font-weight: 600;
	}

	.collection-date {
		font-size: 22rpx;
		color: #97a0af;
	}

	.collection-title {
		margin-top: 22rpx;
		display: block;
		font-size: 30rpx;
		font-weight: 700;
		line-height: 1.6;
		color: #253143;
	}

	.collection-actions {
		margin-top: 26rpx;
		display: flex;
		gap: 16rpx;
	}

	.pill-btn {
		flex: 1;
		height: 72rpx;
		border-radius: 24rpx;
		background: #f5f7fb;
		border: 2rpx solid #e6ebf3;
		color: #5d6777;
		font-size: 24rpx;
		font-weight: 700;
		display: flex;
		align-items: center;
		justify-content: center;
		gap: 8rpx;
		transition: transform 0.15s ease;
	}

	.pill-btn:active {
		transform: scale(0.97);
	}

	.pill-btn.green {
		background: #eaf8ef;
		border-color: #d9f3e2;
		color: #1caa55;
	}

	.pill-btn.danger {
		background: #ffefef;
		border-color: #ffdada;
		color: #ef4444;
	}

	.pill-btn.disabled {
		opacity: 0.55;
		pointer-events: none;
	}
</style>
