/**
 * 首页 Banner 轮播：GET /app/banner，无数据时由首页 fallback 至 Hero 卡片
 */
import { get } from '@/common/api';

/** 展示位置：1=首页 Banner 轮播（与 banner.classify 一致） */
export const BANNER_POSITION_HOME = 1;

/** 状态：1=启用 */
export const BANNER_STATE_ENABLED = 1;

/** 将服务端 AppBannerItemVO 规范为本地结构 */
export const normalizeBannerItem = (item = {}) => ({
	id: item.id,
	imageUrl: item.imageUrl || '',
	link: item.link || '',
	sort: item.sort ?? 0
});

/**
 * 转为 up-swiper 列表（keyName=url 为图片地址，link 供点击跳转）
 * @param {object[]} list
 */
export const mapBannerListForSwiper = (list = []) =>
	(Array.isArray(list) ? list : [])
		.filter((item) => item.imageUrl)
		.map((item) => ({
			id: item.id,
			url: item.imageUrl,
			link: item.link || ''
		}));

/**
 * 拉取首页启用 Banner；失败或空列表返回 { list: [], fromApi: false }
 * @returns {Promise<{ list: object[], fromApi: boolean }>}
 */
export const fetchHomeBanners = async () => {
	try {
		const res = await get('/app/banner', {
			position: BANNER_POSITION_HOME,
			state: BANNER_STATE_ENABLED
		});
		if (res && res.code === 0 && Array.isArray(res.data)) {
			const list = res.data.map(normalizeBannerItem).filter((item) => item.imageUrl);
			return {
				list: mapBannerListForSwiper(list),
				fromApi: true
			};
		}
	} catch (e) {
		// 网络异常时静默回退 Hero
	}
	return {
		list: [],
		fromApi: false
	};
};

/**
 * Banner 点击跳转：内部 uniapp 路径或 https 外链
 * @param {string} link
 */
export const openBannerLink = (link = '') => {
	const trimmed = String(link || '').trim();
	if (!trimmed) {
		return;
	}

	if (/^https?:\/\//i.test(trimmed)) {
		// #ifdef APP-PLUS
		plus.runtime.openURL(trimmed);
		// #endif
		// #ifdef H5
		window.open(trimmed);
		// #endif
		// #ifdef MP
		uni.setClipboardData({
			data: trimmed,
			success: () => {
				uni.showToast({
					title: '链接已复制，请在浏览器打开',
					icon: 'none'
				});
			}
		});
		// #endif
		return;
	}

	let path = trimmed;
	if (!path.startsWith('/')) {
		path = `/${path}`;
	}
	if (path.startsWith('/pages/')) {
		uni.reLaunch({
			url: path
		});
		return;
	}
	uni.navigateTo({
		url: path
	});
};
