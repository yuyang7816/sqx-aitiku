<template>
	<view class="profile-avatar" :class="[sizeClass, shapeClass]" :style="boxStyle">
		<image
			v-if="displayUrl"
			class="profile-avatar-image"
			:src="displayUrl"
			mode="aspectFill"
		></image>
		<text v-else class="profile-avatar-text">{{ avatarText }}</text>
	</view>
</template>

<script setup>
	import { computed } from 'vue';
	import { resolveProfileAvatarUrl, toAvatarText } from '@/common/app-data';

	const props = defineProps({
		/** 用户资料对象（含 avatar / avatarText / name） */
		profile: {
			type: Object,
			default: () => ({})
		},
		/** 尺寸：sm / md / lg */
		size: {
			type: String,
			default: 'md'
		},
		/** 圆角：square / round */
		shape: {
			type: String,
			default: 'square'
		}
	});

	const sizeClass = computed(() => `size-${props.size}`);
	const shapeClass = computed(() => `shape-${props.shape}`);

	const displayUrl = computed(() => resolveProfileAvatarUrl(props.profile?.avatar));

	const avatarText = computed(() => {
		if (props.profile?.avatarText) {
			return props.profile.avatarText;
		}
		return toAvatarText(props.profile?.name);
	});

	const boxStyle = computed(() => ({}));
</script>

<style lang="scss" scoped>
	.profile-avatar {
		overflow: hidden;
		display: flex;
		align-items: center;
		justify-content: center;
		background: linear-gradient(135deg, #1c2848 0%, #7190d9 100%);
		color: #fff;
		font-weight: 700;
		flex-shrink: 0;
	}

	.size-sm {
		width: 80rpx;
		height: 80rpx;
		border-radius: 26rpx;
		font-size: 36rpx;
	}

	.size-md {
		width: 112rpx;
		height: 112rpx;
		border-radius: 30rpx;
		font-size: 46rpx;
	}

	.size-lg {
		width: 128rpx;
		height: 128rpx;
		border-radius: 36rpx;
		font-size: 52rpx;
	}

	.shape-round.size-sm {
		border-radius: 50%;
	}

	.shape-round.size-md,
	.shape-round.size-lg {
		border-radius: 50%;
	}

	.profile-avatar-image {
		width: 100%;
		height: 100%;
	}

	.profile-avatar-text {
		line-height: 1;
	}
</style>
