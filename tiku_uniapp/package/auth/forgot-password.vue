<template>
	<view class="login-page">
		<view class="page-bg-grid"></view>
		<view class="page-bg-radial page-bg-radial-a"></view>
		<view class="page-bg-radial page-bg-radial-b"></view>
		<view class="hero-wave hero-wave-a"></view>
		<view class="hero-wave hero-wave-b"></view>
		<view class="hero-section">
			<view class="hero-content">
				<view class="hero-brand-row">
					<view class="hero-brand-mark">
						<up-icon name="kefu-ermai" size="28" color="#FFFFFF"></up-icon>
					</view>
					<view class="hero-brand-copy">
						<text class="hero-brand-name">AI智能提分</text>
						<text class="hero-brand-sub">中学练习与学情辅助平台</text>
					</view>
				</view>
				<view class="hero-main-copy">
					<text class="hero-title">找回密码</text>
					<text class="hero-subtitle">通过短信验证重置登录密码并恢复账号访问</text>
				</view>
				<view class="learning-scene">
					<view class="scene-chip scene-chip-a">
						<up-icon name="checkmark-circle-fill" size="16" color="#22c55e"></up-icon>
						<text>短信验证</text>
					</view>
					<view class="question-preview">
						<view class="preview-top">
							<view class="preview-badge">
								<up-icon name="grid-fill" size="15" color="#2457d6"></up-icon>
								<text>密码重置</text>
							</view>
						</view>
						<text class="preview-question">手机号校验通过后可重新设置登录密码，并恢复账号访问。</text>
						<view class="preview-bars">
							<view class="preview-bar preview-bar-main"></view>
							<view class="preview-bar preview-bar-sub"></view>
							<view class="preview-bar preview-bar-sub short"></view>
						</view>
					</view>
				</view>
			</view>
		</view>

		<view class="auth-section">
			<view class="section-head">
				<text class="section-title" style="text-align: center;">重置密码</text>
				<text class="section-subtitle" style="text-align: center;">通过验证码校验后设置新的登录密码</text>
			</view>

			<view v-if="accountAlert" class="account-alert">
				<up-icon name="error-circle-fill" size="18" color="#ff4d4f"></up-icon>
				<text class="account-alert-text">{{ accountAlert }}</text>
			</view>

			<view class="field-group">
				<text class="field-label">手机号</text>
				<view class="field-pill" :class="{ 'field-error': phoneHint, 'field-focus': phoneFocus }">
					<up-icon name="phone-fill" size="20" color="#386df5"></up-icon>
					<input :value="phone" class="field-input" type="digit" maxlength="11" placeholder="请输入手机号"
						placeholder-class="placeholder" @input="onPhoneInput" @focus="phoneFocus = true"
						@blur="phoneFocus = false" />
				</view>
				<text v-if="phoneHint" class="field-hint">{{ phoneHint }}</text>
			</view>

			<view class="field-group">
				<view class="field-head">
					<text class="field-label">验证码</text>
				</view>
				<view class="code-row">
					<view class="code-input-wrap">
						<view class="field-pill code-pill" :class="{ 'field-error': displayCodeHint, 'field-focus': codeFocus }">
							<up-icon name="lock" size="20" color="#386df5"></up-icon>
							<input :value="code" class="field-input" type="digit" maxlength="6" placeholder="请输入验证码"
								placeholder-class="placeholder" @input="onCodeInput" @focus="codeFocus = true"
								@blur="codeFocus = false" />
						</view>
						<text v-if="displayCodeHint" class="field-hint">{{ displayCodeHint }}</text>
					</view>
					<view class="captcha-pill" :class="{ disabled: !canSendCode }" @tap="sendCode">
						<text>{{ codeText }}</text>
					</view>
				</view>
			</view>

			<view class="field-group">
				<text class="field-label">新密码</text>
				<view class="field-pill" :class="{ 'field-error': passwordHint, 'field-focus': passwordFocus }">
					<up-icon name="lock-fill" size="20" color="#386df5"></up-icon>
					<input :value="password" class="field-input" password maxlength="20" placeholder="请输入6-20位新密码"
						placeholder-class="placeholder" @input="onPasswordInput" @focus="passwordFocus = true"
						@blur="passwordFocus = false" />
				</view>
				<text v-if="passwordHint" class="field-hint">{{ passwordHint }}</text>
			</view>

			<view class="login-btn" :class="{ disabled: !canSubmit || submitting }" @tap="submitReset">
				<up-loading-icon v-if="submitting" mode="circle" color="#FFFFFF" size="20"></up-loading-icon>
				<text v-else>确认重置</text>
			</view>

			<view class="auth-switch-row">
				<text class="auth-switch-link" @tap="goToAccountLogin">返回账号登录</text>
			</view>
		</view>
	</view>
