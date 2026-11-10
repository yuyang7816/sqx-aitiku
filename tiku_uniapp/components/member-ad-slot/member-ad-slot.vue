<template>
	<view v-if="visible" class="member-ad-slot card" @tap="handleTap">
		<text class="ad-tag">广告</text>
		<view class="ad-body">
			<view class="ad-icon" :style="{ background: ad.iconBg }">
				<up-icon :name="ad.icon" size="22" :color="ad.iconColor"></up-icon>
			</view>
			<view class="ad-copy">
				<text class="ad-title">{{ ad.title }}</text>
				<text class="ad-sub">{{ ad.subtitle }}</text>
			</view>
			<up-icon name="arrow-right" size="14" color="#C0C6D4"></up-icon>
		</view>
	</view>
</template>

<script setup>
	import { computed, ref } from 'vue';
	import { onShow } from '@dcloudio/uni-app';
	import { getMemberState } from '@/common/app-data';
	import { getMemberAdByScene, openMemberAdLink } from '@/common/member-ad';

	const props = defineProps({
		/** 展示场景：home / ai / practice / notebook / my */
		scene: {
			type: String,
			default: 'home'
		}
	});

	const member = ref(getMemberState());

	onShow(() => {
		member.value = getMemberState();
	});

	const visible = computed(() => false);
	const ad = computed(() => getMemberAdByScene(props.scene));

	const handleTap = () => {
		openMemberAdLink(ad.value.link);
	};
</script>

<style lang="scss" scoped>
	.member-ad-slot {
		margin-top: 24rpx;
		padding: 24rpx 28rpx;
		position: relative;
		overflow: hidden;
	}

	.ad-tag {
		position: absolute;
		top: 0;
		right: 0;
		padding: 4rpx 16rpx;
		font-size: 20rpx;
		color: #97a0af;
		background: #f3f5f8;
		border-bottom-left-radius: 20rpx;
	}

	.ad-body {
		display: flex;
		align-items: center;
		gap: 20rpx;
	}

	.ad-icon {
		width: 80rpx;
		height: 80rpx;
		border-radius: 24rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		flex-shrink: 0;
	}

	.ad-copy {
		flex: 1;
		min-width: 0;
		display: flex;
		flex-direction: column;
		gap: 6rpx;
	}

	.ad-title {
		font-size: 28rpx;
		font-weight: 700;
		color: #243041;
		line-height: 1.4;
	}

	.ad-sub {
		font-size: 22rpx;
		color: #97a0af;
		line-height: 1.5;
	}
</style>
