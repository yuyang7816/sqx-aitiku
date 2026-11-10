<template>
	<view class="login-page">
		<view class="page-bg-grid"></view>
		<view class="page-bg-radial page-bg-radial-a"></view>
		<view class="page-bg-radial page-bg-radial-b"></view>
		<view class="hero-wave hero-wave-a"></view>
		<view class="hero-wave hero-wave-b"></view>
		<auth-hero title="欢迎登录" subtitle="同步学习记录、错题本和会员权益"></auth-hero>

		<view class="auth-section" :class="{ 'auth-section-mp-weixin': isMpWeixinClient }">
			<view class="section-head">
				<text class="section-title" style="text-align: center;">{{ isMpWeixinClient ? '微信登录' : '验证码登录' }}</text>
			</view>

			<view v-if="accountAlert" class="account-alert">
				<up-icon name="error-circle-fill" size="18" color="#ff4d4f"></up-icon>
				<text class="account-alert-text">{{ accountAlert }}</text>
			</view>

			<template v-if="!isMpWeixinClient">
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
							<input :value="code" class="field-input" type="digit" maxlength="6" placeholder="验证码"
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

			<view class="login-btn" :class="{ disabled: !canLogin }" @tap="login">
				<up-loading-icon v-if="loggingIn" mode="circle" color="#FFFFFF" size="20"></up-loading-icon>
				<text v-else>立即登录</text>
			</view>

			<view class="auth-switch-row">
				<text class="auth-switch-link" @tap="goToAccountLogin">账号密码登录</text>
				<text class="auth-switch-link" @tap="goToRegister">注册账号</text>
			</view>
			</template>

			<view v-if="!isMpWeixinClient" class="agreement-links agreement-check-row" :class="{ 'agreement-check-row-mp': isMpWeixinClient }" @tap="agreed = !agreed">
				<view class="agree-dot" :class="{ active: agreed }">
					<up-icon v-if="agreed" name="checkmark" size="12" color="#FFFFFF"></up-icon>
				</view>
				<text>已阅读并同意</text>
				<text class="link" hover-class="link-hover" @tap.stop="openAgreement">《用户协议》</text>
				<text> 和 </text>
				<text class="link" hover-class="link-hover" @tap.stop="openPrivacy">《隐私政策》</text>
			</view>

			<view v-if="isMpWeixinClient && showWechatLogin && wechatLoginStep !== 'phone'" class="wechat-entry-card">
				<view class="wechat-entry-icon">
					<up-icon name="weixin-fill" size="42" color="#07C160"></up-icon>
				</view>
				<text class="wechat-entry-title">微信快捷登录</text>
				<text class="wechat-entry-sub">授权后同步学习记录、错题本和会员权益</text>
			</view>

			<view v-if="showWechatLogin && isMpWeixinClient && wechatLoginStep === 'phone'" class="phone-bind-card">
				<view class="phone-bind-icon">
					<up-icon name="phone-fill" size="28" color="#386DF5"></up-icon>
				</view>
				<text class="phone-bind-title">授权手机号</text>
				<text class="phone-bind-sub">微信官方接口获取手机号，用于跨端同步学习数据（同手机号共用账号）</text>
				<button
					class="phone-bind-btn"
					open-type="getPhoneNumber"
					:disabled="phoneBinding"
					@getphonenumber="onWechatGetPhoneNumber"
				>
					<up-loading-icon v-if="phoneBinding" mode="circle" color="#FFFFFF" size="20"></up-loading-icon>
					<text v-else>微信授权手机号</text>
				</button>
			</view>

			<view v-if="showWechatLogin && !(isMpWeixinClient && wechatLoginStep === 'phone')" class="wechat-btn" :class="{ disabled: wechatLoggingIn, 'wechat-btn-mp': isMpWeixinClient }" @tap="wechatLogin">
				<up-loading-icon v-if="wechatLoggingIn" mode="circle" color="#FFFFFF" size="20"></up-loading-icon>
				<template v-else>
					<up-icon name="weixin-fill" size="28" color="#FFFFFF"></up-icon>
					<text>{{ isMpWeixinClient ? '微信授权登录' : '微信一键登录' }}</text>
				</template>
			</view>

			<view v-if="isMpWeixinClient" class="agreement-links agreement-check-row agreement-check-row-mp" @tap="agreed = !agreed">
				<view class="agree-dot" :class="{ active: agreed }">
					<up-icon v-if="agreed" name="checkmark" size="12" color="#FFFFFF"></up-icon>
				</view>
				<text>已阅读并同意</text>
				<text class="link" hover-class="link-hover" @tap.stop="openAgreement">《用户协议》</text>
				<text> 和 </text>
				<text class="link" hover-class="link-hover" @tap.stop="openPrivacy">《隐私政策》</text>
			</view>

			<text class="footer-tip" :class="{ 'footer-tip-mp': isMpWeixinClient }">登录后自动恢复学习记录与历史数据</text>
		</view>
	</view>
