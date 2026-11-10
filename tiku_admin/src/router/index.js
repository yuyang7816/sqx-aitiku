import {
	createRouter,
	createWebHistory
} from 'vue-router'
import {
	useAuthStore
} from '@/stores/auth'
import {
	protectedRoutes,
	whiteRoutes
} from '@/router/routes'
import { BRAND_ADMIN_SUFFIX } from '@/constants/brand'

function normalizeRedirect(target) {
	if (!target || typeof target !== 'string' || target.startsWith('/login')) {
		return '/dashboard'
	}
	return target
}

const routes = [
	...whiteRoutes,
	{
		path: '/',
		component: () => import('@/layouts/AdminLayout.vue'),
		redirect: '/dashboard',
		children: protectedRoutes,
	},
	{
		path: '/:pathMatch(.*)*',
		redirect: '/404',
	},
]

const router = createRouter({
	history: createWebHistory(import.meta.env.BASE_URL),
	routes,
})

router.beforeEach(async (to) => {
	const authStore = useAuthStore()

	if (to.name === 'login') {
		if (authStore.token && !authStore.isLoggedIn) {
			authStore.clearSession()
		}
		if (authStore.isLoggedIn) {
			try {
				await authStore.restore()
			} catch {
				authStore.clearSession()
				return true
			}
			return normalizeRedirect(to.query.redirect)
		}
		return true
	}

	if (!authStore.isLoggedIn) {
		if (authStore.token) {
			authStore.clearSession()
		}
		return {
			path: '/login',
			query: {
				redirect: normalizeRedirect(to.fullPath)
			}
		}
	}

	try {
		const restored = await authStore.restore()
		if (!restored) {
			return {
				path: '/login',
				query: {
					redirect: normalizeRedirect(to.fullPath)
				}
			}
		}
	} catch {
		authStore.clearSession()
		return {
			path: '/login',
			query: {
				redirect: normalizeRedirect(to.fullPath)
			}
		}
	}

	if (to.meta?.permission && !authStore.hasPermission(to.meta.permission)) {
		// 已在导航中授权的菜单路径允许进入，避免误跳运营看板
		if (authStore.hasMenuPathAccess(to.path)) {
			return true
		}
		return '/dashboard'
	}
})

router.afterEach((to) => {
	document.title = `${to.meta?.title || '后台管理'} | ${BRAND_ADMIN_SUFFIX}`
})

export default router
