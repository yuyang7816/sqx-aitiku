<template>
	<view class="app-page">
		<view class="page-scroll">
			<view class="screen notice-screen">
				<view class="notice-hero">
					<view class="hero-copy">
						<text class="hero-eyebrow">通知中心</text>
						<text class="hero-title">学习提醒与系统消息</text>
						<text class="hero-meta">共 {{ notices.length }} 条消息，未读 {{ unreadCount }} 条</text>
					</view>
					<view class="hero-action" :class="{ disabled: readAllLoading }" @tap="readAll">
						<up-icon name="checkmark-circle-fill" size="16" color="#FFFFFF"></up-icon>
						<text>全部已读</text>
					</view>
				</view>
				<view v-if="loading" class="notice-loading">
					<up-loading-icon mode="circle" color="#386df5"></up-loading-icon>
					<text class="subtle-text">消息加载中...</text>
				</view>
				<app-empty
					v-else-if="!notices.length"
					mode="message"
					content="暂无消息"
					sub-content="系统通知与学习提醒会显示在这里"
					padding-top="120rpx"
				/>
				<view v-else class="notice-list">
					<view
						v-for="item in notices"
						:key="resolveNoticeListKey(item)"
						class="notice-card"
						:class="item.read ? 'is-read' : 'is-unread'"
						@tap="openNotice(item)"
					>
						<view v-if="!item.read" class="notice-accent"></view>
						<view class="notice-icon" :class="{ dim: item.read }">
							<up-icon name="bell-fill" size="18" :color="item.read ? '#9AA5B6' : '#4E88F6'"></up-icon>
						</view>
						<view class="notice-body">
							<view class="notice-head">
								<text class="notice-title" :class="{ muted: item.read }">{{ item.title }}</text>
								<view class="notice-side">
									<text class="notice-time">{{ item.time }}</text>
									<view v-if="!item.read" class="notice-state">未读</view>
								</view>
							</view>
							<text class="notice-copy" :class="{ muted: item.read }">{{ item.content }}</text>
						</view>
						<view class="notice-arrow">
							<up-icon name="arrow-right" size="14" :color="item.read ? '#B3BDCB' : '#7FA8F8'"></up-icon>
						</view>
					</view>
				</view>
			</view>
		</view>
	</view>
</template>

<script setup>
	import { computed, ref } from 'vue';
	import { onShow } from '@dcloudio/uni-app';
	import { markNoticeRead } from '@/common/app-data';
	import {
		fetchNoticeList,
		markAllNoticesReadAsync,
		resolveNoticeJumpUrl,
		resolveNoticeListKey,
		resolveNoticeReadKey
	} from '@/common/messages';
	import { playMessageActionSound } from '@/common/sound-feedback';

	const notices = ref([]);
	const loading = ref(false);
	const readAllLoading = ref(false);
	const unreadCount = computed(() => notices.value.filter((item) => !item.read).length);

	const loadNotices = async () => {
		loading.value = true;
		try {
			const { list } = await fetchNoticeList();
			notices.value = list;
		} finally {
			loading.value = false;
		}
	};

	onShow(() => {
		loadNotices();
	});

	const readAll = async () => {
		if (readAllLoading.value) {
			return;
		}
		readAllLoading.value = true;
		try {
			notices.value = await markAllNoticesReadAsync(notices.value);
			playMessageActionSound();
			uni.showToast({ title: '已全部标记为已读', icon: 'none' });
		} finally {
			readAllLoading.value = false;
		}
	};

	/** Tab 主包页用 reLaunch，分包页用 navigateTo（与首页/我的 go 一致） */
	const goNotice = (url) => {
		if (url.startsWith('/pages/')) {
			uni.reLaunch({ url });
			return;
		}
		uni.navigateTo({ url });
	};

	const openNotice = (item) => {
		const readKey = resolveNoticeReadKey(item);
		notices.value = markNoticeRead(readKey, notices.value);
		playMessageActionSound();
		const url = resolveNoticeJumpUrl(item);
		if (url) {
			goNotice(url);
		}
	};
</script>