</template>

<script setup>
	import { computed, onBeforeUnmount, ref } from 'vue';
	import {
		resetPasswordByPhone,
		sendAuthVerifyCode
	} from '@/common/auth-api';
	import {
		applyLoginErrorFeedback,
		parseNetworkError
	} from '@/common/login-error';

	const PHONE_REGEX = /^1\d{10}$/;
	const phone = ref('');
	const code = ref('');
	const password = ref('');
	const countdown = ref(0);
	const phoneFocus = ref(false);
	const codeFocus = ref(false);
	const passwordFocus = ref(false);
	const submitting = ref(false);
	const sending = ref(false);
	const apiCodeHint = ref('');
	const accountAlert = ref('');
	let timer = null;

	const phoneValid = computed(() => PHONE_REGEX.test(phone.value));
	const phoneHint = computed(() => {
		if (!phone.value || phoneValid.value) return '';
		if (phone.value.length < 11) return '请输入11位手机号';
		return '请输入以1开头的11位手机号';
	});
	const codeValid = computed(() => code.value.length >= 4);
	const codeHint = computed(() => {
		if (!code.value || codeValid.value) return '';
		return '请输入至少4位验证码';
	});
	const displayCodeHint = computed(() => apiCodeHint.value || codeHint.value);
	const passwordValid = computed(() => password.value.length >= 6 && password.value.length <= 20);
	const passwordHint = computed(() => {
		if (!password.value || passwordValid.value) return '';
		return '请输入6-20位新密码';
	});
	const canSendCode = computed(() => phoneValid.value && countdown.value === 0 && !sending.value);
	const codeText = computed(() => countdown.value > 0 ? `${countdown.value}s` : '获取验证码');
	const canSubmit = computed(() => phoneValid.value && codeValid.value && passwordValid.value && !submitting.value);

	const clearApiErrorState = () => {
		apiCodeHint.value = '';
		accountAlert.value = '';
	};

	const showErrorToast = (title) => {
		uni.showToast({ title, icon: 'none' });
	};

	const onPhoneInput = (event) => {
		phone.value = String(event.detail?.value || '').replace(/\D/g, '').slice(0, 11);
		clearApiErrorState();
	};
	const onCodeInput = (event) => {
		code.value = String(event.detail?.value || '').replace(/\D/g, '').slice(0, 6);
		clearApiErrorState();
	};
	const onPasswordInput = (event) => {
		password.value = String(event.detail?.value || '').slice(0, 20);
		clearApiErrorState();
	};
	const startCountdown = () => {
		if (timer) {
			clearInterval(timer);
			timer = null;
		}
		countdown.value = 60;
		timer = setInterval(() => {
			countdown.value -= 1;
			if (countdown.value <= 0) {
				clearInterval(timer);
				timer = null;
			}
		}, 1000);
	};
	const sendCode = async () => {
		if (!canSendCode.value) {
			showErrorToast(countdown.value > 0 ? '请稍后再试' : (phone.value ? '请输入正确手机号' : '请输入手机号'));
			return;
		}
		sending.value = true;
		clearApiErrorState();
		try {
			await sendAuthVerifyCode(phone.value);
			startCountdown();
			showErrorToast('验证码已发送');
		} catch (e) {
			if (e?.loginError) {
				applyLoginErrorFeedback(e.loginError, {
					setCodeHint: (msg) => {
						apiCodeHint.value = msg;
					},
					setAccountAlert: (msg) => {
						accountAlert.value = msg;
					},
					showToast: showErrorToast
				});
			} else {
				applyLoginErrorFeedback(parseNetworkError(), {
					showToast: showErrorToast
				});
			}
		} finally {
			sending.value = false;
		}
	};
	const submitReset = async () => {
		if (submitting.value) return;
		if (!canSubmit.value) {
			if (!phoneValid.value) {
				showErrorToast(phone.value ? '请输入正确手机号' : '请输入手机号');
				return;
			}
			if (!codeValid.value) {
				showErrorToast('请输入至少4位验证码');
				return;
			}
			showErrorToast('请输入6-20位新密码');
			return;
		}
		submitting.value = true;
		clearApiErrorState();
		try {
			await resetPasswordByPhone({
				phone: phone.value,
				code: code.value,
				password: password.value
			});
			showErrorToast('密码重置成功，请重新登录');
			setTimeout(() => {
				uni.navigateTo({ url: '/package/auth/account-login' });
			}, 800);
		} catch (e) {
			if (e?.loginError) {
				applyLoginErrorFeedback(e.loginError, {
					setCodeHint: (msg) => {
						apiCodeHint.value = msg;
					},
					setAccountAlert: (msg) => {
						accountAlert.value = msg;
					},
					showToast: showErrorToast
				});
			} else {
				showErrorToast(e?.message || parseNetworkError().message);
			}
		} finally {
			submitting.value = false;
		}
	};
	const goToAccountLogin = () => {
		uni.navigateTo({ url: '/package/auth/account-login' });
	};
	onBeforeUnmount(() => {
		if (timer) clearInterval(timer);
	});
