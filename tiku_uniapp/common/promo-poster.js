/**
 * 推广专属海报：管理端底图 + 底部用户信息/邀请码/二维码（canvas 合成）
 * - 小程序：使用服务端带 scene 的小程序码
 * - H5 / App：客户端本地生成普通二维码（邀请链接）
 */
import config from '@/common/config';
import { getUserProfile } from '@/common/app-data';
import { fetchInviteCode, fetchPromoSummary, formatCommissionAmount } from '@/common/promo-api';
import { wrapPosterTextLines } from '@/common/report-poster';
import { loadPosterImageInfo, loadPosterImagePath } from '@/common/poster-image-loader';
import { savePosterImage, sharePosterImage } from '@/common/poster-save-share';
import { LEGACY_APP_URL_KEY } from '@/common/storage-keys';

export const PROMO_POSTER_CANVAS_ID = 'promoPosterCanvas';
export const PROMO_POSTER_QR_CANVAS_ID = 'promoPosterQrCanvas';

const POSTER_CANVAS_WIDTH = 750;
const POSTER_FOOTER_HEIGHT = 220;
const POSTER_QR_SIZE = 140;
const POSTER_PADDING_X = 36;
const POSTER_QR_PADDING = 32;

/** 项目主题色（与 theme.scss 一致） */
const POSTER_COLOR = {
	footerBg: '#ffffff',
	footerBorder: 'rgba(0,0,0,0.06)',
	nickname: '#2f3440',
	inviteCode: '#2f3440',
	inviteText: '#8b93a7',
	qrBorder: '#eef2ff',
	qrPlaceholder: '#eef2ff',
	qrPlaceholderText: '#386df5'
};

/** 接口失败时的空邀请码 */
const PROMO_INVITE_EMPTY = {
	inviteCode: '',
	qrCodeUrl: '',
	posterImageUrl: '',
	inviteText: ''
};

/** 当前端是否应使用服务端小程序码（仅微信小程序） */
export const isMiniProgramPosterQr = () => {
	// #ifdef MP-WEIXIN
	return true;
	// #endif
	return false;
};

/** H5 / App 本地二维码内容：带 inviteCode 的登录页深链 */
export const buildPromoPosterQrText = (inviteCode = '') => {
	const code = String(inviteCode || '').trim();
	const inviteQuery = code ? `?inviteCode=${encodeURIComponent(code)}` : '';

	// #ifdef H5
	if (typeof window !== 'undefined' && window.location?.origin) {
		const base = `${window.location.origin}${window.location.pathname || '/'}`;
		return `${base}#/package/auth/login${inviteQuery}`;
	}
	// #endif

	try {
		const appUrl = uni.getStorageSync(LEGACY_APP_URL_KEY);
		if (appUrl) {
			const normalized = String(appUrl).trim().replace(/\/$/, '');
			return `${normalized}/#/package/auth/login${inviteQuery}`;
		}
	} catch (e) {
		// ignore
	}

	const domain = String(config.APIHOST || '').trim().replace(/\/sqx_fast\/?$/i, '');
	if (domain) {
		return `${domain}/#/package/auth/login${inviteQuery}`;
	}
	return `https://tiku.xianmaxiong.com/#/package/auth/login${inviteQuery}`;
};

export const resolvePromoPosterQrHint = () => (
	isMiniProgramPosterQr() ? '长按识别小程序码' : '扫码识别二维码'
);

/** 将服务端相对路径补全为可加载图片地址 */
export const resolveRemoteAssetUrl = (url = '') => {
	const normalized = String(url || '').trim();
	if (!normalized) {
		return '';
	}
	if (/^https?:\/\//i.test(normalized)) {
		return normalized;
	}
	if (normalized.startsWith('/')) {
		return `${config.APIHOST}${normalized}`;
	}
	return `${config.APIHOST}/${normalized}`;
};

