<template>
	<view class="success-page">
		<view class="success-wrap">
			<view class="success-badge">
				<view class="badge-ring"></view>
				<up-icon name="checkmark" size="44" color="#FFFFFF"></up-icon>
			</view>
			<text class="success-title">开通成功</text>
			<text class="success-sub">{{ successSubText }}</text>
			<text v-if="expireLabel" class="success-expire">{{ expireLabel }}</text>

			<view v-if="previewRights.length" class="rights-preview card">
				<view v-for="item in previewRights" :key="item.code || item.label" class="rights-row-item">
					<up-icon :name="item.iconName" size="18" :color="item.iconColor"></up-icon>
					<text>{{ item.displayText }}</text>
				</view>
			</view>

			<view class="solid-button" @tap="goHome">
				<text>回首页</text>
			</view>
			<view class="ghost-button mt" @tap="goOrderList">查看订单详情</view>
			<view class="ghost-button mt subtle-btn" @tap="goMemberCenter">会员中心</view>
		</view>
	</view>
</template>

<script setup>
	import { computed, ref } from 'vue';
	import { onLoad, onShow } from '@dcloudio/uni-app';
	import { getMemberState } from '@/common/app-data';
	import { MEMBER_CENTER_PAGE, MEMBER_ORDER_LIST_PAGE } from '@/common/routes';
	import { fetchMemberRightsCompare } from '@/common/vip-rights-api';
	import { fetchAndMergeUserInfo } from '@/common/user-info';

	const planName = ref('');
	const member = ref(getMemberState());
	const previewRights = ref([]);

	/** 权益项 code 对应图标（与会员中心语义一致） */
	const resolveRightsIcon = (code = '') => {
		const map = {
			ai: { name: 'chat-fill', color: '#16a34a' },
			ocr: { name: 'camera-fill', color: '#386df5' },
			video: { name: 'play-circle-fill', color: '#f59e0b' },
			report: { name: 'column-line', color: '#8a62f8' },
			ad_free: { name: 'checkmark-circle-fill', color: '#386df5' }
		};
		return map[code] || { name: 'checkmark-circle-fill', color: '#386df5' };
	};

	/** 开通成功副标题：展示所选套餐名 */
	const successSubText = computed(() => {
		const plan = planName.value || member.value.currentPlanName || '';
		if (plan && plan !== '未开通') {
			return `已开通${plan}，会员权益已全部生效`;
		}
		return '你的会员权益已全部生效';
	});

	/** 会员有效期（支付成功后 GET /app/user 已写入本地） */
	const expireLabel = computed(() => {
		const expireAt = String(member.value.expireAt || '').trim();
		return expireAt ? `有效期至 ${expireAt}` : '';
	});

	/** 同步会员态：pay 页已激活，此处仅拉服务端 vip 并刷新展示 */
	const syncMemberState = async () => {
		try {
			await fetchAndMergeUserInfo();
		} catch (e) {
			// 网络异常时仍展示本地缓存
		}
		member.value = getMemberState();
	};

	/** 权益预览：GET /app/vipRights 前三项，失败回退 mock */
	const loadRightsPreview = async () => {
		const { list } = await fetchMemberRightsCompare();
		previewRights.value = list.slice(0, 3).map((item) => {
			const icon = resolveRightsIcon(item.code);
			const vipText = String(item.vip || '').trim();
			return {
				code: item.code,
				label: item.label,
				iconName: icon.name,
				iconColor: icon.color,
				displayText: vipText ? `${item.label} · ${vipText}` : item.label
			};
		});
	};

	onLoad((options = {}) => {
		planName.value = decodeURIComponent(String(options.planName || '')).trim();
	});

	onShow(() => {
		syncMemberState();
		loadRightsPreview();
	});

	/** 回首页 Tab（清空支付栈，避免返回 success/pay） */
	const goHome = () => {
		uni.reLaunch({ url: '/pages/index/index' });
	};

	/** 查看历史订单列表（success 页主路径，非仅回会员中心） */
	const goOrderList = () => {
		uni.navigateTo({ url: MEMBER_ORDER_LIST_PAGE });
	};

	/** 回会员中心（replace 当前页，避免栈内重复 success） */
	const goMemberCenter = () => {
		uni.redirectTo({ url: MEMBER_CENTER_PAGE });
	};
</script>

<style lang="scss" scoped>
	.success-page {
		min-height: 100vh;
		background: linear-gradient(180deg, #f0f5ff 0%, #ffffff 40%);
		display: flex;
		flex-direction: column;
	}

	.success-wrap {
		flex: 1;
		min-height: 0;
		padding: 60rpx 48rpx calc(56rpx + env(safe-area-inset-bottom));
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: center;
	}

	.success-badge {
		width: 160rpx;
		height: 160rpx;
		border-radius: 50%;
		background: linear-gradient(135deg, #22c55e 0%, #16a34a 100%);
		box-shadow: 0 20rpx 40rpx rgba(34, 197, 94, 0.3);
		display: flex;
		align-items: center;
		justify-content: center;
		position: relative;
	}

	.badge-ring {
		position: absolute;
		inset: -24rpx;
		border-radius: 50%;
		border: 2rpx solid rgba(34, 197, 94, 0.25);
	}

	.success-title {
		margin-top: 56rpx;
		font-size: 54rpx;
		font-weight: 800;
		color: var(--text-main);
	}

	.success-sub {
		margin-top: 18rpx;
		font-size: var(--text-lg);
		color: #97a0af;
		text-align: center;
	}

	.success-expire {
		margin-top: 12rpx;
		font-size: var(--text-base);
		color: var(--primary);
		font-weight: 600;
	}

	.rights-preview {
		margin-top: 48rpx;
		width: 100%;
		padding: 28rpx 32rpx;
		display: flex;
		flex-direction: column;
		gap: 22rpx;
	}

	.rights-row-item {
		display: flex;
		align-items: center;
		gap: 16rpx;
		font-size: var(--text-lg);
		font-weight: 600;
		color: var(--text-main);
	}

	.solid-button {
		margin-top: 56rpx;
		width: 100%;
	}

	.mt {
		margin-top: 24rpx;
		width: 100%;
	}

	.subtle-btn {
		opacity: 0.92;
	}
</style>
