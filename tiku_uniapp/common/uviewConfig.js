/**
 * uview-plus 全局配置：图标字体统一走 Vite 打包后的本地资源，
 * 避免 CSS @font-face 与 uni.loadFontFace 指向不同路径导致 H5 图标空白。
 */
import iconFontUrl from '@/static/fonts/uview-icon.ttf';

/** Vite 构建后的字体路径，例如 /assets/uview-icon-xxxxx.ttf */
export const UVIEW_ICON_FONT_URL = iconFontUrl;

/** uni.loadFontFace 在 H5 上需要绝对地址，否则 FontFace API 可能加载失败 */
export function resolveIconFontUrl(url = UVIEW_ICON_FONT_URL) {
	if (typeof window !== 'undefined' && typeof url === 'string' && url.startsWith('/')) {
		return `${window.location.origin}${url}`;
	}
	return url;
}

export function getUviewInstallOptions() {
	return {
		options: {
			config: {
				iconUrl: resolveIconFontUrl(),
				loadFontOnce: true
			}
		}
	};
}