</script>

<style lang="scss" scoped>
	.login-page {
		min-height: 100vh;
		height: 100vh;
		background:
			radial-gradient(circle at 18% 14%, rgba(67, 214, 151, 0.22) 0, rgba(67, 214, 151, 0) 18%),
			radial-gradient(circle at 86% 22%, rgba(110, 168, 255, 0.28) 0, rgba(110, 168, 255, 0) 20%),
			linear-gradient(180deg, #0f265c 0%, #16377e 22%, #254f9f 36%, #dce8fb 60%, #eef4fd 78%, #f7f9fd 100%);
		position: relative;
		padding-bottom: calc(18rpx + env(safe-area-inset-bottom));
		overflow: hidden;
		display: flex;
		flex-direction: column;
	}
	.page-bg-grid { position: absolute; inset: 0; background-image: linear-gradient(rgba(255, 255, 255, 0.05) 1rpx, transparent 1rpx), linear-gradient(90deg, rgba(255, 255, 255, 0.05) 1rpx, transparent 1rpx); background-size: 32rpx 32rpx; pointer-events: none; }
	.hero-wave { position: absolute; left: -10%; width: 120%; border-radius: 50%; pointer-events: none; }
	.hero-wave-a { height: 260rpx; top: 340rpx; background: rgba(255, 255, 255, 0.05); transform: rotate(-6deg); }
	.hero-wave-b { height: 180rpx; top: 404rpx; background: rgba(255, 255, 255, 0.08); transform: rotate(4deg); }
	.page-bg-radial { position: absolute; border-radius: 50%; filter: blur(90rpx); pointer-events: none; }
	.page-bg-radial-a { width: 320rpx; height: 320rpx; top: 180rpx; right: -100rpx; background: rgba(34, 197, 94, 0.12); }
	.page-bg-radial-b { width: 280rpx; height: 280rpx; top: 420rpx; left: -90rpx; background: rgba(245, 158, 11, 0.1); }
	.hero-section { position: relative; padding-bottom: 18rpx; }
	.hero-content { padding: 8rpx 32rpx 0; position: relative; z-index: 1; }
	.hero-brand-row { display: flex; align-items: center; gap: 18rpx; }
	.hero-brand-mark { width: 72rpx; height: 72rpx; border-radius: 20rpx; background: rgba(255, 255, 255, 0.16); box-shadow: inset 0 0 0 2rpx rgba(255, 255, 255, 0.06); display: flex; align-items: center; justify-content: center; backdrop-filter: blur(12rpx); }
	.hero-brand-copy { min-width: 0; display: flex; flex-direction: column; gap: 6rpx; }
	.hero-brand-name { font-size: 30rpx; font-weight: 700; color: #ffffff; }
	.hero-brand-sub { font-size: 22rpx; color: rgba(255, 255, 255, 0.72); }
	.hero-main-copy { margin-top: 18rpx; }
	.hero-title { display: block; font-size: 44rpx; line-height: 1.08; font-weight: 800; color: #ffffff; }
	.hero-subtitle { display: block; margin-top: 10rpx; font-size: 22rpx; line-height: 1.45; color: rgba(255, 255, 255, 0.78); }
	.learning-scene { margin-top: 0; height: 88rpx; position: relative; opacity: 0.14; pointer-events: none; }
	.scene-chip, .question-preview { position: absolute; }
	.scene-chip { padding: 6rpx 10rpx; border-radius: 16rpx; background: rgba(255, 255, 255, 0.9); box-shadow: 0 8rpx 18rpx rgba(8, 18, 45, 0.1); display: flex; align-items: center; gap: 6rpx; font-size: 16rpx; font-weight: 600; color: #344054; }
	.scene-chip-a { top: 10rpx; left: 14rpx; }
	.question-preview { left: 142rpx; right: 96rpx; top: 10rpx; padding: 14rpx; border-radius: 20rpx; background: linear-gradient(180deg, rgba(255, 255, 255, 0.98) 0%, rgba(243, 247, 255, 0.98) 100%); box-shadow: 0 12rpx 24rpx rgba(7, 19, 47, 0.12); transform: rotate(-6deg) scale(0.74); transform-origin: top center; }
	.preview-top { display: flex; align-items: center; justify-content: space-between; gap: 16rpx; }
	.preview-badge { display: inline-flex; align-items: center; gap: 8rpx; padding: 8rpx 14rpx; border-radius: 999rpx; background: #eef4ff; font-size: 20rpx; font-weight: 700; color: #2457d6; }
	.preview-question { display: block; margin-top: 8rpx; font-size: 18rpx; line-height: 1.4; font-weight: 700; color: #101828; }
	.preview-bars { margin-top: 8rpx; display: flex; flex-direction: column; gap: 6rpx; }
	.preview-bar { height: 8rpx; border-radius: 999rpx; background: #dce7fb; }
	.preview-bar-main { width: 100%; background: linear-gradient(90deg, #2457d6 0%, #4f86ff 100%); }
	.preview-bar-sub { width: 78%; }
	.preview-bar-sub.short { width: 52%; }
	.auth-section { position: relative; z-index: 2; margin: 68rpx 24rpx 0; padding: 26rpx 24rpx 20rpx; background: #ffffff; border-radius: 28rpx; box-shadow: 0 18rpx 42rpx rgba(18, 31, 66, 0.1); }
	.section-head { display: flex; flex-direction: column; gap: 6rpx; margin-bottom: 18rpx; }
	.section-title { font-size: 32rpx; font-weight: 700; color: #101828; }
	.section-subtitle { font-size: 22rpx; line-height: 1.45; color: #667085; }
	.account-alert { margin-bottom: 16rpx; padding: 16rpx 18rpx; border-radius: 16rpx; background: #fff2f0; display: flex; align-items: flex-start; gap: 10rpx; }
	.account-alert-text { flex: 1; font-size: 22rpx; line-height: 1.5; color: #ff4d4f; }
	.field-group { margin-top: 16rpx; }
	.field-group:first-of-type { margin-top: 0; }
	.field-head { display: flex; align-items: center; justify-content: space-between; gap: 16rpx; margin-bottom: 8rpx; }
	.field-label { display: block; margin-bottom: 18rpx; font-size: 26rpx; font-weight: 600; color: #344054; }
	.field-pill { height: 90rpx; border-radius: 16rpx; background: #f7f8fc; padding: 0 16rpx; display: flex; align-items: center; gap: 10rpx; box-shadow: inset 0 0 0 2rpx #edf1f7; transition: all 0.2s ease; }
	.field-pill.field-focus { background: #ffffff; box-shadow: inset 0 0 0 2rpx rgba(56, 109, 245, 0.9), 0 8rpx 24rpx rgba(56, 109, 245, 0.08); }
	.field-pill.field-error { background: #fff2f0; box-shadow: inset 0 0 0 2rpx rgba(255, 77, 79, 0.18); }
	.field-input { flex: 1; height: 100%; font-size: 23rpx; color: #0f1b3d; caret-color: #386df5; font-weight: 500; }
	.placeholder { color: #b7c0d1; font-weight: 400; }
	.field-hint { display: block; margin-top: 6rpx; padding-left: 6rpx; font-size: 20rpx; color: #ff4d4f; }
	.code-row { display: flex; align-items: flex-start; gap: 10rpx; }
	.code-input-wrap { flex: 1; min-width: 0; }
	.code-pill { margin-top: 0; }
	.captcha-pill { width: 148rpx; height: 90rpx; border-radius: 16rpx; background: #eef4ff; color: #386df5; font-size: 20rpx; font-weight: 700; display: flex; align-items: center; justify-content: center; flex-shrink: 0; box-shadow: inset 0 0 0 2rpx rgba(56, 109, 245, 0.1); }
	.captcha-pill.disabled { background: #f3f4f6; color: #b7c0d1; box-shadow: inset 0 0 0 2rpx #ebedf2; }
	.captcha-pill:active { background: #e4edff; }
	.login-btn { margin-top: 72rpx; height: 90rpx; border-radius: 16rpx; background: linear-gradient(135deg, #2457d6 0%, #3c74f7 100%); color: #fff; font-size: 25rpx; font-weight: 700; display: flex; align-items: center; justify-content: center; letter-spacing: 1rpx; box-shadow: 0 14rpx 30rpx rgba(36, 87, 214, 0.22); }
	.login-btn:active { transform: translateY(1rpx); }
	.login-btn.disabled { background: #e5e7eb; box-shadow: none; color: #98a2b3; }
	.auth-switch-row { margin-top: 18rpx; display: flex; align-items: center; justify-content: center; gap: 28rpx; }
	.auth-switch-link { font-size: 22rpx; line-height: 1.5; color: #2457d6; font-weight: 600; }
	@media screen and (max-width: 375px) { .hero-title { font-size: 40rpx; } .hero-content { padding-left: 24rpx; padding-right: 24rpx; } .learning-scene { height: 70rpx; } .question-preview { left: 120rpx; right: 74rpx; } .auth-section { margin: 0 18rpx 0; padding: 22rpx 20rpx 18rpx; } .captcha-pill { width: 136rpx; } }
</style>