/** 将邀请码/概览/昵称/管理端配置组装为海报展示结构 */
export const buildPromoPosterPayload = ({ invite = {}, summary = {}, profile = {} } = {}) => {
	const nickname = String(profile.name || '').trim() || '题库用户';
	const inviteCode = String(invite.inviteCode || '').trim();
	const inviteCount = Number(summary.inviteCount);
	const safeInviteCount = Number.isFinite(inviteCount) ? inviteCount : 0;
	const posterImageUrl = resolveRemoteAssetUrl(invite.posterImageUrl || '');
	const inviteText = String(invite.inviteText || '').trim() || '扫码体验 AI 智能提分';

	return {
		pageTitle: '邀请海报',
		badge: '邀请有礼',
		nickname,
		inviteCode,
		title: inviteCode ? `${nickname} 邀请你一起进步` : '邀请好友一起学习',
		sub: '每邀请一位好友开通会员，双方都可获得惊喜奖励',
		highlights: [
			{
				label: '累计收益',
				value: `¥${formatCommissionAmount(summary.totalCommission ?? 0)}`
			},
			{
				label: '邀请人数',
				value: `${safeInviteCount}人`
			},
			{
				label: '今日收益',
				value: `¥${formatCommissionAmount(summary.todayCommission ?? 0)}`
			}
		],
		posterImageUrl,
		inviteText,
		useServerMiniProgramQr: isMiniProgramPosterQr(),
		qrCodeUrl: isMiniProgramPosterQr() ? resolveRemoteAssetUrl(invite.qrCodeUrl || '') : '',
		localQrText: buildPromoPosterQrText(inviteCode),
		qrHint: resolvePromoPosterQrHint(),
		inviteCodeLabel: '我的邀请码',
		inviterFooter: `邀请人：${nickname}`
	};
};

/** 拉取推广海报数据；接口失败时使用空数据 + 本地 profile 昵称 */
export const fetchPromoPosterData = async () => {
	const profile = getUserProfile();
	const [inviteResult, summaryResult] = await Promise.all([
		fetchInviteCode(),
		fetchPromoSummary()
	]);

	return {
		payload: buildPromoPosterPayload({
			invite: inviteResult.invite || PROMO_INVITE_EMPTY,
			summary: summaryResult.summary || {},
			profile
		}),
		inviteFromApi: Boolean(inviteResult.fromApi),
		fromApi: Boolean(inviteResult.fromApi || summaryResult.fromApi)
	};
};

const drawRoundRect = (ctx, x, y, width, height, radius, fillColor) => {
	const r = Math.min(radius, width / 2, height / 2);
	ctx.save();
	ctx.beginPath();
	ctx.moveTo(x + r, y);
	ctx.lineTo(x + width - r, y);
	ctx.quadraticCurveTo(x + width, y, x + width, y + r);
	ctx.lineTo(x + width, y + height - r);
	ctx.quadraticCurveTo(x + width, y + height, x + width - r, y + height);
	ctx.lineTo(x + r, y + height);
	ctx.quadraticCurveTo(x, y + height, x, y + height - r);
	ctx.lineTo(x, y + r);
	ctx.quadraticCurveTo(x, y, x + r, y);
	ctx.closePath();
	if (fillColor) {
		ctx.setFillStyle(fillColor);
		ctx.fill();
	}
	ctx.restore();
};

const loadImageInfo = (url = '') => loadPosterImageInfo(url);

/** 根据底图比例计算 canvas 尺寸（底图全高，底部信息条叠加在底图下方区域） */
export const resolvePromoPosterCanvasSize = (imageWidth = POSTER_CANVAS_WIDTH, imageHeight = POSTER_CANVAS_WIDTH) => {
	const safeW = Number(imageWidth) > 0 ? Number(imageWidth) : POSTER_CANVAS_WIDTH;
	const safeH = Number(imageHeight) > 0 ? Number(imageHeight) : POSTER_CANVAS_WIDTH;
	const bgHeight = Math.round(POSTER_CANVAS_WIDTH * (safeH / safeW));
	const footerHeight = POSTER_FOOTER_HEIGHT;
	return {
		width: POSTER_CANVAS_WIDTH,
		bgHeight,
		footerHeight,
		height: bgHeight
	};
};

