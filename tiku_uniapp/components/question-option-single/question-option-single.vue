<template>
	<view class="option-list">
		<view
			v-for="item in normalizedOptions"
			:key="item.label"
			class="option-item"
			:class="{ active: isOptionSelected(item.label) }"
			@tap="handleSelect(item.label)"
		>
			<view class="option-dot" :class="{ active: isOptionSelected(item.label) }">{{ item.label }}</view>
			<text class="option-text" :class="{ active: isOptionSelected(item.label) }">{{ item.value }}</text>
		</view>
	</view>
</template>

<script setup>
	import { computed } from 'vue';

	const props = defineProps({
		/** 当前选中选项 label（单选仅一项） */
		modelValue: {
			type: String,
			default: ''
		},
		/** 选项列表：{ label, value } */
		options: {
			type: Array,
			default: () => []
		}
	});

	const emit = defineEmits(['update:modelValue', 'select']);

	const normalizeLabel = (label = '') => String(label || '').trim().toUpperCase();

	const normalizedOptions = computed(() => (
		Array.isArray(props.options)
			? props.options
				.filter((item) => item && (item.label || item.value))
				.map((item) => ({
					label: normalizeLabel(item.label),
					value: String(item.value ?? '').trim()
				}))
			: []
	));

	const selectedLabel = computed(() => normalizeLabel(props.modelValue));

	/** 判断选项是否选中（单选互斥：同一时刻仅一项 active） */
	const isOptionSelected = (label) => {
		const current = selectedLabel.value;
		if (!current) {
			return false;
		}
		return normalizeLabel(label) === current;
	};

	/** 点击选项：覆盖旧选中项，保证单选互斥 */
	const handleSelect = (label) => {
		const nextLabel = normalizeLabel(label);
		if (!nextLabel || nextLabel === selectedLabel.value) {
			return;
		}
		emit('update:modelValue', nextLabel);
		emit('select', nextLabel);
	};
</script>

<style lang="scss" scoped>
	.option-list {
		display: flex;
		flex-direction: column;
		gap: 14rpx;
	}

	.option-item {
		min-height: 104rpx;
		border-radius: 28rpx;
		padding: 20rpx 22rpx;
		display: flex;
		align-items: center;
		gap: 20rpx;
		background: linear-gradient(180deg, rgba(255, 255, 255, 0.98) 0%, rgba(247, 250, 255, 0.98) 100%);
		box-shadow: 0 16rpx 32rpx rgba(15, 23, 42, 0.05);
		border: 2rpx solid rgba(228, 233, 242, 0.95);
		transition: all 0.2s ease;
		position: relative;
		overflow: hidden;
	}

	.option-item::before {
		content: '';
		position: absolute;
		left: 0;
		top: 0;
		bottom: 0;
		width: 8rpx;
		background: transparent;
	}

	.option-item:active {
		transform: translateY(2rpx);
	}

	.option-item.active {
		background: linear-gradient(180deg, #eef4ff 0%, #e7efff 100%);
		border-color: #386df5;
		box-shadow: 0 18rpx 34rpx rgba(56, 109, 245, 0.14);
	}

	.option-item.active::before {
		background: linear-gradient(180deg, #386df5 0%, #78a0ff 100%);
	}

	.option-dot {
		width: 54rpx;
		height: 54rpx;
		border-radius: 50%;
		border: 2rpx solid #d4dbe8;
		background: #ffffff;
		font-size: 21rpx;
		font-weight: 700;
		color: #98a2b3;
		display: flex;
		align-items: center;
		justify-content: center;
		flex-shrink: 0;
		box-shadow: 0 8rpx 16rpx rgba(15, 23, 42, 0.04);
	}

	.option-dot.active {
		background: linear-gradient(135deg, #386df5 0%, #5d8eff 100%);
		border-color: #386df5;
		color: #fff;
	}

	.option-text {
		font-size: 23rpx;
		font-weight: 600;
		color: #253143;
		line-height: 1.5;
		transition: color 0.2s ease;
		flex: 1;
	}

	.option-text.active {
		color: #1f4fbf;
	}
</style>
