/** 页面路由与导航常量（非业务 mock 数据） */

/** OCR 搜题页（首页搜索框、拍照搜题快捷入口共用） */
export const OCR_RESULT_PAGE = '/package/question/ocr-result';

/** 找题枢纽页（拍照/粘贴/知识点/我的错题/收藏/记录） */
export const QUESTION_SEARCH_PAGE = '/package/question/search';

/** @deprecated 手动 OCR 搜题仍保留为二级入口 */
export const OCR_MANUAL_SEARCH_URL = `${OCR_RESULT_PAGE}?manual=1`;

/** 答题页（学习卡片错题练习、推荐错题练习等共用） */
export const QUESTION_ANSWER_PAGE = '/package/question/answer';

/** 答题卡页（从答题页跳转，题量多时独立滚动） */
export const QUESTION_ANSWER_SHEET_PAGE = '/package/question/answer-sheet';

/** 练习结果页（交卷后跳转） */
export const QUESTION_RESULT_PAGE = '/package/question/result';

/** 题目解析页（结果页/错题本「查看解析」共用） */
export const QUESTION_ANALYSIS_PAGE = '/package/question/analysis';

/** 高清视频解析页（会员权益，题目解析页入口） */
export const QUESTION_VIDEO_ANALYSIS_PAGE = '/package/question/video-analysis';

/** 学习卡片点击错题练习时的 practiceSource.title */
export const STUDY_CARD_WRONG_TITLE = '今日待复习错题';

/** 月度报告「今晚先重做错题」按钮写入 practiceSource.title */
export const REPORT_WRONG_TITLE = '报告推荐错题强化';

/** 分享海报页（月度报告 scene=report、推广中心 scene=promo 共用） */
export const POSTER_PAGE = '/package/common/poster';

/** 月度报告「分享海报」跳转 URL（scene=report） */
export const REPORT_POSTER_URL = `${POSTER_PAGE}?scene=report`;

/** 按业务月份（yyyy-MM）跳转报告海报页 */
export const buildReportPosterUrl = (month = '') => {
	const key = String(month || '').trim();
	return key
		? `${POSTER_PAGE}?scene=report&month=${encodeURIComponent(key)}`
		: REPORT_POSTER_URL;
};

/** 错题本「重做」按钮写入 practiceSource.title（mode=single + questionTitle） */
export const NOTEBOOK_REDO_TITLE = '错题重做';

/** 首页 Tab 页（底部导航「首页」、登录就绪默认落地页共用） */
export const HOME_TAB_PAGE = '/pages/index/index';

/** 刷题中心 Tab 页（8 宫格「刷题中心」、首页「查看全部」共用） */
export const PRACTICE_TAB_PAGE = '/pages/practice/practice';

/** AI 学习 Tab 页（底部导航中间凸起「AI学习」共用） */
export const AI_TAB_PAGE = '/pages/ai/ai';

/** 错题本 Tab 页（8 宫格「错题本」、我的页「我的错题本」共用） */
export const NOTEBOOK_TAB_PAGE = '/pages/notebook/notebook';

/** 我的 Tab 页（底部导航「我的」共用） */
export const MY_TAB_PAGE = '/pages/my/my';

/** 我的收藏页（我的页「我的收藏」菜单共用） */
export const COLLECTION_PAGE = '/package/profile/collection';

/** 试卷中心页（专项/真题题集列表） */
export const PAPER_CENTER_PAGE = '/package/practice/paper-center';

/** 章节练习页（为你推荐「本周提分练习」共用） */
export const CHAPTER_PRACTICE_PAGE = '/package/practice/chapter';

/** 学情概览页（8 宫格「学情报告」共用） */
export const ANALYSIS_DETAIL_PAGE = '/package/analysis/detail';

/** 月度学习报告页 */
export const REPORT_PAGE = '/package/analysis/report';

/** 历史月度报告列表页 */
export const REPORT_HISTORY_PAGE = '/package/analysis/report-history';

/** 按业务月份（yyyy-MM）跳转月度报告 */
export const buildReportPageUrl = (month = '') => {
	const key = String(month || '').trim();
	return key ? `${REPORT_PAGE}?month=${encodeURIComponent(key)}` : REPORT_PAGE;
};

/** 会员中心页（8 宫格「会员中心」、Hero Banner 按钮共用） */
export const MEMBER_CENTER_PAGE = '/package/member/center';

/** 会员支付页（center 页 navigateTo 落地） */
export const MEMBER_PAY_PAGE = '/package/member/pay';

/** 会员支付成功页（pay 页 redirectTo 落地） */
export const MEMBER_SUCCESS_PAGE = '/package/member/success';

/** 按套餐信息构建会员支付页 URL（兼容旧入口，实际跳转 center 弹框支付） */
export const buildMemberPayPageUrl = ({ name = '', price = 0, days = 0 } = {}) => buildMemberCenterPayUrl({ name, price, days });

