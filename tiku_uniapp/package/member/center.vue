<template>
	<view class="app-page">
		<view class="page-scroll">
			<view class="screen member-screen">
				<view class="member-hero">
					<view class="member-orb orb-a"></view>
					<view class="member-orb orb-b"></view>
					<text class="hero-bg-word">VIP</text>
					<view class="member-user">
						<profile-avatar :profile="profile" size="md"></profile-avatar>
						<view class="user-copy">
							<text class="name">{{ profile.name }}</text>
						</view>
						<view class="vip-badge" :class="{ active: member.isVip }">
							<up-icon name="coupon-fill" size="12" :color="member.isVip ? '#8A6D10' : '#4E88F6'"></up-icon>
							<text>{{ member.isVip ? 'VIP' : '未开通' }}</text>
						</view>
					</view>

					<view class="hero-main">
						<view class="hero-copy">
							<text class="hero-value">{{ expireValue }}</text>
							<text class="hero-meta">{{ member.isVip ? '当前会员权益已生效，可继续使用 AI 与拍照搜题能力' : '开通后可享 AI 无限、拍照搜题与更多学习权益' }}</text>
						</view>
					</view>
				</view>

				<view class="section-head mt32">
					<text class="section-title">会员权益对比</text>
					<text class="section-sub">免费版与 VIP 能力对照</text>
				</view>
				<view class="rights-card">
					<view class="rights-head">
						<text class="rights-head-label">权益内容</text>
						<view class="rights-head-free">免费</view>
						<view class="rights-head-vip gold">VIP</view>
					</view>
					<view v-for="item in rights" :key="item.code || item.label" class="rights-row">
						<text class="rights-label">{{ item.label }}</text>
						<view class="rights-free-cell">
							<text class="rights-free">{{ item.free }}</text>
						</view>
						<view class="rights-vip">
							<up-icon name="checkmark-circle-fill" size="14" color="#D49E13"></up-icon>
							<text class="gold">{{ item.vip }}</text>
						</view>
					</view>
				</view>

				<view class="section-head mt32">
					<text class="section-title">选择套餐</text>
					<text class="section-sub">按学习周期选择更合适的会员方案</text>
				</view>
				<view class="plan-row">
					<view v-for="item in plans" :key="item.name" class="plan-card" :class="{ active: item.name === selectedPlan.name }" @tap="selectedPlan = item">
						<text v-if="item.recommend" class="tag">推荐</text>
						<view v-if="item.name === selectedPlan.name" class="plan-check">
							<up-icon name="checkmark-circle-fill" size="18" color="#386df5"></up-icon>
						</view>
						<text class="plan-name">{{ item.name }}</text>
						<text v-if="item.days" class="plan-days">{{ item.days }} 天</text>
						<text class="price" :class="{ dark: item.name !== selectedPlan.name }">¥{{ item.price }}</text>
						<text class="line-price">¥{{ item.origin }}</text>
					</view>
				</view>
				<view class="open-btn" @tap="openPayPopup">
					<view class="open-btn-copy">
						<text class="open-btn-title">立即开通 {{ selectedPlan.name }}</text>
						<text class="open-btn-sub">会员权益即时生效</text>
					</view>
					<view class="open-btn-icon">
						<up-icon name="arrow-right" size="18" color="#FFFFFF"></up-icon>
					</view>
				</view>
			</view>
		</view>

		<member-pay-popup
			v-model:show="showPayPopup"
			:plan="payPlan"
			@success="handlePaySuccess"
		></member-pay-popup>
	</view>
</template>

