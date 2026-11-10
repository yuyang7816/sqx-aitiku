<template>
	<view v-if="show" class="app-empty" :style="{ paddingTop }">
		<up-empty
			:mode="resolvedMode"
			:text="content"
			:textSize="textSize"
			:textColor="textColor"
			:iconSize="iconSize"
			:iconColor="iconColor"
		></up-empty>
		<text v-if="subContent" class="app-empty__sub">{{ subContent }}</text>
		<view v-if="buttonText" class="app-empty__btn" @tap="handleButton">
			<up-icon v-if="buttonIcon" :name="buttonIcon" size="16" color="#386df5"></up-icon>
			<text>{{ buttonText }}</text>
		</view>
		<slot></slot>
	</view>
</template>

<script setup>
	/**
	 * 统一空态组件：图标（up-empty mode）、主/副文案、可选引导按钮。
	 * 错题本、收藏、消息中心等列表无数据时复用；加载中请用 v-if 避免闪空态。
	 */
	import { computed } from 'vue';
	import { EMPTY_ICON_COLOR, EMPTY_MODES, EMPTY_TEXT_COLOR } from '@/common/empty';

	const emit = defineEmits(['button']);

	const props = defineProps({
		/** 主文案，如「暂无错题」 */
		content: {
			type: String,
			default: '暂无数据'
		},
		/** 副文案/引导说明 */
		subContent: {
			type: String,
			default: ''
		},
		/** up-empty 图标类型，见 common/empty.js EMPTY_MODES */
		mode: {
			type: String,
			default: EMPTY_MODES.LIST
		},
		/** 兼容旧 prop：与 mode 等价，mode 优先 */
		image: {
			type: String,
			default: ''
		},
		paddingTop: {
			type: String,
			default: '200rpx'
		},
		textSize: {
			type: [String, Number],
			default: 14
		},
		textColor: {
			type: String,
			default: EMPTY_TEXT_COLOR
		},
		iconSize: {
			type: [String, Number],
			default: 120
		},
		iconColor: {
			type: String,
			default: EMPTY_ICON_COLOR
		},
		buttonText: {
			type: String,
			default: ''
		},
		buttonIcon: {
			type: String,
			default: ''
		},
		show: {
			type: Boolean,
			default: true
		}
	});

	const resolvedMode = computed(() => {
		if (props.image) {
			return props.image;
		}
		return props.mode || EMPTY_MODES.LIST;
	});

	const handleButton = () => {
		emit('button');
	};
</script>

<style lang="scss" scoped>
	.app-empty {
		display: flex;
		flex-direction: column;
		align-items: center;
	}

	.app-empty__sub {
		margin-top: 12rpx;
		padding: 0 40rpx;
		font-size: 24rpx;
		color: #97a0af;
		text-align: center;
		line-height: 1.6;
	}

	.app-empty__btn {
		margin-top: 32rpx;
		padding: 0 40rpx;
		height: 72rpx;
		border-radius: 999rpx;
		background: #eef4ff;
		color: #386df5;
		font-size: 28rpx;
		font-weight: 700;
		display: flex;
		align-items: center;
		justify-content: center;
		gap: 12rpx;
	}

	/* #ifdef H5 */
	.app-empty__btn {
		cursor: pointer;
	}
	/* #endif */
</style>
