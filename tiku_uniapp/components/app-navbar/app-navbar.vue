<template>
	<up-navbar
		:bgColor="bgColor"
		:statusBarBgColor="statusBarBgColor || bgColor"
		leftIcon=""
		title=""
		:fixed="fixed"
		:placeholder="placeholder"
		:border="false"
		:safeAreaInsetTop="safeAreaInsetTop"
		:height="height"
	>
		<template #left>
			<slot name="left">
				<view class="nav-left" @tap="handleLeftClick">
					<view v-if="back" class="nav-back">
						<up-icon name="arrow-left" :size="iconSize" :color="iconColor"></up-icon>
					</view>
					<text v-if="title" class="nav-title" :class="{ 'has-back': back }">{{ title }}</text>
				</view>
			</slot>
		</template>
		<template #center>
			<slot name="center"></slot>
		</template>
		<template #right>
			<slot name="right"></slot>
		</template>
	</up-navbar>
</template>

<script setup>
	/**
	 * 统一自定义导航栏：返回、标题、left/center/right 插槽，顶部安全区由 up-navbar 适配。
	 */
	const emit = defineEmits(['back']);

	const props = defineProps({
		title: {
			type: String,
			default: ''
		},
		back: {
			type: Boolean,
			default: false
		},
		fallbackUrl: {
			type: String,
			default: '/pages/index/index'
		},
		bgColor: {
			type: String,
			default: '#FFFFFF'
		},
		statusBarBgColor: {
			type: String,
			default: ''
		},
		safeAreaInsetTop: {
			type: Boolean,
			default: true
		},
		height: {
			type: [String, Number],
			default: '44px'
		},
		fixed: {
			type: Boolean,
			default: true
		},
		placeholder: {
			type: Boolean,
			default: true
		},
		iconColor: {
			type: String,
			default: '#243041'
		},
		iconSize: {
			type: [String, Number],
			default: 18
		}
	});

	const handleLeftClick = () => {
		if (!props.back) {
			return;
		}
		emit('back');
		if (getCurrentPages().length > 1) {
			uni.navigateBack();
			return;
		}
		const fallbackUrl = String(props.fallbackUrl || '').trim() || '/pages/index/index';
		uni.reLaunch({ url: fallbackUrl });
	};
</script>

<style lang="scss" scoped>
	.nav-left {
		height: 100%;
		display: flex;
		align-items: center;
		padding-left: 8rpx;
	}

	.nav-back {
		width: 56rpx;
		height: 56rpx;
		border-radius: 24rpx;
		background: rgba(36, 48, 65, 0.04);
		display: flex;
		align-items: center;
		justify-content: center;
		transition: background 0.2s ease;
	}

	.nav-back:active {
		background: rgba(36, 48, 65, 0.08);
	}

	.nav-title {
		font-size: 32rpx;
		font-weight: 700;
		color: #243041;
		line-height: 1;
	}

	.nav-title.has-back {
		margin-left: 16rpx;
	}
</style>
