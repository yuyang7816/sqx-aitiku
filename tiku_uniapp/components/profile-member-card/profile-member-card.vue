<template>
	<view class="member-card" :class="{ 'is-vip': member.isVip }" @tap="goMemberCenter">
		<text class="member-bg-word">VIP</text>
		<view class="member-left">
			<view class="member-title-row">
				<text class="member-title">{{ memberLevel }}</text>
			</view>
			<view class="member-status-row">
				<view class="member-chip">
					<text>{{ memberChipText }}</text>
				</view>
				<text v-if="member.isVip" class="member-state-tag">已开通</text>
			</view>
			<view class="member-benefits">
				<view v-for="item in benefitList" :key="item" class="member-benefit">
					<up-icon name="checkmark-circle-fill" size="12" color="#4E88F6"></up-icon>
					<text>{{ item }}</text>
				</view>
			</view>
		</view>
		<view class="member-btn">
			<text>{{ actionText }}</text>
			<up-icon name="arrow-right" size="14" :color="member.isVip ? '#3B2D12' : '#FFFFFF'"></up-icon>
		</view>
	</view>
</template>

<script setup>
	import { computed } from 'vue';

	const props = defineProps({
		/** 会员态：isVip / level / expireAt，来自 getMemberState() */
		member: {
			type: Object,
			default: () => ({})
		},
		/** GET /app/vipRights 预览文案，最多 2 条 */
		benefits: {
			type: Array,
			default: () => []
		}
	});

	/** 卡片主标题：会员等级 */
	const memberLevel = computed(() => props.member.level || '普通会员');

	const memberChipText = computed(() => (props.member.isVip ? '权益生效中' : '开通提醒'));

	/** 副文案：VIP 展示有效期，非 VIP 展示开通引导 */
	const memberSub = computed(() => {
		if (props.member.isVip) {
			const expireAt = String(props.member.expireAt || '').trim();
			return expireAt ? `有效期至 ${expireAt}` : '会员权益已生效';
		}
		return '升级后可享无限次 AI 搜题';
	});

	/** 操作按钮：VIP 续费，非 VIP 开通 */
	const actionText = computed(() => (props.member.isVip ? '立即续费' : '立即开通'));

	const benefitList = computed(() => {
		if (props.benefits.length) {
			return props.benefits;
		}
		if (props.member.isVip) {
			return ['有效期内可用', memberSub.value];
		}
		return ['开通后享更多学习权益'];
	});

	const goMemberCenter = () => {
		uni.navigateTo({
			url: '/package/member/center'
		});
	};
</script>

<style lang="scss" scoped>
	.member-card {
		margin: 18rpx 0 0;
		padding: 16rpx 20rpx;
		border-radius: 24rpx;
		background: linear-gradient(135deg, #ffffff 0%, #f8fbff 100%);
		overflow: hidden;
		border: 1rpx solid rgba(102, 144, 214, 0.12);
		display: flex;
		align-items: center;
		gap: 16rpx;
		justify-content: space-between;
		position: relative;
		z-index: 1;
		box-shadow: 0 12rpx 28rpx rgba(45, 86, 156, 0.08);
	}

	.member-card.is-vip {
		background: linear-gradient(135deg, #ffffff 0%, #f4f9ff 100%);
	}

	.member-card::after {
		content: '';
		position: absolute;
		right: -36rpx;
		bottom: -48rpx;
		width: 220rpx;
		height: 220rpx;
		border-radius: 50%;
		background: rgba(190, 222, 255, 0.26);
	}

	.member-card::before {
		content: '';
		position: absolute;
		left: -40rpx;
		top: -40rpx;
		width: 140rpx;
		height: 140rpx;
		border-radius: 50%;
		background: rgba(103, 154, 247, 0.08);
	}

	.member-left {
		position: relative;
		z-index: 1;
		flex: 1;
		min-width: 0;
	}

	.member-bg-word {
		position: absolute;
		right: 154rpx;
		top: 50%;
		transform: translateY(-50%);
		font-size: 144rpx;
		font-weight: 900;
		font-style: italic;
		line-height: 1;
		letter-spacing: 4rpx;
		color: rgba(78, 136, 246, 0.05);
		-webkit-text-stroke: 2rpx rgba(78, 136, 246, 0.2);
		text-shadow: 0 8rpx 20rpx rgba(78, 136, 246, 0.06);
		pointer-events: none;
		z-index: 0;
	}

	.member-chip {
		display: inline-flex;
		align-items: center;
		padding: 6rpx 12rpx;
		border-radius: 999rpx;
		background: rgba(78, 136, 246, 0.08);
		font-size: 17rpx;
		font-weight: 600;
		color: #4E88F6;
	}

	.member-title-row {
		display: flex;
		align-items: center;
	}

	.member-status-row {
		margin-top: 8rpx;
		display: flex;
		align-items: center;
		gap: 10rpx;
	}

	.member-title {
		display: block;
		font-size: 30rpx;
		font-weight: 700;
		color: #243041;
		line-height: 1.3;
		position: relative;
		z-index: 1;
	}

	.member-state-tag {
		flex-shrink: 0;
		padding: 3rpx 10rpx;
		border-radius: 999rpx;
		background: rgba(78, 136, 246, 0.1);
		font-size: 16rpx;
		font-weight: 600;
		color: #4E88F6;
	}

	.member-benefits {
		margin-top: 10rpx;
		display: flex;
		flex-wrap: wrap;
		gap: 8rpx 10rpx;
	}

	.member-benefit {
		display: inline-flex;
		align-items: center;
		gap: 6rpx;
		padding: 5rpx 10rpx;
		border-radius: 999rpx;
		background: #F1F6FF;
		font-size: 17rpx;
		color: #355886;
		border: 1rpx solid rgba(102, 144, 214, 0.1);
	}

	.member-btn {
		height: 46rpx;
		padding: 0 16rpx;
		border-radius: 999rpx;
		background: linear-gradient(135deg, #4e88f6 0%, #6aa0fb 100%);
		display: flex;
		align-items: center;
		justify-content: center;
		gap: 8rpx;
		font-size: 18rpx;
		font-weight: 600;
		color: #fff;
		flex-shrink: 0;
		box-shadow: 0 10rpx 20rpx rgba(78, 136, 246, 0.2);
		position: relative;
		z-index: 1;
	}

	.is-vip .member-btn {
		background: linear-gradient(135deg, #e8b020 0%, #d49e13 100%);
		color: #3b2d12;
	}

	@media screen and (max-width: 420px) {
		.member-card {
			padding: 16rpx 18rpx;
		}

		.member-title {
			font-size: 28rpx;
		}

		.member-btn {
			padding: 0 14rpx;
		}

		.member-bg-word {
			right: 126rpx;
			font-size: 124rpx;
		}
	}
</style>