/** 预加载底图与二维码，并返回 canvas 尺寸（绘制前需先更新 canvas 高度） */
export const preparePromoPosterAssets = async (payload = {}, fallbackQrPath = '') => {
	const bgInfo = await loadImageInfo(payload.posterImageUrl);
	const size = resolvePromoPosterCanvasSize(bgInfo.width, bgInfo.height);
	const qrPath = await loadQrImagePath(payload, fallbackQrPath);
	return { bgInfo, size, qrPath };
};

const drawFooterOverlay = (
	ctx,
	payload = {},
	qrImagePath = '',
	width = POSTER_CANVAS_WIDTH,
	footerY = 0,
	footerHeight = POSTER_FOOTER_HEIGHT
) => {
	ctx.setFillStyle(POSTER_COLOR.footerBg);
	ctx.fillRect(0, footerY, width, footerHeight);

	ctx.setStrokeStyle(POSTER_COLOR.footerBorder);
	ctx.setLineWidth(1);
	ctx.beginPath();
	ctx.moveTo(0, footerY);
	ctx.lineTo(width, footerY);
	ctx.stroke();

	const textX = POSTER_PADDING_X;
	const qrSize = POSTER_QR_SIZE;
	const qrX = width - qrSize - POSTER_QR_PADDING;
	const qrY = footerY + (footerHeight - qrSize) / 2;

	ctx.setFillStyle(POSTER_COLOR.nickname);
	ctx.setFontSize(38);
	ctx.setTextAlign('left');
	ctx.fillText(String(payload.nickname || '题库用户'), textX, footerY + 66);

	ctx.setFillStyle(POSTER_COLOR.inviteCode);
	ctx.setFontSize(30);
	const codeLabel = `邀请码：${String(payload.inviteCode || '—')}`;
	ctx.fillText(codeLabel, textX, footerY + 112);

	ctx.setFillStyle(POSTER_COLOR.inviteText);
	ctx.setFontSize(24);
	const inviteLines = wrapPosterTextLines(payload.inviteText, 14).slice(0, 2);
	let textCursorY = footerY + 152;
	inviteLines.forEach((line) => {
		ctx.fillText(line, textX, textCursorY);
		textCursorY += 32;
	});

	if (qrImagePath) {
		drawRoundRect(ctx, qrX - 8, qrY - 8, qrSize + 16, qrSize + 16, 12, POSTER_COLOR.footerBg);
		ctx.setStrokeStyle(POSTER_COLOR.qrBorder);
		ctx.setLineWidth(1);
		ctx.strokeRect(qrX - 8, qrY - 8, qrSize + 16, qrSize + 16);
		ctx.drawImage(qrImagePath, qrX, qrY, qrSize, qrSize);
	} else {
		drawRoundRect(ctx, qrX, qrY, qrSize, qrSize, 12, POSTER_COLOR.qrPlaceholder);
		ctx.setStrokeStyle(POSTER_COLOR.qrBorder);
		ctx.setLineWidth(1);
		ctx.strokeRect(qrX, qrY, qrSize, qrSize);
		ctx.setFillStyle(POSTER_COLOR.qrPlaceholderText);
		ctx.setFontSize(24);
		ctx.setTextAlign('center');
		ctx.fillText('码', qrX + qrSize / 2, qrY + qrSize / 2 + 8);
		ctx.setTextAlign('left');
	}
};

