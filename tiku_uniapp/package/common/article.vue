<template>
	<view class="app-page">
		<view class="page-scroll">
			<view class="screen article-screen">
				<view v-if="loading" class="article-loading">
					<up-loading-icon mode="circle" color="#386df5"></up-loading-icon>
					<text class="loading-text">加载中...</text>
				</view>
				<view v-else-if="loadError" class="article-error card">
					<view class="error-icon">
						<up-icon name="close-circle" size="26" color="#f05252"></up-icon>
					</view>
					<text class="error-text">{{ loadError }}</text>
					<view class="retry-btn" @tap="loadArticle">
						<up-icon name="reload" size="14" color="#386df5"></up-icon>
						<text>重新加载</text>
					</view>
				</view>
				<view v-else class="article-card card">
					<text class="article-title">{{ article.title }}</text>
					<text class="article-sub">{{ article.sub }}</text>
					<view class="article-html">
						<up-parse :content="article.content"></up-parse>
					</view>
				</view>
			</view>
		</view>
	</view>
</template>

<script setup>
	import { computed, ref } from 'vue';
	import { onLoad } from '@dcloudio/uni-app';
	import { fetchArticleByType } from '@/common/article-config';
	import { usePageNavigationTitle } from '@/common/navigation-bar';

	const type = ref('about');
	const loading = ref(false);
	const loadError = ref('');
	const remoteArticle = ref(null);

	const article = computed(() => remoteArticle.value || {});
	const navigationTitle = computed(() => article.value.title || '详情');
	usePageNavigationTitle(navigationTitle);

	const loadArticle = async () => {
		loading.value = true;
		loadError.value = '';
		remoteArticle.value = null;
		try {
			const data = await fetchArticleByType(type.value);
			if (data) {
				remoteArticle.value = data;
			} else {
				loadError.value = '内容加载失败';
			}
		} catch (e) {
			loadError.value = '内容加载失败，请稍后再重试';
		} finally {
			loading.value = false;
		}
	};

	onLoad((options) => {
		if (options?.type) {
			type.value = options.type;
		}
		loadArticle();
	});
</script>

<style lang="scss" scoped>
	.article-screen {
		padding-top: 12rpx;
	}

	.article-loading {
		margin-top: 80rpx;
		display: flex;
		flex-direction: column;
		align-items: center;
		gap: 24rpx;
	}

	.loading-text {
		font-size: var(--text-lg);
		color: #8d96a6;
	}

	.article-error {
		margin-top: 16rpx;
		padding: 48rpx 28rpx;
		display: flex;
		flex-direction: column;
		align-items: center;
	}

	.error-icon {
		width: 96rpx;
		height: 96rpx;
		border-radius: var(--radius-lg);
		background: var(--error-light);
		display: flex;
		align-items: center;
		justify-content: center;
	}

	.error-text {
		display: block;
		margin-top: 20rpx;
		font-size: var(--text-lg);
		color: #f05252;
	}

	.retry-btn {
		margin-top: 28rpx;
		display: inline-flex;
		align-items: center;
		gap: 8rpx;
		padding: 16rpx 40rpx;
		border-radius: var(--radius-full);
		background: var(--primary-light);
		color: var(--primary);
		font-size: var(--text-lg);
		font-weight: 600;
	}

	.article-card {
		margin-top: 16rpx;
		padding: 36rpx 32rpx;
	}

	.article-title {
		display: block;
		font-size: 40rpx;
		font-weight: 800;
		color: var(--text-main);
	}

	.article-sub {
		margin-top: 20rpx;
		display: block;
		font-size: var(--text-lg);
		line-height: 1.8;
		color: #667085;
	}

	.article-html {
		margin-top: 30rpx;
		font-size: var(--text-md);
		line-height: 1.8;
		color: #5f6979;
	}

	/* #ifdef MP-WEIXIN */
	.loading-text,
	.error-text,
	.retry-btn,
	.article-sub {
		font-size: 28rpx;
	}

	.error-icon {
		border-radius: 24rpx;
		background: #ffefef;
	}

	.retry-btn {
		border-radius: 999rpx;
		background: #eef4ff;
		color: #386df5;
	}

	.article-title {
		color: #243041;
	}

	.article-html {
		font-size: 26rpx;
	}
	/* #endif */

</style>