<script setup>
	import ProfileAvatar from '@/components/profile-avatar/profile-avatar.vue';
	import MemberPayPopup from '@/components/member-pay-popup/member-pay-popup.vue';
	import { computed, ref } from 'vue';
	import { storeToRefs } from 'pinia';
	import { onLoad, onShow } from '@dcloudio/uni-app';
	import { refreshMemberExpireState } from '@/common/app-data';
	import { fetchAndMergeUserInfo } from '@/common/user-info';
	import { buildEmptyMemberPlans, fetchMemberPlans } from '@/common/vip-plan-api';
	import { buildEmptyMemberRights, fetchMemberRightsCompare } from '@/common/vip-rights-api';
	import { MEMBER_SUCCESS_PAGE } from '@/common/routes';
	import { useUserStore } from '@/stores/user';
	import { useMemberStore } from '@/stores/member';

	const userStore = useUserStore();
	const memberStore = useMemberStore();
	const { profile } = storeToRefs(userStore);
	const { member } = storeToRefs(memberStore);

	const rights = ref(buildEmptyMemberRights());
	const plans = ref(buildEmptyMemberPlans());
	const selectedPlan = ref({ name: '', price: 0, days: 0 });
	const showPayPopup = ref(false);
	const pendingOpenPay = ref(false);

	const payPlan = computed(() => selectedPlan.value || { name: '', price: 0, days: 0 });

	const loadMemberRights = async () => {
		const result = await fetchMemberRightsCompare();
		rights.value = result.list;
	};

	const loadMemberPlans = async () => {
		const result = await fetchMemberPlans();
		if (!result.list.length) {
			plans.value = [];
			selectedPlan.value = { name: '', price: 0, days: 0 };
			return;
		}
		const currentName = selectedPlan.value?.name;
		plans.value = result.list;
		const matched = result.list.find((item) => item.name === currentName);
		selectedPlan.value = matched || result.list[0];
	};

	onLoad((options = {}) => {
		const planName = decodeURIComponent(String(options.name || '')).trim();
		if (String(options.openPay || '') === '1' && planName) {
			selectedPlan.value = {
				name: planName,
				price: Number(options.price) || 0,
				days: Number(options.days) || 0
			};
			pendingOpenPay.value = true;
		}
	});

	onShow(async () => {
		refreshMemberExpireState();
		await fetchAndMergeUserInfo();
		await Promise.all([loadMemberRights(), loadMemberPlans()]);
		if (pendingOpenPay.value && selectedPlan.value?.name) {
			pendingOpenPay.value = false;
			showPayPopup.value = true;
		}
	});

	const expireLabel = computed(() => (member.value.isVip ? '有效期至' : '暂未开通会员'));
	const expireValue = computed(() => (member.value.isVip ? member.value.expireAt : '开通后可享 AI 无限'));

	const openPayPopup = () => {
		const plan = selectedPlan.value;
		if (!plan?.name) {
			uni.showToast({ title: '请选择会员套餐', icon: 'none' });
			return;
		}
		showPayPopup.value = true;
	};

	const handlePaySuccess = ({ planName, planDays } = {}) => {
		const query = `planName=${encodeURIComponent(planName || selectedPlan.value?.name || '')}&days=${planDays || selectedPlan.value?.days || 0}`;
		uni.redirectTo({ url: `${MEMBER_SUCCESS_PAGE}?${query}` });
	};
</script>