/** 在 canvas 上绘制推广海报（底图 + 底部叠加用户信息/二维码） */
export const drawPromoPosterCanvas = (
	ctx,
	payload = {},
	backgroundPath = '',
	qrImagePath = '',
	width = POSTER_CANVAS_WIDTH,
	totalHeight = POSTER_CANVAS_WIDTH
) => {
	const footerHeight = POSTER_FOOTER_HEIGHT;
	const bgHeight = totalHeight;
	const footerY = Math.max(0, bgHeight - footerHeight);

	if (backgroundPath) {
		ctx.drawImage(backgroundPath, 0, 0, width, bgHeight);
	} else {
		ctx.setFillStyle('#eef4ff');
		ctx.fillRect(0, 0, width, bgHeight);
	}

	drawFooterOverlay(ctx, payload, qrImagePath, width, footerY, footerHeight);
	return totalHeight;
};

const loadQrImagePath = (payload = {}, fallbackQrPath = '') => {
	if (!isMiniProgramPosterQr()) {
		return Promise.resolve(String(fallbackQrPath || '').trim());
	}
	const serverUrl = String(payload.qrCodeUrl || '').trim();
	if (serverUrl) {
		return loadPosterImagePath(serverUrl, fallbackQrPath);
	}
	return Promise.resolve(String(fallbackQrPath || '').trim());
};

/**
 * 生成推广海报图片
 * @param {string} canvasId
 * @param {object} payload
 * @param {string} fallbackQrPath up-qrcode 兜底路径
 * @param {object} componentInstance
 */
export const renderPromoPosterImage = async (
	canvasId,
	payload = {},
	fallbackQrPath = '',
	componentInstance,
	prepared
) => {
	if (!String(payload.posterImageUrl || '').trim()) {
		throw new Error('请先在管理端配置邀请海报底图');
	}

	const assets = prepared || await preparePromoPosterAssets(payload, fallbackQrPath);
	const { bgInfo, size, qrPath } = assets;
	const exportDelay = 600;
	const footerY = Math.max(0, size.height - POSTER_FOOTER_HEIGHT);

	return new Promise((resolve, reject) => {
		const ctx = uni.createCanvasContext(canvasId, componentInstance);

		if (bgInfo.path) {
			ctx.drawImage(bgInfo.path, 0, 0, size.width, size.height);
		} else {
			ctx.setFillStyle('#eef4ff');
			ctx.fillRect(0, 0, size.width, size.height);
		}

		ctx.draw(false, () => {
			setTimeout(() => {
				const overlayCtx = uni.createCanvasContext(canvasId, componentInstance);
				drawFooterOverlay(
					overlayCtx,
					payload,
					qrPath,
					size.width,
					footerY,
					POSTER_FOOTER_HEIGHT
				);
				overlayCtx.draw(true, () => {
					setTimeout(() => {
						uni.canvasToTempFilePath({
							canvasId,
							width: size.width,
							height: size.height,
							destWidth: size.width,
							destHeight: size.height,
							fileType: 'png',
							success: (res) => resolve({
								path: res.tempFilePath,
								width: size.width,
								height: size.height
							}),
							fail: (err) => reject(err || new Error('海报导出失败'))
						}, componentInstance);
					}, exportDelay);
				});
			}, 120);
		});
	});
};

/** 保存推广海报图片 */
export const savePromoPosterImage = async (filePath = '') => {
	await savePosterImage(filePath, { fileNamePrefix: '邀请海报' });
};

/** 分享推广海报图片 */
export const sharePromoPosterImage = async (filePath = '', payload = {}) => {
	const inviteText = String(payload.inviteText || '').trim();
	const code = String(payload.inviteCode || '').trim();
	const shareText = code
		? `${inviteText || '邀请你一起进步'}（邀请码 ${code}）`
		: (inviteText || '邀请你一起进步，开通会员双方都有奖励');
	await sharePosterImage(filePath, {
		fileNamePrefix: '邀请海报',
		shareTitle: '邀请海报',
		shareText
	});
};

export { wrapPosterTextLines };