</template>

<script setup>
	import AuthHero from '@/package/auth/auth-hero.vue';
	import {
		computed,
		onBeforeUnmount,
		onMounted,
		ref
	} from 'vue';
	import {
		redirectAfterLogin
	} from '@/common/app-data';
	import { completeAuthSession } from '@/common/auth-api';
	import {
		applyLoginErrorFeedback,
		createMockLoginApiResponse,
		getLoginErrorMockCode,
		parseLoginApiError,
		parseNetworkError
	} from '@/common/login-error';
	import { buildAuthInvitePayload } from '@/common/invite-bind';
	import {
		AGREEMENT_TIP,
		buildAuthLoginBody
	} from '@/common/login-rules';
	import {
		startWechatOAuth,
		shouldShowWechatLoginButton,
		resolveWechatLoginVisible,
		tryConsumeMpOAuthCodeFromUrl
	} from '@/common/wechat-oauth';
	import {
		bindWechatPhoneByCode,
		extractWechatPhoneCode
	} from '@/common/wechat-phone';
	import { fetchWechatPublicConfig } from '@/common/wechat-config';
	import { isMpWeixin } from '@/common/platform';
	import { sendAuthVerifyCode } from '@/common/auth-api';
	import {
		postJson,
		SILENT_REQUEST_OPTIONS
	} from '@/common/api';
	import {
		AGREEMENT_PAGE,
		PRIVACY_PAGE
	} from '@/common/routes';

	const PHONE_REGEX = /^1\d{10}$/;
	const CODE_MIN_LENGTH = 4;
	const isMpWeixinClient = isMpWeixin();

	const phone = ref('');
	const code = ref('');
	const agreed = ref(false);
	const showWechatLogin = ref(false);
	const countdown = ref(0);
	const sending = ref(false);
	const loggingIn = ref(false);
	const wechatLoggingIn = ref(false);
	const phoneBinding = ref(false);
	const wechatLoginStep = ref('auth');
	const apiCodeHint = ref('');
	const accountAlert = ref('');
	const phoneFocus = ref(false);
	const codeFocus = ref(false);
	let timer = null;

	const refreshWechatLoginVisibility = async () => {
		if (!shouldShowWechatLoginButton()) {
			showWechatLogin.value = false;
			return;
		}
		const config = await fetchWechatPublicConfig();
		showWechatLogin.value = resolveWechatLoginVisible(config);
	};

	onMounted(async () => {
		await refreshWechatLoginVisibility();
		try {
			await tryConsumeMpOAuthCodeFromUrl();
		} catch (e) {
			showErrorToast(e?.message || '微信登录失败，请稍后重试');
		}
	});

	const phoneValid = computed(() => PHONE_REGEX.test(phone.value));
	const phoneHint = computed(() => {
		if (!phone.value || phoneValid.value) return '';
		if (phone.value.length < 11) return '请输入11位手机号';
		return '请输入以1开头的11位手机号';
	});
	const codeValid = computed(() => code.value.length >= CODE_MIN_LENGTH);
	const codeHint = computed(() => {
		if (!code.value || codeValid.value) return '';
		return `请输入至少${CODE_MIN_LENGTH}位验证码`;
	});
	const displayCodeHint = computed(() => apiCodeHint.value || codeHint.value);
	const canSendCode = computed(() => phoneValid.value && countdown.value === 0 && !sending.value);
	const canLogin = computed(() => phoneValid.value && codeValid.value && agreed.value && !loggingIn.value);
	const codeText = computed(() => countdown.value > 0 ? `${countdown.value}s` : '获取验证码');

	const clearApiErrorState = () => {
		apiCodeHint.value = '';
		accountAlert.value = '';
	};

	const showErrorToast = (title) => {
		uni.showToast({
			title,
			icon: 'none'
		});
	};

	const handleLoginApiFailure = (res, scene) => {
		const errorInfo = parseLoginApiError(res, scene);
		applyLoginErrorFeedback(errorInfo, {
			setCodeHint: (msg) => {
				apiCodeHint.value = msg;
			},
			setAccountAlert: (msg) => {
				accountAlert.value = msg;
			},
			showToast: showErrorToast
		});
	};

	const onPhoneInput = (event) => {
		const raw = event.detail?.value ?? '';
		phone.value = String(raw).replace(/\D/g, '').slice(0, 11);
		clearApiErrorState();
	};

	const onCodeInput = (event) => {
		const raw = event.detail?.value ?? '';
		code.value = String(raw).replace(/\D/g, '').slice(0, 6);
		clearApiErrorState();
	};

	const getPhoneBlockTip = () => {
		if (!phone.value) return '请输入手机号';
		return '请输入正确手机号';
	};

	const getLoginBlockTip = () => {
		if (!phoneValid.value) return getPhoneBlockTip();
		if (!codeValid.value) return `请输入至少${CODE_MIN_LENGTH}位验证码`;
		if (!agreed.value) return AGREEMENT_TIP;
		return '请完善登录信息';
	};

	const openArticle = (url) => {
		uni.navigateTo({
			url
		});
	};

	const openAgreement = () => openArticle(AGREEMENT_PAGE);
	const openPrivacy = () => openArticle(PRIVACY_PAGE);
	const goToAccountLogin = () => openArticle('/package/auth/account-login');
	const goToRegister = () => openArticle('/package/auth/register');

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
			uni.showToast({
				title: countdown.value > 0 ? '请稍后再试' : getPhoneBlockTip(),
				icon: 'none'
			});
			return;
		}
		sending.value = true;
		clearApiErrorState();
		try {
			const mockCode = getLoginErrorMockCode('verify-code');
			if (mockCode) {
				handleLoginApiFailure(createMockLoginApiResponse(mockCode), 'verify-code');
				return;
			}
			await sendAuthVerifyCode(phone.value);
			uni.showToast({
				title: '验证码已发送',
				icon: 'none'
			});
			startCountdown();
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

	const login = async () => {
		if (loggingIn.value) return;
		if (!phoneValid.value || !codeValid.value || !agreed.value) {
			uni.showToast({
				title: getLoginBlockTip(),
				icon: 'none'
			});
			return;
		}
		loggingIn.value = true;
		clearApiErrorState();
		try {
			const mockCode = getLoginErrorMockCode('sms-login');
			const res = mockCode ?
				createMockLoginApiResponse(mockCode) :
				await postJson('/app/auth/sms', buildAuthLoginBody({
					phone: phone.value,
					code: code.value,
					...buildAuthInvitePayload()
				}, agreed.value), SILENT_REQUEST_OPTIONS);
			if (res && res.code === 0 && res.data?.token) {
				await completeAuthSession({
					token: res.data.token,
					phone: phone.value,
					loginType: 'sms',
					profilePatch: res.data.profile
				});
				redirectAfterLogin();
				return;
			}
			handleLoginApiFailure(res, 'sms-login');
		} catch (e) {
			applyLoginErrorFeedback(parseNetworkError(), {
				showToast: showErrorToast
			});
		} finally {
			loggingIn.value = false;
		}
	};

	const wechatLogin = async () => {
		if (wechatLoggingIn.value) return;
		if (!agreed.value) {
			uni.showToast({
				title: AGREEMENT_TIP,
				icon: 'none'
			});
			return;
		}
		wechatLoggingIn.value = true;
		clearApiErrorState();
		try {
			const result = await startWechatOAuth({ agreedProtocol: agreed.value });
			if (result?.needBindPhone) {
				wechatLoginStep.value = 'phone';
				return;
			}
		} catch (e) {
			if (e?.loginError) {
				applyLoginErrorFeedback(e.loginError, {
					setAccountAlert: (msg) => {
						accountAlert.value = msg;
					},
					showToast: showErrorToast
				});
				return;
			}
			showErrorToast(e?.message || '微信登录失败，请稍后重试');
		} finally {
			wechatLoggingIn.value = false;
		}
	};

	const onWechatGetPhoneNumber = async (event) => {
		if (phoneBinding.value) {
			return;
		}
		const phoneCode = extractWechatPhoneCode(event);
		if (!phoneCode) {
			showErrorToast('需要授权手机号才能继续');
			return;
		}
		phoneBinding.value = true;
		clearApiErrorState();
		try {
			await bindWechatPhoneByCode(phoneCode);
			redirectAfterLogin();
		} catch (e) {
			showErrorToast(e?.msg || e?.message || '绑定手机号失败，请重试');
		} finally {
			phoneBinding.value = false;
		}
	};

	onBeforeUnmount(() => {
		if (timer) clearInterval(timer);
	});
</script>

<style lang="scss" scoped>
	@import './auth-page.scss';

	.wechat-btn {
		margin-top: 16rpx;
		height: 68rpx;
		border-radius: 16rpx;
		background: linear-gradient(135deg, #07c160 0%, #06ad56 100%);
		color: #fff;
		font-size: 25rpx;
		font-weight: 700;
		display: flex;
		align-items: center;
		justify-content: center;
		gap: 16rpx;
		letter-spacing: 1rpx;
		box-shadow: 0 14rpx 30rpx rgba(7, 193, 96, 0.2);
	}

	.wechat-btn:active {
		transform: translateY(1rpx);
	}

	.wechat-btn.disabled {
		opacity: 0.6;
	}

	.auth-section-mp-weixin {
		margin-top: 30rpx;
		padding-top: 64rpx;
		padding-bottom: 60rpx;
	}

	.wechat-entry-card {
		margin-top: 42rpx;
		display: flex;
		flex-direction: column;
		align-items: center;
		text-align: center;
	}

	.wechat-entry-icon {
		width: 116rpx;
		height: 116rpx;
		border-radius: 32rpx;
		background: linear-gradient(180deg, #f3fff8 0%, #e5f8ee 100%);
		box-shadow: inset 0 0 0 2rpx rgba(7, 193, 96, 0.1);
		display: flex;
		align-items: center;
		justify-content: center;
	}

	.wechat-entry-title {
		margin-top: 36rpx;
		font-size: 28rpx;
		font-weight: 700;
		color: #101828;
	}

	.wechat-entry-sub {
		margin-top: 22rpx;
		font-size: 22rpx;
		line-height: 1.6;
		color: #667085;
	}

	.wechat-btn-mp {
		margin-top: 56rpx;
		height: 88rpx;
		border-radius: 18rpx;
		font-size: 28rpx;
		font-weight: 700;
		box-shadow: 0 16rpx 32rpx rgba(7, 193, 96, 0.18);
	}

	.agreement-check-row-mp {
		margin: 30rpx 0 0;
		font-size: 23rpx;
		line-height: 1.7;
		color: #8a94a6;
	}

	.footer-tip-mp {
		margin-top: 30rpx;
		font-size: 21rpx;
		color: #a0a8b8;
	}

	.phone-bind-card {
		margin-top: 40rpx;
		padding: 36rpx 28rpx;
		border-radius: 24rpx;
		background: #ffffff;
		box-shadow: 0 16rpx 36rpx rgba(36, 48, 65, 0.06);
		display: flex;
		flex-direction: column;
		align-items: center;
		text-align: center;
	}

	.phone-bind-icon {
		width: 88rpx;
		height: 88rpx;
		border-radius: 24rpx;
		background: #eef4ff;
		display: flex;
		align-items: center;
		justify-content: center;
	}

	.phone-bind-title {
		margin-top: 24rpx;
		font-size: 30rpx;
		font-weight: 700;
		color: #182230;
	}

	.phone-bind-sub {
		margin-top: 16rpx;
		font-size: 22rpx;
		line-height: 1.6;
		color: #667085;
	}

	.phone-bind-btn {
		margin-top: 32rpx;
		width: 100%;
		height: 88rpx;
		border-radius: 18rpx;
		background: linear-gradient(135deg, #386df5 0%, #4f46e5 100%);
		color: #ffffff;
		font-size: 28rpx;
		font-weight: 700;
		display: flex;
		align-items: center;
		justify-content: center;
		border: none;
		padding: 0;
		line-height: 1;
	}

	.phone-bind-btn::after {
		border: none;
	}
</style>