<style lang="scss" scoped>
	.member-screen {
		padding-top: 12rpx;
		padding-bottom: 32rpx;
		background: linear-gradient(180deg, #F4F7FF 0, #F8FAFD 260rpx, #F6F8FC 100%);
	}

	.member-hero {
		margin-top: 20rpx;
		padding: 34rpx 30rpx;
		border-radius: 32rpx;
		background: linear-gradient(155deg, #2E63D6 0%, #3569DD 30%, #4576E3 68%, #5D8CED 100%);
		color: #fff;
		position: relative;
		overflow: hidden;
		box-shadow: 0 22rpx 46rpx rgba(43, 78, 151, 0.22);
	}

	.member-orb {
		position: absolute;
		border-radius: 50%;
		background: rgba(255, 255, 255, 0.12);
		pointer-events: none;
	}

	.orb-a {
		right: -36rpx;
		top: 18rpx;
		width: 220rpx;
		height: 220rpx;
	}

	.orb-b {
		left: -72rpx;
		bottom: -92rpx;
		width: 280rpx;
		height: 280rpx;
		background: rgba(190, 222, 255, 0.18);
	}

	.member-user {
		display: flex;
		align-items: center;
		gap: 20rpx;
		position: relative;
		z-index: 1;
	}

	.hero-bg-word {
		position: absolute;
		right: 28rpx;
		top: 50%;
		transform: translateY(-50%);
		font-size: 156rpx;
		font-weight: 900;
		font-style: italic;
		line-height: 1;
		letter-spacing: 4rpx;
		color: rgba(255, 255, 255, 0.06);
		-webkit-text-stroke: 2rpx rgba(255, 255, 255, 0.14);
		text-shadow: 0 8rpx 20rpx rgba(255, 255, 255, 0.04);
		pointer-events: none;
		z-index: 0;
	}

	.user-copy {
		flex: 1;
		min-width: 0;
	}

	.name {
		display: block;
		font-size: 36rpx;
		font-weight: 700;
	}

	.vip-badge {
		display: flex;
		align-items: center;
		gap: 6rpx;
		padding: 8rpx 16rpx;
		border-radius: 999rpx;
		background: rgba(255, 255, 255, 0.14);
		color: #DDEBFF;
		font-size: 22rpx;
		font-weight: 600;
		flex-shrink: 0;
	}

	.vip-badge.active {
		background: linear-gradient(135deg, #F3C826 0%, #D49E13 100%);
		color: #8A6D10;
	}

	.hero-main {
		margin-top: 28rpx;
		display: flex;
		align-items: flex-end;
		justify-content: space-between;
		gap: 20rpx;
		position: relative;
		z-index: 1;
	}

	.hero-copy {
		flex: 1;
		min-width: 0;
	}

	.hero-label {
		display: block;
		font-size: 22rpx;
		color: rgba(255, 255, 255, 0.74);
	}

	.hero-value {
		display: block;
		margin-top: 10rpx;
		font-size: 42rpx;
		font-weight: 700;
		line-height: 1.2;
		color: #FFFFFF;
	}

	.hero-meta {
		display: block;
		margin-top: 12rpx;
		font-size: 22rpx;
		line-height: 1.55;
		color: rgba(255, 255, 255, 0.74);
	}

	.section-head {
		margin-bottom: 16rpx;
	}

	.section-sub {
		display: block;
		margin-top: 8rpx;
		font-size: 22rpx;
		line-height: 1.5;
		color: #7B8798;
	}

	.section-title {
		display: block;
		font-size: 30rpx;
		font-weight: 600;
		color: #22324B;
	}

	.mt32 {
		margin-top: 32rpx;
	}

	.rights-card {
		padding: 0;
		overflow: hidden;
		border-radius: 28rpx;
		background: linear-gradient(180deg, #FFFFFF 0%, #F9FBFF 100%);
		box-shadow: 0 12rpx 28rpx rgba(33, 70, 136, 0.06);
		border: 1rpx solid rgba(102, 144, 214, 0.08);
	}

	.rights-head,
	.rights-row {
		display: grid;
		grid-template-columns: 1.4fr 0.8fr 1fr;
		align-items: center;
		padding: 24rpx 26rpx;
		font-size: 24rpx;
	}

	.rights-head {
		background: linear-gradient(180deg, #F7FAFF 0%, #F2F7FF 100%);
		color: #7B8798;
		font-weight: 600;
	}

	.rights-head-label,
	.rights-head-free,
	.rights-head-vip {
		display: block;
		width: 100%;
	}

	.rights-head-label {
		text-align: left;
	}

	.rights-head-free,
	.rights-head-vip {
		display: flex;
		align-items: center;
		justify-content: flex-start;
		text-align: left;
	}

	.rights-row + .rights-row {
		border-top: 1rpx solid #EEF2F8;
	}

	.rights-label {
		color: #22324B;
		padding-right: 12rpx;
		line-height: 1.45;
	}

	.rights-free {
		color: #7B8798;
		text-align: left;
		display: block;
		width: 100%;
	}

	.rights-free-cell {
		width: 100%;
		display: flex;
		justify-content: flex-start;
		align-items: center;
	}

	.rights-vip {
		display: flex;
		align-items: center;
		justify-content: flex-start;
		gap: 8rpx;
		width: 100%;
	}

	.gold {
		color: #d49e13;
		font-weight: 600;
	}

	.plan-row {
		display: grid;
		grid-template-columns: repeat(3, 1fr);
		gap: 16rpx;
	}

	.plan-card {
		position: relative;
		padding: 32rpx 18rpx 24rpx;
		border-radius: 24rpx;
		background: linear-gradient(180deg, #FFFFFF 0%, #F6F8FC 100%);
		border: 2rpx solid #EEF2F8;
		text-align: center;
		box-shadow: 0 8rpx 20rpx rgba(33, 70, 136, 0.04);
	}

	.plan-card.active {
		border-color: #386df5;
		background: linear-gradient(180deg, #F3F8FF 0%, #EAF2FF 100%);
		box-shadow: 0 18rpx 34rpx rgba(56, 109, 245, 0.16);
	}

	.tag {
		position: absolute;
		top: 0;
		left: 50%;
		transform: translate(-50%, -50%);
		padding: 4rpx 16rpx;
		border-radius: 999rpx;
		background: #386df5;
		color: #fff;
		font-size: 20rpx;
	}

	.plan-check {
		position: absolute;
		top: 12rpx;
		right: 12rpx;
	}

	.plan-name {
		display: block;
		font-size: 28rpx;
		font-weight: 600;
		color: #22324B;
	}

	.plan-days {
		display: block;
		margin-top: 8rpx;
		font-size: 20rpx;
		color: #7B8798;
	}

	.price {
		display: block;
		margin-top: 12rpx;
		font-size: 36rpx;
		font-weight: 700;
		color: #386df5;
	}

	.price.dark {
		color: #4E5D73;
	}

	.line-price {
		display: block;
		margin-top: 6rpx;
		font-size: 22rpx;
		color: #A2ACBA;
		text-decoration: line-through;
	}

	.open-btn {
		margin-top: 32rpx;
		display: flex;
		align-items: center;
		justify-content: space-between;
		gap: 18rpx;
		padding: 0 28rpx;
		height: 100rpx;
		border-radius: 999rpx;
		background: linear-gradient(135deg, #386df5 0%, #5b8cff 100%);
		color: #fff;
		box-shadow: 0 18rpx 30rpx rgba(56, 109, 245, 0.18);
	}

	.open-btn-copy {
		min-width: 0;
	}

	.open-btn-title {
		display: block;
		font-size: 30rpx;
		font-weight: 700;
		line-height: 1.2;
	}

	.open-btn-sub {
		display: block;
		margin-top: 6rpx;
		font-size: 20rpx;
		color: rgba(255, 255, 255, 0.78);
	}

	.open-btn-icon {
		width: 52rpx;
		height: 52rpx;
		border-radius: 50%;
		background: rgba(255, 255, 255, 0.16);
		display: flex;
		align-items: center;
		justify-content: center;
		flex-shrink: 0;
	}

	/* #ifdef MP-WEIXIN */
	.rights-head,
	.rights-row {
		display: flex;
		align-items: center;
	}

	.rights-head-label,
	.rights-label {
		width: 44%;
	}

	.rights-head-free,
	.rights-free-cell {
		width: 24%;
		flex-shrink: 0;
	}

	.rights-head-vip,
	.rights-vip {
		width: 32%;
		flex-shrink: 0;
	}

	.plan-row {
		display: flex;
		flex-wrap: wrap;
		gap: 0;
		margin-left: -8rpx;
		margin-right: -8rpx;
	}

	.plan-card {
		width: calc(33.3333% - 16rpx);
		margin: 0 8rpx 16rpx;
	}
	/* #endif */
</style>
