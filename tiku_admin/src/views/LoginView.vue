<script setup>
import { ArrowRight, DataAnalysis, Lock, Picture, Setting, User, Wallet } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { nextTick, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
	BRAND_ADMIN_TITLE,
	BRAND_COMPANY,
	BRAND_LOGO_TEXT,
	BRAND_NAME
} from '@/constants/brand'
import { useAuthStore } from '@/stores/auth'
import { validateForm } from '@/utils/form'
import { getCaptchaUrl, getUUID } from '@/utils/tool'
import '@/styles/login.scss'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const formRef = ref()
const loading = ref(false)
const captchaUrl = ref('')
const DEFAULT_VIEWPORT = 'width=1600, minimum-scale=0.1, maximum-scale=10, user-scalable=yes, viewport-fit=cover'
const LOGIN_VIEWPORT = 'width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no, viewport-fit=cover'

const features = [
	{
		icon: Setting,
		title: '题库与章节',
		desc: '题目、试卷、章节树统一管理'
	},
	{
		icon: Wallet,
		title: '会员与推广',
		desc: '套餐权益、分销佣金、提现审核'
	},
	{
		icon: DataAnalysis,
		title: '学情与 AI',
		desc: '学习报告、OCR 与 AI 能力配置'
	},
	{
		icon: Lock,
		title: '安全运营',
		desc: '权限角色、操作日志、系统配置'
	}
]

const form = reactive({
	username: 'zww',
	password: '123456',
	captcha: '',
	uuid: ''
})

const rules = {
	username: [{
		required: true,
		message: '请输入账号',
		trigger: 'blur'
	}],
	password: [{
		required: true,
		message: '请输入密码',
		trigger: 'blur'
	}],
	captcha: [{
		required: true,
		message: '请输入验证码',
		trigger: 'blur'
	}]
}

function getRedirectTarget() {
	const redirect = Array.isArray(route.query.redirect) ? route.query.redirect[0] : route.query.redirect
	if (!redirect || typeof redirect !== 'string' || redirect.startsWith('/login')) {
		return '/dashboard'
	}
	return redirect
}

function refreshCaptcha() {
	form.uuid = getUUID()
	captchaUrl.value = getCaptchaUrl(form.uuid)
}

function setViewport(content) {
	if (typeof document === 'undefined') return
	const viewport = document.querySelector('meta[name="viewport"]')
	viewport?.setAttribute('content', content)
}

async function blurActiveField() {
	if (typeof document === 'undefined') return
	const activeElement = document.activeElement
	if (activeElement instanceof HTMLElement) {
		activeElement.blur()
	}
	await nextTick()
}

async function handleLogin() {
	if (loading.value) {
		return
	}
	const valid = await validateForm(formRef)
	if (!valid) {
		return
	}
	loading.value = true
	try {
		await authStore.login(form)
		await blurActiveField()
		ElMessage.success('登录成功')
		window.location.replace(router.resolve(getRedirectTarget()).href)
	} catch (error) {
		ElMessage.error(error.msg || error.message || '登录失败')
		refreshCaptcha()
		form.captcha = ''
	} finally {
		loading.value = false
	}
}

onMounted(() => {
	setViewport(LOGIN_VIEWPORT)
	refreshCaptcha()
})

onBeforeUnmount(() => {
	setViewport(DEFAULT_VIEWPORT)
})
</script>

