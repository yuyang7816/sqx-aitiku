<template>
	<view class="fill-wrap">
		<text class="fill-hint">填空题，请在下方输入答案</text>
		<input
			class="fill-input"
			type="text"
			:value="modelValue"
			:placeholder="placeholder"
			placeholder-class="fill-placeholder"
			confirm-type="done"
			@input="handleInput"
		/>
	</view>
</template>

<script setup>
	defineProps({
		/** 用户填写的答案文本 */
		modelValue: {
			type: String,
			default: ''
		},
		placeholder: {
			type: String,
			default: '请输入答案'
		}
	});

	const emit = defineEmits(['update:modelValue', 'input']);

	const handleInput = (event) => {
		const value = String(event?.detail?.value ?? '').trimStart();
		emit('update:modelValue', value);
		emit('input', value);
	};
</script>

<style lang="scss" scoped>
	.fill-wrap {
		display: flex;
		flex-direction: column;
		gap: 14rpx;
	}

	.fill-hint {
		font-size: 20rpx;
		color: #7b8698;
		padding-left: 4rpx;
	}

	.fill-input {
		min-height: 96rpx;
		border-radius: 28rpx;
		border: 2rpx solid rgba(228, 233, 242, 0.95);
		padding: 0 28rpx;
		font-size: 23rpx;
		font-weight: 600;
		color: #253143;
		background: linear-gradient(180deg, rgba(255, 255, 255, 0.98) 0%, rgba(247, 250, 255, 0.98) 100%);
		box-shadow: 0 16rpx 32rpx rgba(15, 23, 42, 0.05);
		transition: all 0.2s ease;
	}

	.fill-input:focus {
		border-color: #386df5;
		box-shadow: 0 18rpx 34rpx rgba(56, 109, 245, 0.12);
	}

	.fill-placeholder {
		color: #b8c0cf;
		font-weight: 500;
	}
</style>
