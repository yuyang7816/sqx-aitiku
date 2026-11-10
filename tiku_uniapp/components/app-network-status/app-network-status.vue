<template>
	<view v-if="visible" class="app-network-status">
		<view class="app-network-status__safe" :style="safeAreaStyle"></view>
		<view class="app-network-status__body">
			<up-icon name="error-circle-fill" size="16" color="#FFFFFF"></up-icon>
			<text class="app-network-status__text">当前无网络连接，请检查网络设置</text>
		</view>
	</view>
</template>

<script setup>
	import { computed, onBeforeUnmount, onMounted, ref } from 'vue';
	import { subscribeNetworkStatus } from '@/common/network-status';

	/** 断网时顶部固定提示，恢复网络后自动隐藏 */
	const visible = ref(false);
	const statusBarHeight = ref(0);
	let unsubscribe = null;

	const safeAreaStyle = computed(() => ({
		height: `${statusBarHeight.value}px`
	}));

	onMounted(() => {
		statusBarHeight.value = Number(uni.getWindowInfo?.().statusBarHeight || 0);
		unsubscribe = subscribeNetworkStatus((connected) => {
			visible.value = !connected;
		});
	});

	onBeforeUnmount(() => {
		if (typeof unsubscribe === 'function') {
			unsubscribe();
			unsubscribe = null;
		}
	});
</script>

<style lang="scss" scoped>
	.app-network-status {
		position: fixed;
		top: 0;
		left: 0;
		right: 0;
		z-index: 10090;
		background-color: #ef4444;
		box-shadow: 0 8rpx 24rpx rgba(239, 68, 68, 0.28);
	}

	.app-network-status__body {
		display: flex;
		align-items: center;
		justify-content: center;
		gap: 12rpx;
		min-height: 72rpx;
		padding: 12rpx 32rpx 16rpx;
	}

	.app-network-status__safe {
		width: 100%;
	}

	.app-network-status__text {
		color: #ffffff;
		font-size: 26rpx;
		line-height: 1.4;
	}
</style>
