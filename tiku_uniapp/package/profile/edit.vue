<template>
	<view class="app-page edit-page">
		<scroll-view scroll-y class="page-scroll">
			<view class="screen edit-screen">
				<view class="profile-hero">
					<view class="hero-topline">
						<view class="hero-chip">
							<up-icon name="account" size="12" color="#386df5"></up-icon>
							<text>个人资料</text>
						</view>
						<text class="hero-sync">修改后同步</text>
					</view>
					<view class="hero-main-row">
						<view class="avatar-block" @tap="chooseAvatar">
							<profile-avatar :profile="previewProfile" size="lg" shape="round"></profile-avatar>
							<view class="avatar-camera">
								<up-icon name="camera-fill" size="14" color="#ffffff"></up-icon>
							</view>
						</view>
						<view class="hero-copy">
							<view class="hero-name-row" @tap="startEditNickname">
								<template v-if="editingNickname">
									<input
										v-model="nickname"
										class="hero-name-input"
										type="nickname"
										maxlength="20"
										placeholder="请输入昵称"
										placeholder-class="placeholder"
										:focus="editingNickname"
										confirm-type="done"
										@blur="finishEditNickname"
										@confirm="finishEditNickname"
									/>
								</template>
								<template v-else>
									<text class="hero-name">{{ nickname || '未设置昵称' }}</text>
									<text class="hero-edit-tag">编辑</text>
								</template>
							</view>
							<text class="hero-phone">{{ displayPhone }}</text>
						</view>
					</view>
				</view>

				<view class="section-card">
					<view class="section-head section-head--plan">
						<view>
							<text class="section-eyebrow">学习计划</text>
							<text class="section-title">计划概览</text>
						</view>
						<text class="section-badge">{{ planSummary.subjects.length }} 科</text>
					</view>
					<view class="plan-summary-board">
						<view class="summary-cell summary-cell--emphasis">
							<text class="cell-value">{{ planSummary.stage }}</text>
						</view>
						<view class="summary-cell">
							<text class="cell-value">{{ planSummary.grade }}</text>
						</view>
						<view class="summary-cell">
							<text class="cell-value">{{ planSummary.version }}</text>
						</view>
					</view>
					<view class="subjects-block">
						<view class="subjects-head">
							<text class="cell-label">学习科目</text>
							<text class="subjects-caption">推荐与筛选</text>
						</view>
						<view v-if="planSummary.subjects.length" class="subject-tags">
							<text
								v-for="item in planSummary.subjects"
								:key="item"
								class="subject-tag"
							>{{ item }}</text>
						</view>
						<text v-else class="plan-empty">暂未选择学科</text>
					</view>
					<view class="plan-edit-row" @tap="openEditPlan">
						<view class="plan-edit-left">
							<view class="plan-edit-icon">
								<up-icon name="map" size="18" color="#386df5"></up-icon>
							</view>
							<view class="plan-edit-copy">
								<text class="plan-edit-title">修改学习计划</text>
								<text class="plan-edit-sub">调整学段、年级、科目和教材</text>
							</view>
						</view>
						<up-icon name="arrow-right" size="14" color="#C0C6D4"></up-icon>
					</view>
				</view>
			</view>
		</scroll-view>

		<view class="edit-footer">
			<view class="solid-button" :class="{ disabled: submitting }" @tap="handleSave">
				<text>{{ submitting ? '保存中...' : '保存资料' }}</text>
			</view>
		</view>
	</view>
</template>

