/**
 * 个人中心 UI 配置（菜单、设置项，非业务 mock 数据）
 */
import {
	ABOUT_PAGE,
	COLLECTION_PAGE,
	HELP_FEEDBACK_PAGE,
	NOTEBOOK_TAB_PAGE,
	PROMO_INDEX_PAGE
} from '@/common/routes';

export const myMenus = [{
	title: '我的错题本',
	icon: 'bookmark',
	color: '#FF6871',
	bg: '#FFECEE',
	url: NOTEBOOK_TAB_PAGE
}, {
	title: '我的收藏',
	icon: 'star',
	color: '#FF9A48',
	bg: '#FFF2E7',
	url: COLLECTION_PAGE
}, {
	title: '我的推广（赚佣金）',
	icon: 'rmb-circle',
	color: '#35C96F',
	bg: '#E8FBEE',
	url: PROMO_INDEX_PAGE
}, {
	title: '帮助与反馈',
	icon: 'file-text',
	color: '#6A92FF',
	bg: '#EEF3FF',
	url: HELP_FEEDBACK_PAGE
}, {
	title: '关于我们',
	icon: 'info-circle',
	color: '#7E8797',
	bg: '#F0F2F6',
	url: ABOUT_PAGE
}];

export const settingItems = [{
	key: 'pushEnabled',
	title: '学习提醒',
	copy: '开启后将收到计划、复习和报告提醒'
}, {
	key: 'soundEnabled',
	title: '提示音',
	copy: '答题和消息操作时提供轻量提示反馈'
}, {
	key: 'autoSync',
	title: '自动同步学习记录',
	copy: '跨端登录时自动恢复收藏、错题和练习数据'
}];
