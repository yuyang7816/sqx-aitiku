<script setup>
	import {
		computed,
		nextTick,
		onBeforeUnmount,
		onMounted,
		ref,
		watch
	} from 'vue'
	import {
		useRoute,
		useRouter
	} from 'vue-router'
	import {
		ArrowDown,
		DataBoard,
		Files,
		Fold,
		Setting,
		User,
		Expand
	} from '@element-plus/icons-vue'
	import IconSvg from '@/components/icon-svg.vue'
	import {
		confirmAction
	} from '@/utils/modal'
	import {
		useAppStore
	} from '@/stores/app'
	import {
		useAuthStore
	} from '@/stores/auth'
	import {
		useMenuBadgesStore
	} from '@/stores/menu-badges'
	import {
		BRAND_ADMIN_TITLE,
		BRAND_LOGO_TEXT
	} from '@/constants/brand'
	import AppUserDetailDialog from '@/components/business/AppUserDetailDialog.vue'
	import VipOrderDetailDialog from '@/components/business/VipOrderDetailDialog.vue'

	const svgIconModules = import.meta.glob('/src/icons/svg/icon-*.svg', { eager: true, import: 'default' })

	const iconMap = {
		DataBoard,
		User,
		Setting,
		Files
	}
	const route = useRoute()
	const router = useRouter()
	const appStore = useAppStore()
	const authStore = useAuthStore()
	const tabListRef = ref()
	const canScrollLeft = ref(false)
	const canScrollRight = ref(false)
	const tabItemRefs = new Map()
	const prefetchedRoutePaths = new Set()
	const menuBadgesStore = useMenuBadgesStore()
	let prefetchTimer = 0
	let badgeTimer = 0
	let badgePollTimer = 0

	const menuList = computed(() => authStore.menuList)

	const activeMenu = computed(() => route.meta?.activeMenu || route.path)
	const routeTitleMap = computed(() => {
		const map = new Map()
		router.getRoutes().forEach((item) => {
			if (item.meta?.title) {
				map.set(item.path, item.meta.title)
			}
		})
		return map
	})

	function getTitleByPath(path) {
		return routeTitleMap.value.get(path) || ''
	}

	const headerDisplay = computed(() => {
		const currentTitle = route.meta?.title || '工作台'
		if (!route.meta?.title) {
			return {
				title: '工作台',
				subtitle: ''
			}
		}

		if (route.meta?.hidden && route.meta?.activeMenu) {
			const parentTitle = getTitleByPath(route.meta.activeMenu)
			return {
				title: parentTitle || currentTitle,
				subtitle: currentTitle,
			}
		}

		const matchedWithTitles = route.matched.filter((item) => item.meta?.title && item.path !== '/')
		if (matchedWithTitles.length > 1) {
			return {
				title: matchedWithTitles[0].meta.title,
				subtitle: matchedWithTitles[matchedWithTitles.length - 1].meta.title,
			}
		}

		const segments = route.path.split('/').filter(Boolean)
		if (segments.length > 1) {
			const basePath = `/${segments[0]}`
			const baseTitle = getTitleByPath(basePath)
			if (baseTitle && baseTitle !== currentTitle) {
				return {
					title: baseTitle,
					subtitle: currentTitle,
				}
			}
		}

		return {
			title: route.meta?.headerTitle || currentTitle,
			subtitle: '',
		}
	})

	watch(
		() => route.fullPath,
		async () => {
			scheduleMenuBadgesRefresh()
			if (!route.meta?.title || route.meta.hidden) {
				return
			}
			appStore.addTab({
				path: route.fullPath,
				title: route.meta.title,
				affix: Boolean(route.meta.affix)
			})
			await nextTick()
			scrollActiveTabIntoView()
			updateTabScrollState()
		}, {
			immediate: true
		},
	)

	watch(
		() => appStore.tabs.length,
		async () => {
			await nextTick()
			updateTabScrollState()
		},
	)

	watch(
		() => menuList.value,
		() => {
			scheduleMenuRoutePrefetch()
		}, {
			deep: true,
			immediate: true,
		},
	)

	function getMenuBadge(path) {
		return menuBadgesStore.getCount(path)
	}

	function getSubMenuBadge(children = []) {
		return menuBadgesStore.getSubMenuCount(children)
	}

	function scheduleMenuBadgesRefresh() {
		window.clearTimeout(badgeTimer)
		badgeTimer = window.setTimeout(() => {
			void menuBadgesStore.refresh()
		}, 120)
	}

	function navigate(path) {
		if (path) {
			router.push(path)
		}
	}

	function normalizeIconName(name) {
		return String(name || '').trim().replace(/^icon-/, '')
	}

	function hasSvgIcon(name) {
		const iconName = normalizeIconName(name)
		if (!iconName) {
			return false
		}
		return Boolean(svgIconModules[`/src/icons/svg/icon-${iconName}.svg`])
	}

	function collectMenuPaths(items = []) {
		return items.flatMap((item) => {
			const current = item.path ? [item.path] : []
			return current.concat(collectMenuPaths(item.children || []))
		})
	}

	function getRouteLoaders(record) {
		const loaders = []
		if (typeof record?.component === 'function') {
			loaders.push(record.component)
		}
		if (record?.components && typeof record.components === 'object') {
			Object.values(record.components).forEach((loader) => {
				if (typeof loader === 'function') {
					loaders.push(loader)
				}
			})
		}
		return loaders
	}

	async function prefetchMenuRoutes() {
		const routeMap = new Map(router.getRoutes().map((item) => [item.path, item]))
		const menuPathList = collectMenuPaths(menuList.value)
		const hiddenPathList = router.getRoutes()
			.filter((item) => item.meta?.hidden && item.meta?.activeMenu && menuPathList.includes(item.meta.activeMenu))
			.map((item) => item.path)
		const pathList = [...new Set([...menuPathList, ...hiddenPathList])]

		for (const path of pathList) {
			if (!path || prefetchedRoutePaths.has(path) || path === route.path) {
				continue
			}
			const record = routeMap.get(path)
			if (!record) {
				continue
			}
			const loaders = getRouteLoaders(record)
			if (!loaders.length) {
				prefetchedRoutePaths.add(path)
				continue
			}

			prefetchedRoutePaths.add(path)
			const results = await Promise.allSettled(loaders.map((loader) => loader()))
			if (results.some((item) => item.status === 'rejected')) {
				prefetchedRoutePaths.delete(path)
			}
		}
	}

	function scheduleMenuRoutePrefetch() {
		window.clearTimeout(prefetchTimer)
		prefetchTimer = window.setTimeout(() => {
			if (typeof window.requestIdleCallback === 'function') {
				window.requestIdleCallback(() => {
					void prefetchMenuRoutes()
				})
				return
			}
			void prefetchMenuRoutes()
		}, 180)
	}

	async function handleLogout() {
		await confirmAction('确认退出当前账号吗？')
		await authStore.logout()
		appStore.clearTabs(false)
		router.replace('/login')
	}

	function closeTab(path) {
		const current = route.fullPath === path
		appStore.removeTab(path)
		if (current) {
			const fallback = appStore.tabs[appStore.tabs.length - 1]?.path || '/dashboard'
			router.push(fallback)
		}
	}

	function closeAllTabs() {
		const current = route.fullPath
		const shouldRedirect = appStore.tabs.some((item) => item.path === current && !item.affix)
		appStore.clearTabs(true)
		if (shouldRedirect) {
			const fallback = appStore.tabs[appStore.tabs.length - 1]?.path || '/dashboard'
			router.push(fallback)
		}
	}

	function closeOtherTabs() {
		appStore.closeOtherTabs(route.fullPath)
	}

	function closeRightTabs() {
		appStore.closeRightTabs(route.fullPath)
	}

	function handleTabAction(command) {
		if (command === 'all') {
			closeAllTabs()
			return
		}
		if (command === 'right') {
			closeRightTabs()
			return
		}
		if (command === 'other') {
			closeOtherTabs()
		}
	}

	function setTabItemRef(path, el) {
		if (el) {
			tabItemRefs.set(path, el)
			return
		}
		tabItemRefs.delete(path)
	}

	function updateTabScrollState() {
		const el = tabListRef.value
		if (!el) {
			canScrollLeft.value = false
			canScrollRight.value = false
			return
		}
		canScrollLeft.value = el.scrollLeft > 4
		canScrollRight.value = el.scrollLeft + el.clientWidth < el.scrollWidth - 4
	}

	function scrollTabsBy(offset) {
		const el = tabListRef.value
		if (!el) return
		el.scrollBy({
			left: offset,
			behavior: 'smooth'
		})
		window.setTimeout(updateTabScrollState, 220)
	}

	function scrollActiveTabIntoView() {
		const el = tabItemRefs.get(route.fullPath)
		el?.scrollIntoView({
			behavior: 'smooth',
			inline: 'nearest',
			block: 'nearest'
		})
	}

	function handleTabWheel(event) {
		const el = tabListRef.value
		if (!el) return
		if (Math.abs(event.deltaY) <= Math.abs(event.deltaX)) return
		event.preventDefault()
		el.scrollLeft += event.deltaY
		updateTabScrollState()
	}

	onMounted(() => {
		updateTabScrollState()
		scheduleMenuRoutePrefetch()
		scheduleMenuBadgesRefresh()
		badgePollTimer = window.setInterval(() => {
			void menuBadgesStore.refresh()
		}, 30000)
		window.addEventListener('resize', updateTabScrollState)
	})

	onBeforeUnmount(() => {
		window.clearTimeout(prefetchTimer)
		window.clearTimeout(badgeTimer)
		window.clearInterval(badgePollTimer)
		window.removeEventListener('resize', updateTabScrollState)
	})