<template>
	<div class="login-page">
		<div class="login-page__backdrop" aria-hidden="true"></div>
		<div class="login-page__grid" aria-hidden="true"></div>
		<div class="login-page__orb login-page__orb--a" aria-hidden="true"></div>
		<div class="login-page__orb login-page__orb--b" aria-hidden="true"></div>

		<section class="login-page__hero">
			<div class="login-page__brand">
				<div class="login-page__logo">{{ BRAND_LOGO_TEXT }}</div>
				<div class="login-page__brand-copy">
					<span class="login-page__brand-name">{{ BRAND_NAME }}</span>
					<span class="login-page__brand-tag">智慧学习 · 运营中枢</span>
				</div>
			</div>

			<h1 class="login-page__title">{{ BRAND_ADMIN_TITLE }}</h1>
			<p class="login-page__subtitle">
				统一管理题库内容、会员权益、推广分销与学情数据，让 C 端学习体验与后台运营效率同频提升。
			</p>

			<div class="login-page__features">
				<div v-for="item in features" :key="item.title" class="login-page__feature">
					<div class="login-page__feature-icon">
						<el-icon :size="18">
							<component :is="item.icon" />
						</el-icon>
					</div>
					<div>
						<strong>{{ item.title }}</strong>
						<span>{{ item.desc }}</span>
					</div>
				</div>
			</div>

			<div class="login-page__preview" aria-hidden="true">
				<div class="login-page__preview-head">
					<span class="login-page__preview-badge">运营看板预览</span>
					<span class="login-page__preview-score">今日活跃 128</span>
				</div>
				<div class="login-page__preview-bars">
					<div class="login-page__preview-bar login-page__preview-bar--main"></div>
					<div class="login-page__preview-bar login-page__preview-bar--sub"></div>
					<div class="login-page__preview-bar login-page__preview-bar--short"></div>
				</div>
			</div>
		</section>

		<section class="login-page__panel">
			<div class="login-page__panel-glow login-page__panel-glow--a" aria-hidden="true"></div>
			<div class="login-page__panel-glow login-page__panel-glow--b" aria-hidden="true"></div>
			<div class="login-page__panel-dots" aria-hidden="true"></div>

			<div class="login-page__card">
				<div class="login-page__card-accent" aria-hidden="true"></div>

				<div class="login-page__card-head">
					<div class="login-page__card-badge">
						<div class="login-page__card-badge-icon">
							<el-icon :size="22"><Lock /></el-icon>
						</div>
						<div>
							<span class="login-page__card-eyebrow">管理员登录</span>
							<h2 class="login-page__card-title">欢迎回来</h2>
						</div>
					</div>
					<p class="login-page__card-desc">请输入账号信息，安全进入 AI 题库运营控制台。</p>

					<div class="login-page__trust-row">
						<span class="login-page__trust-item">权限隔离</span>
						<span class="login-page__trust-item">操作审计</span>
						<span class="login-page__trust-item">安全加密</span>
					</div>
				</div>

				<el-form
					ref="formRef"
					class="login-page__form"
					:model="form"
					:rules="rules"
					label-position="top"
					@submit.prevent
				>
					<el-form-item prop="username">
						<div class="login-page__field-block">
							<label class="login-page__label" for="login-username">账号</label>
							<div class="login-page__input-shell">
								<span class="login-page__input-icon">
									<el-icon :size="18"><User /></el-icon>
								</span>
								<el-input
									id="login-username"
									v-model="form.username"
									placeholder="请输入管理员账号"
									clearable
								/>
							</div>
						</div>
					</el-form-item>

					<el-form-item prop="password">
						<div class="login-page__field-block">
							<label class="login-page__label" for="login-password">密码</label>
							<div class="login-page__input-shell">
								<span class="login-page__input-icon">
									<el-icon :size="18"><Lock /></el-icon>
								</span>
								<el-input
									id="login-password"
									v-model="form.password"
									type="password"
									show-password
									placeholder="请输入登录密码"
								/>
							</div>
						</div>
					</el-form-item>

					<el-form-item prop="captcha">
						<div class="login-page__captcha-head">
							<label class="login-page__label" for="login-captcha">验证码</label>
							<button type="button" class="login-page__captcha-refresh" @click="refreshCaptcha">
								换一张
							</button>
						</div>
						<div class="login-page__captcha-layout">
							<div class="login-page__input-shell login-page__input-shell--captcha">
								<span class="login-page__input-icon">
									<el-icon :size="18"><Picture /></el-icon>
								</span>
								<el-input
									id="login-captcha"
									v-model="form.captcha"
									placeholder="请输入图形验证码"
									clearable
									@keyup.enter="handleLogin"
								/>
							</div>
							<button
								type="button"
								class="login-page__captcha-box"
								title="点击刷新验证码"
								aria-label="刷新验证码"
								@click="refreshCaptcha"
							>
								<img
									v-if="captchaUrl"
									:src="captchaUrl"
									alt="图形验证码"
									class="login-page__captcha-image"
								/>
								<span v-else class="login-page__captcha-placeholder">加载中</span>
							</button>
						</div>
						<p class="login-page__captcha-tip">看不清验证码？点击图片或「换一张」刷新</p>
					</el-form-item>

					<el-button
						native-type="button"
						type="primary"
						:loading="loading"
						class="login-page__submit"
						@click="handleLogin"
					>
						进入系统
						<el-icon v-if="!loading" class="login-page__submit-icon"><ArrowRight /></el-icon>
					</el-button>
				</el-form>

				<div class="login-page__footer">
					{{ BRAND_COMPANY }}
				</div>
			</div>
		</section>
	</div>
</template>