<script setup>
	import ProfileAvatar from '@/components/profile-avatar/profile-avatar.vue';
	import { computed, ref } from 'vue';
	import { onShow } from '@dcloudio/uni-app';
	import {
		getAuthState,
		getUserProfile,
		gradeToShort,
		resolveProfileAvatarUrl,
		SETUP_PLAN_EDIT_PAGE
	} from '@/common/app-data';
	import { uploadLocalImage } from '@/common/file-upload';
	import { fetchAndMergeUserInfo, updateUserAvatarNickname } from '@/common/user-info';

	const nickname = ref('');
	const avatarUrl = ref('');
	const editingNickname = ref(false);
	const submitting = ref(false);
	const profileSnapshot = ref(getUserProfile());
	const authSnapshot = ref(getAuthState());

	const previewProfile = computed(() => ({
		name: nickname.value,
		avatarText: nickname.value ? nickname.value.charAt(0) : '学',
		avatar: resolveProfileAvatarUrl(avatarUrl.value)
	}));

	const normalizeVersion = (version = '') => String(version).replace(/（默认）/g, '').trim();

	const resolveStageLabel = (profile = {}) => {
		const gradeValue = String(profile.grade || '').trim();
		const gradeShort = String(profile.gradeShort || '').trim();
		const source = gradeValue || gradeShort;
		if (source.startsWith('小学')) return '小学';
		if (source.startsWith('初中') || source.startsWith('初')) return '初中';
		if (source.startsWith('高中') || source.startsWith('高')) return '高中';
		return '未设置';
	};

	const planSummary = computed(() => {
		const profile = profileSnapshot.value || {};
		const stage = resolveStageLabel(profile);
		const grade = profile.gradeShort || gradeToShort(profile.grade) || profile.grade || '未设置';
		const version = profile.version ? normalizeVersion(profile.version) : '未设置';
		const subjects = Array.isArray(profile.subjects)
			? profile.subjects.filter(Boolean)
			: [];
		return { stage, grade, version, subjects };
	});

	const maskPhone = (phone = '') => {
		const value = String(phone).trim();
		if (!/^1\d{10}$/.test(value)) {
			return value || '未绑定';
		}
		return `${value.slice(0, 3)}****${value.slice(-4)}`;
	};

	const displayPhone = computed(() => maskPhone(authSnapshot.value.phone));
	const refreshForm = () => {
		profileSnapshot.value = getUserProfile();
		authSnapshot.value = getAuthState();
		const profile = profileSnapshot.value;
		nickname.value = profile.name || '';
		avatarUrl.value = profile.avatar || '';
	};

	onShow(async () => {
		await fetchAndMergeUserInfo();
		refreshForm();
	});

	const openEditPlan = () => {
		uni.navigateTo({ url: SETUP_PLAN_EDIT_PAGE });
	};

	const startEditNickname = () => {
		if (editingNickname.value) {
			return;
		}
		editingNickname.value = true;
	};

	const finishEditNickname = () => {
		editingNickname.value = false;
	};

	const chooseAvatar = () => {
		uni.chooseImage({
			count: 1,
			sizeType: ['compressed'],
			sourceType: ['album', 'camera'],
			success: async (res) => {
				const path = res.tempFilePaths && res.tempFilePaths[0];
				if (!path) {
					return;
				}
				try {
					uni.showLoading({ title: '上传中', mask: true });
					const url = await uploadLocalImage(path);
					avatarUrl.value = url;
				} catch (e) {
					uni.showToast({ title: e.msg || e.message || '头像上传失败', icon: 'none' });
				} finally {
					uni.hideLoading();
				}
			}
		});
	};

	const handleSave = async () => {
		if (submitting.value) {
			return;
		}
		submitting.value = true;
		try {
			const result = await updateUserAvatarNickname({
				nickname: nickname.value,
				avatar: avatarUrl.value
			});
			if (!result.success) {
				uni.showToast({ title: result.msg || '保存失败', icon: 'none' });
				return;
			}
			uni.showToast({ title: '资料已更新', icon: 'success' });
			setTimeout(() => {
				uni.navigateBack();
			}, 400);
		} finally {
			submitting.value = false;
		}
	};
</script>