/** 会员中心页 URL，openPay=1 时进入后自动弹出支付弹框 */
export const buildMemberCenterPayUrl = ({ name = '', price = 0, days = 0 } = {}) => {
	const query = [
		`name=${encodeURIComponent(String(name || '').trim())}`,
		`price=${Number(price) || 0}`,
		`days=${Number(days) || 0}`,
		'openPay=1'
	].join('&');
	return `${MEMBER_CENTER_PAGE}?${query}`;
};

/** 会员订单列表页（success 页「查看订单详情」落地） */
export const MEMBER_ORDER_LIST_PAGE = '/package/member/order-list';

/** AI 答疑页（8 宫格「在线客服」无 m02 配置时的 fallback） */
export const CUSTOMER_SERVICE_AI_CHAT_PAGE = '/package/ai/chat';

/** 学习日历页（打卡/连续天数） */
export const STUDY_CALENDAR_PAGE = '/package/analysis/calendar';

/** 首页搜索框占位文案 */
export const HOME_SEARCH_PLACEHOLDER = '找题 / 搜知识点 / 拍照搜题';

/** 用户协议页 */
export const AGREEMENT_PAGE = '/package/common/article?type=agreement';

/** 隐私政策页 */
export const PRIVACY_PAGE = '/package/common/article?type=privacy';

/** 关于我们页 */
export const ABOUT_PAGE = '/package/common/article?type=about';

/** 帮助与反馈页 */
export const HELP_FEEDBACK_PAGE = '/package/profile/help-feedback';

/** 推广中心主页 */
export const PROMO_INDEX_PAGE = '/package/promo/index';

/** 推广收益明细页 */
export const PROMO_DETAIL_PAGE = '/package/promo/detail';

/** 邀请人分佣 / 邀请人数列表页（scene=commission|invite） */
export const PROMO_INVITE_LIST_PAGE = '/package/promo/invite-list';

/** 构建邀请列表页 URL */
export const buildPromoInviteListUrl = (scene = 'invite') => {
	const key = String(scene || '').trim() === 'commission' ? 'commission' : 'invite';
	return `${PROMO_INVITE_LIST_PAGE}?scene=${key}`;
};

/** 推广立即提现页 */
export const PROMO_WITHDRAW_PAGE = '/package/promo/withdraw';

/** 推广提现账户绑定页 */
export const PROMO_WITHDRAW_ACCOUNT_PAGE = '/package/promo/withdraw-account';

/** 推广提现记录列表页 */
export const PROMO_WITHDRAW_RECORDS_PAGE = '/package/promo/withdraw-records';

/** 推广中心「生成专属推广海报」跳转 URL（scene=promo） */
export const PROMO_POSTER_URL = '/package/common/poster?scene=promo';

/** 头像昵称编辑页 */
export const PROFILE_EDIT_PAGE = '/package/profile/edit';

/** 勋章墙页 */
export const BADGE_WALL_PAGE = '/package/profile/badges';

export const homeQuickActions = [{
	title: '刷题中心',
	icon: 'edit-pen-fill',
	color: '#4D7CFE',
	bg: 'linear-gradient(180deg, #EAF1FF 0%, #DCE8FF 100%)',
	url: PRACTICE_TAB_PAGE
}, {
	title: '拍照搜题',
	icon: 'camera-fill',
	color: '#28B66A',
	bg: 'linear-gradient(180deg, #E6FAF0 0%, #D5F6E5 100%)',
	action: 'ocr-camera',
	url: OCR_RESULT_PAGE
}, {
	title: '试卷中心',
	icon: 'file-text-fill',
	color: '#FF8A3D',
	bg: 'linear-gradient(180deg, #FFF3E8 0%, #FFE7D0 100%)',
	url: PAPER_CENTER_PAGE
}, {
	title: '错题本',
	icon: 'bookmark-fill',
	color: '#FF5E66',
	bg: 'linear-gradient(180deg, #FFEDEE 0%, #FFE1E3 100%)',
	url: NOTEBOOK_TAB_PAGE
}, {
	title: '学情报告',
	icon: 'integral-fill',
	color: '#975CFF',
	bg: 'linear-gradient(180deg, #F4EAFF 0%, #ECDDFF 100%)',
	url: ANALYSIS_DETAIL_PAGE
}, {
	title: '会员中心',
	icon: 'coupon-fill',
	color: '#D7A421',
	bg: 'linear-gradient(180deg, #FFF7DB 0%, #FFF0BC 100%)',
	url: MEMBER_CENTER_PAGE
}, {
	title: '学习日历',
	icon: 'calendar-fill',
	color: '#24B7A8',
	bg: 'linear-gradient(180deg, #E8FAF8 0%, #D7F5F1 100%)',
	url: STUDY_CALENDAR_PAGE
}, {
	title: '在线客服',
	icon: 'server-fill',
	color: '#7685FF',
	bg: 'linear-gradient(180deg, #EEF0FF 0%, #E0E4FF 100%)',
	action: 'customer-service',
	url: CUSTOMER_SERVICE_AI_CHAT_PAGE
}];
