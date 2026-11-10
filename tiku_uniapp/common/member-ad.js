/**
 * 会员广告位（已全站关闭展示，保留 API 供后续运营开关）
 */
import { MEMBER_CENTER_PAGE } from '@/common/routes';

const defaultMemberAd = {
	title: '开通 AI 至尊会员',
	sub: '无限次 AI 答疑与拍照搜题',
	link: MEMBER_CENTER_PAGE
};

const memberAdScenes = {
	home: defaultMemberAd,
	ai: defaultMemberAd,
	practice: defaultMemberAd,
	notebook: defaultMemberAd,
	my: defaultMemberAd
};

/** 是否应展示广告位（产品侧已关闭全站广告模块） */
export const shouldShowMemberAd = () => false;

/**
 * 按场景获取广告位文案与跳转
 * @param {string} scene home | ai | practice | notebook | my
 */
export const getMemberAdByScene = (scene = 'home') => {
	const key = String(scene || 'home').trim();
	return memberAdScenes[key] || defaultMemberAd;
};

/**
 * 广告位点击跳转（内部 uniapp 路径）
 * @param {string} link
 */
export const openMemberAdLink = (link = '') => {
	const path = String(link || '').trim();
	if (!path) {
		return;
	}
	if (path.startsWith('/pages/')) {
		uni.reLaunch({ url: path });
		return;
	}
	uni.navigateTo({ url: path });
};