<style lang="scss" scoped>
	.edit-page {
		background: linear-gradient(180deg, #f2f6ff 0, #f7f9fc 260rpx, #f5f7fb 100%);
	}

	.edit-screen {
		padding-top: 12rpx;
		padding-bottom: calc(136rpx + env(safe-area-inset-bottom));
	}

	.profile-hero,
	.section-card {
		margin-top: 16rpx;
		border-radius: 30rpx;
		background: rgba(255, 255, 255, 0.94);
		border: 2rpx solid rgba(222, 231, 246, 0.9);
		box-shadow: 0 18rpx 40rpx rgba(37, 58, 96, 0.06);
	}

	.profile-hero {
		padding: 24rpx;
	}

	.hero-topline {
		display: flex;
		align-items: center;
		justify-content: space-between;
		gap: 16rpx;
	}

	.hero-chip {
		display: inline-flex;
		align-items: center;
		gap: 6rpx;
		padding: 8rpx 16rpx;
		border-radius: 999rpx;
		background: #edf3ff;
		font-size: 20rpx;
		font-weight: 600;
		color: #386df5;
	}

	.hero-sync {
		font-size: 20rpx;
		color: #8b98aa;
		text-align: right;
	}

	.hero-main-row {
		margin-top: 20rpx;
		display: flex;
		align-items: center;
		gap: 24rpx;
	}

	.avatar-block {
		position: relative;
		padding: 6rpx;
		border-radius: 32rpx;
		background: linear-gradient(180deg, #eff4ff 0%, #ffffff 100%);
		box-shadow: inset 0 0 0 2rpx rgba(56, 109, 245, 0.08);
	}

	.avatar-block :deep(.profile-avatar.size-lg) {
		width: 116rpx;
		height: 116rpx;
		font-size: 46rpx;
	}

	.avatar-camera {
		position: absolute;
		right: 4rpx;
		bottom: 4rpx;
		width: 46rpx;
		height: 46rpx;
		border-radius: 50%;
		background: linear-gradient(135deg, #386df5 0%, #5e88ff 100%);
		border: 4rpx solid #ffffff;
		display: flex;
		align-items: center;
		justify-content: center;
		box-shadow: 0 10rpx 24rpx rgba(56, 109, 245, 0.28);
	}

	.hero-copy {
		min-width: 0;
		flex: 1;
	}

	.hero-name-row {
		min-height: 52rpx;
		display: inline-flex;
		align-items: center;
		gap: 12rpx;
		max-width: 100%;
	}

	.hero-name {
		display: block;
		font-size: 38rpx;
		line-height: 1.2;
		font-weight: 700;
		color: #172033;
	}

	.hero-name-input {
		width: 100%;
		max-width: 320rpx;
		height: 52rpx;
		padding: 0 18rpx;
		border-radius: 18rpx;
		background: #ffffff;
		box-shadow: inset 0 0 0 2rpx #dce5f4;
		font-size: 30rpx;
		font-weight: 700;
		color: #172033;
	}

	.hero-edit-tag {
		padding: 6rpx 14rpx;
		border-radius: 999rpx;
		background: #edf3ff;
		font-size: 20rpx;
		font-weight: 600;
		color: #386df5;
		flex-shrink: 0;
	}

	.hero-phone {
		display: block;
		margin-top: 12rpx;
		font-size: 22rpx;
		line-height: 1.5;
		color: #8b98aa;
	}

	.section-card {
		padding: 26rpx 24rpx;
	}

	.section-head {
		display: flex;
		align-items: flex-start;
		justify-content: space-between;
		gap: 18rpx;
	}

	.section-eyebrow {
		display: block;
		font-size: 20rpx;
		font-weight: 600;
		color: #6f84ab;
	}

	.section-title {
		display: block;
		margin-top: 8rpx;
		font-size: 32rpx;
		line-height: 1.2;
		font-weight: 700;
		color: #1d2939;
	}

	.section-badge {
		flex-shrink: 0;
	}

	.section-badge {
		padding: 10rpx 16rpx;
		border-radius: 999rpx;
		background: #eef4ff;
		font-size: 20rpx;
		font-weight: 600;
		color: #386df5;
	}

	.subjects-head {
		display: flex;
		align-items: center;
		justify-content: space-between;
		gap: 16rpx;
	}

	.cell-label {
		display: block;
		font-size: 22rpx;
		font-weight: 600;
		color: #4c5b70;
	}

	.subjects-caption,
	.plan-empty,
	.plan-edit-sub {
		font-size: 20rpx;
		line-height: 1.5;
		color: #98a2b3;
	}

	.plan-summary-board {
		margin-top: 20rpx;
		padding: 8rpx 0;
		border-radius: 22rpx;
		background: #f8fafe;
		box-shadow: inset 0 0 0 2rpx #edf1f7;
		display: grid;
		grid-template-columns: repeat(3, minmax(0, 1fr));
		gap: 0;
	}

	.summary-cell {
		position: relative;
		padding: 18rpx 12rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		min-width: 0;
	}

	.summary-cell + .summary-cell::before {
		content: '';
		position: absolute;
		left: 0;
		top: 18rpx;
		bottom: 18rpx;
		width: 2rpx;
		background: #e6ebf3;
	}

	.cell-value {
		display: block;
		font-size: 28rpx;
		line-height: 1.3;
		font-weight: 600;
		color: #22324a;
		text-align: center;
		white-space: nowrap;
		overflow: hidden;
		text-overflow: ellipsis;
	}

	.subjects-block {
		margin-top: 20rpx;
		padding-top: 20rpx;
		border-top: 2rpx solid #edf2f7;
	}

	.subject-tags {
		margin-top: 14rpx;
		display: flex;
		flex-wrap: wrap;
		gap: 10rpx;
	}

	.subject-tag {
		padding: 8rpx 16rpx;
		border-radius: 999rpx;
		background: #f3f6fc;
		font-size: 22rpx;
		font-weight: 600;
		color: #4f6585;
	}

	.plan-edit-row {
		margin-top: 22rpx;
		padding: 20rpx;
		border-radius: 24rpx;
		background: linear-gradient(135deg, #f7faff 0%, #eef4ff 100%);
		box-shadow: inset 0 0 0 2rpx rgba(56, 109, 245, 0.08);
		display: flex;
		align-items: center;
		justify-content: space-between;
		gap: 20rpx;
	}

	.plan-edit-row:active {
		opacity: 0.72;
	}

	.plan-edit-left {
		display: flex;
		align-items: center;
		gap: 18rpx;
		min-width: 0;
		flex: 1;
	}

	.plan-edit-icon {
		width: 56rpx;
		height: 56rpx;
		border-radius: 18rpx;
		background: #ffffff;
		display: flex;
		align-items: center;
		justify-content: center;
		flex-shrink: 0;
		box-shadow: 0 10rpx 24rpx rgba(56, 109, 245, 0.1);
	}

	.plan-edit-title {
		display: block;
		font-size: 28rpx;
		font-weight: 700;
		color: #22324a;
	}

	.plan-edit-sub {
		display: block;
		margin-top: 6rpx;
	}

	.edit-footer {
		position: fixed;
		left: 0;
		right: 0;
		bottom: 0;
		padding: 12rpx 28rpx calc(12rpx + env(safe-area-inset-bottom));
		background: rgba(255, 255, 255, 0.96);
		box-shadow: 0 -10rpx 28rpx rgba(36, 48, 65, 0.06);
		backdrop-filter: blur(12px);
	}

	.edit-footer .solid-button {
		font-weight: 700;
	}

	.solid-button.disabled {
		opacity: 0.5;
		box-shadow: none;
	}

	@media screen and (max-width: 375px) {
		.hero-topline,
		.section-head,
		.subjects-head {
			align-items: flex-start;
			flex-direction: column;
		}

		.hero-main-row {
			align-items: flex-start;
			gap: 18rpx;
		}

		.avatar-block :deep(.profile-avatar.size-lg) {
			width: 104rpx;
			height: 104rpx;
			font-size: 42rpx;
		}

		.hero-name {
			font-size: 34rpx;
		}

		.plan-summary-board {
			grid-template-columns: 1fr;
		}
	}

	/* #ifdef MP-WEIXIN */
	.plan-summary-board {
		display: flex;
		flex-wrap: wrap;
		background: #f8fafe;
	}

	.summary-cell {
		width: 33.3333%;
	}

	.edit-footer {
		background: rgba(255, 255, 255, 1);
		backdrop-filter: none;
	}

	.edit-footer .solid-button {
		background: #386df5;
		color: #ffffff;
	}

	.edit-footer .solid-button text {
		display: block;
		color: #ffffff;
		line-height: 1;
		transform: translateY(-2rpx);
	}
	/* #endif */
</style>