<style lang="scss" scoped>
	.notice-screen {
		padding-top: 20rpx;
		padding-bottom: 24rpx;
	}

	.notice-hero {
		position: relative;
		padding: 30rpx 28rpx;
		border-radius: 28rpx;
		background: linear-gradient(135deg, #FFFFFF 0%, #F4F8FF 100%);
		box-shadow: 0 16rpx 34rpx rgba(45, 86, 156, 0.08);
		overflow: hidden;
		display: flex;
		align-items: flex-start;
		justify-content: space-between;
		gap: 18rpx;

		&::after {
			content: '';
			position: absolute;
			right: -56rpx;
			top: -42rpx;
			width: 200rpx;
			height: 200rpx;
			border-radius: 50%;
			background: rgba(78, 136, 246, 0.08);
		}
	}

	.hero-copy {
		position: relative;
		z-index: 1;
		flex: 1;
		min-width: 0;
	}

	.hero-eyebrow {
		display: block;
		font-size: 20rpx;
		font-weight: 600;
		color: #4E88F6;
	}

	.hero-title {
		display: block;
		margin-top: 10rpx;
		font-size: 34rpx;
		font-weight: 700;
		line-height: 1.3;
		color: #23324A;
	}

	.hero-meta {
		display: block;
		margin-top: 12rpx;
		font-size: 22rpx;
		line-height: 1.5;
		color: #6B7A90;
	}

	.hero-action {
		position: relative;
		z-index: 1;
		height: 60rpx;
		padding: 0 18rpx;
		border-radius: 999rpx;
		background: linear-gradient(135deg, #4E88F6 0%, #6AA0FB 100%);
		display: flex;
		align-items: center;
		justify-content: center;
		gap: 8rpx;
		font-size: 22rpx;
		font-weight: 600;
		color: #fff;
		box-shadow: 0 12rpx 24rpx rgba(78, 136, 246, 0.2);
		transition: opacity 0.2s ease;
	}

	.hero-action:active {
		opacity: 0.8;
	}

	.hero-action.disabled {
		opacity: 0.5;
		pointer-events: none;
	}

	.notice-loading {
		margin-top: 96rpx;
		display: flex;
		flex-direction: column;
		align-items: center;
		gap: 20rpx;
	}

	.notice-list {
		margin-top: 24rpx;
		display: flex;
		flex-direction: column;
		gap: 16rpx;
	}

	.notice-card {
		position: relative;
		padding: 28rpx 26rpx;
		display: flex;
		align-items: center;
		gap: 18rpx;
		border-radius: 26rpx;
		border: 1rpx solid rgba(102, 144, 214, 0.08);
		background: #FFFFFF;
		box-shadow: 0 12rpx 28rpx rgba(33, 70, 136, 0.06);
		transition: transform 0.15s ease, box-shadow 0.2s ease, background 0.2s ease;
	}

	.notice-card:active {
		transform: scale(0.99);
	}

	.notice-card.is-unread {
		background: linear-gradient(135deg, #FFFFFF 0%, #F6FAFF 100%);
		box-shadow: 0 14rpx 30rpx rgba(78, 136, 246, 0.08);
	}

	.notice-card.is-read {
		background: #FFFFFF;
	}

	.notice-accent {
		position: absolute;
		left: 0;
		top: 20rpx;
		bottom: 20rpx;
		width: 8rpx;
		border-radius: 0 999rpx 999rpx 0;
		background: linear-gradient(180deg, #4E88F6 0%, #79ABFA 100%);
	}

	.notice-icon {
		width: 76rpx;
		height: 76rpx;
		border-radius: 22rpx;
		background: linear-gradient(180deg, #EEF5FF 0%, #E6F0FF 100%);
		display: flex;
		align-items: center;
		justify-content: center;
		flex-shrink: 0;
		box-shadow: inset 0 1rpx 0 rgba(255, 255, 255, 0.5);
	}

	.notice-icon.dim {
		background: #F4F7FB;
	}

	.notice-body {
		flex: 1;
		min-width: 0;
	}

	.notice-head {
		display: flex;
		justify-content: space-between;
		align-items: flex-start;
		gap: 18rpx;
	}

	.notice-title {
		flex: 1;
		font-size: 29rpx;
		font-weight: 700;
		line-height: 1.35;
		color: #22324B;
	}

	.notice-title.muted,
	.notice-copy.muted {
		color: #93A0B1;
	}

	.notice-side {
		display: flex;
		align-items: center;
		gap: 10rpx;
		flex-shrink: 0;
	}

	.notice-time {
		font-size: 20rpx;
		color: #9AA5B6;
	}

	.notice-state {
		padding: 4rpx 10rpx;
		border-radius: 999rpx;
		background: rgba(78, 136, 246, 0.1);
		font-size: 18rpx;
		font-weight: 600;
		color: #4E88F6;
	}

	.notice-copy {
		margin-top: 12rpx;
		display: block;
		font-size: 23rpx;
		line-height: 1.65;
		color: #607086;
	}

	.notice-arrow {
		width: 40rpx;
		height: 40rpx;
		border-radius: 50%;
		background: #F2F6FD;
		display: flex;
		align-items: center;
		justify-content: center;
		flex-shrink: 0;
	}
</style>