</script>

<template>
	<div class="admin-shell">
		<el-container class="admin-frame">
			<el-aside :width="appStore.collapsed ? '78px' : '248px'" class="admin-aside">
				<div class="brand-box">
					<div class="brand-box__logo">{{ BRAND_LOGO_TEXT }}</div>
					<div v-show="!appStore.collapsed" class="brand-box__text">
						<strong>{{ BRAND_ADMIN_TITLE }}</strong>
					</div>
				</div>
				<el-menu :default-active="activeMenu" :collapse="appStore.collapsed" :collapse-transition="false"
					class="aside-menu" @select="navigate">
					<template v-for="item in menuList" :key="item.path">
						<el-sub-menu v-if="item.children?.length" :index="item.path || `menu-${item.id}`">
							<template #title>
								<el-icon>
									<IconSvg v-if="hasSvgIcon(item.icon)" :name="item.icon" class="site-sidebar__menu-icon" />
									<component :is="iconMap[item.icon] || Setting" v-else />
								</el-icon>
								<span class="menu-item-label">
									<span>{{ item.title }}</span>
									<el-badge
										v-if="getSubMenuBadge(item.children) > 0"
										class="menu-item-badge"
										:value="getSubMenuBadge(item.children)"
										:max="99"
									/>
								</span>
							</template>
							<el-menu-item v-for="child in item.children" :key="child.path || child.id"
								:index="child.path">
								<el-icon>
									<IconSvg v-if="hasSvgIcon(child.icon)" :name="child.icon" class="site-sidebar__menu-icon" />
									<component :is="iconMap[child.icon] || DataBoard" v-else />
								</el-icon>
								<span class="menu-item-label">
									<span>{{ child.title }}</span>
									<el-badge
										v-if="getMenuBadge(child.path) > 0"
										class="menu-item-badge"
										:value="getMenuBadge(child.path)"
										:max="99"
									/>
								</span>
							</el-menu-item>
						</el-sub-menu>
						<el-menu-item v-else :index="item.path || `menu-${item.id}`">
							<el-icon>
								<IconSvg v-if="hasSvgIcon(item.icon)" :name="item.icon" class="site-sidebar__menu-icon" />
								<component :is="iconMap[item.icon] || DataBoard" v-else />
							</el-icon>
							<span class="menu-item-label">
								<span>{{ item.title }}</span>
								<el-badge
									v-if="getMenuBadge(item.path) > 0"
									class="menu-item-badge"
									:value="getMenuBadge(item.path)"
									:max="99"
								/>
							</span>
						</el-menu-item>
					</template>
				</el-menu>
			</el-aside>
			<el-container>
				<el-header class="admin-header">
					<div class="admin-header__left">
						<el-button circle plain @click="appStore.toggleCollapsed()">
							<el-icon>
								<component :is="appStore.collapsed ? Expand : Fold" />
							</el-icon>
						</el-button>
						<div class="admin-header__boss">
							<div class="admin-header__title">{{ headerDisplay.title }}</div>
							<div v-if="headerDisplay.subtitle" class="admin-header__subtitle admin-header__subtitlex">/
							</div>
							<div v-if="headerDisplay.subtitle" class="admin-header__subtitle">
								{{ headerDisplay.subtitle }}
							</div>
						</div>
					</div>
					<el-space>
						<el-dropdown>
							<div class="admin-user">
								<el-avatar>{{ authStore.profile.name?.slice(0, 1) }}</el-avatar>
								<div>
									<div>{{ authStore.profile.name }}</div>
									<small>{{ authStore.profile.email }}</small>
								</div>
								<el-icon>
									<ArrowDown />
								</el-icon>
							</div>
							<template #dropdown>
								<el-dropdown-menu>
									<el-dropdown-item @click="router.push('/dashboard')">返回工作台</el-dropdown-item>
									<el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
								</el-dropdown-menu>
							</template>
						</el-dropdown>
					</el-space>
				</el-header>
				<el-main class="admin-main">
					<div class="tab-bar">
						<el-button text class="tab-bar__scroll" :disabled="!canScrollLeft"
							@click="scrollTabsBy(-240)">‹</el-button>
						<div ref="tabListRef" class="tab-bar__list" @scroll="updateTabScrollState"
							@wheel="handleTabWheel">
							<button v-for="item in appStore.tabs" :key="item.path" class="tab-bar__item"
								:ref="(el) => setTabItemRef(item.path, el)"
								:class="{ 'is-active': item.path === route.fullPath }" @click="navigate(item.path)">
								<span>{{ item.title }}</span>
								<span v-if="!item.affix" class="tab-bar__close"
									@click.stop="closeTab(item.path)">×</span>
							</button>
						</div>
						<el-button text class="tab-bar__scroll" :disabled="!canScrollRight"
							@click="scrollTabsBy(240)">›</el-button>
						<el-dropdown trigger="click" @command="handleTabAction">
							<el-button text class="tab-bar__action">
								标签操作
								<el-icon>
									<ArrowDown />
								</el-icon>
							</el-button>
							<template #dropdown>
								<el-dropdown-menu>
									<el-dropdown-item command="all">全部关闭</el-dropdown-item>
									<el-dropdown-item command="right">关闭右侧</el-dropdown-item>
									<el-dropdown-item command="other">关闭其他</el-dropdown-item>
								</el-dropdown-menu>
							</template>
						</el-dropdown>
					</div>
					<router-view />
					<AppUserDetailDialog />
					<VipOrderDetailDialog />
				</el-main>
			</el-container>
		</el-container>
	</div>
</template>

<style scoped>
	.admin-header__subtitlex {
		margin: 0 5px;
	}

	.admin-header__subtitle {
		font-size: 13px;
		color: var(--el-text-color-secondary);
	}

	.admin-header__boss {
		display: flex;
		align-items: center;
	}
</style>
