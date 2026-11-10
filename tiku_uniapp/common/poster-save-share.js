/**
 * 海报保存与分享：相册写入权限、保存图片、分享图片、小程序转发配置
 */

/** 解析 saveImageToPhotosAlbum 失败原因 */
const parseSaveAlbumError = (err = {}) => {
	const errMsg = String(err.errMsg || err.message || '').toLowerCase();
	if (errMsg.includes('auth deny') || errMsg.includes('authorize') || errMsg.includes('permission')) {
		return '未获得相册权限，请在设置中开启';
	}
	if (errMsg.includes('cancel')) {
		return '已取消保存';
	}
	return '保存失败，请重试';
};

/** 请求相册写入权限（微信小程序 / App） */
export const ensurePhotosAlbumPermission = () => new Promise((resolve, reject) => {
	// #ifdef H5
	resolve();
	return;
	// #endif

	uni.getSetting({
		success: (res) => {
			if (res.authSetting?.['scope.writePhotosAlbum']) {
				resolve();
				return;
			}
			uni.authorize({
				scope: 'scope.writePhotosAlbum',
				success: resolve,
				fail: () => {
					uni.showModal({
						title: '需要相册权限',
						content: '保存海报需要访问您的相册，请在设置中开启',
						confirmText: '去设置',
						cancelText: '取消',
						success: (modalRes) => {
							if (!modalRes.confirm) {
								reject(new Error('未获得相册权限'));
								return;
							}
							uni.openSetting({
								success: (settingRes) => {
									if (settingRes.authSetting?.['scope.writePhotosAlbum']) {
										resolve();
										return;
									}
									reject(new Error('未获得相册权限'));
								},
								fail: () => reject(new Error('打开设置失败'))
							});
						}
					});
				}
			});
		},
		fail: () => reject(new Error('获取权限状态失败'))
	});
});

/**
 * 保存海报到相册（H5 触发下载）
 * @param {string} filePath canvas 导出的临时图片路径
 * @param {{ fileNamePrefix?: string }} options
 */
export const savePosterImage = async (filePath = '', { fileNamePrefix = '海报' } = {}) => {
	const path = String(filePath || '').trim();
	if (!path) {
		throw new Error('海报图片无效');
	}

	// #ifdef H5
	const link = document.createElement('a');
	link.href = path;
	link.download = `${fileNamePrefix}-${Date.now()}.png`;
	document.body.appendChild(link);
	link.click();
	document.body.removeChild(link);
	return;
	// #endif

	await ensurePhotosAlbumPermission();

	await new Promise((resolve, reject) => {
		uni.saveImageToPhotosAlbum({
			filePath: path,
			success: resolve,
			fail: (err) => reject(new Error(parseSaveAlbumError(err)))
		});
	});
};

/**
 * 分享海报图片（H5 系统分享 / 微信图片分享菜单 / 其他端保存后提示）
 * @param {string} filePath
 * @param {{ fileNamePrefix?: string, shareTitle?: string, shareText?: string }} options
 */
export const sharePosterImage = async (
	filePath = '',
	{ fileNamePrefix = '海报', shareTitle = '分享海报', shareText = '快来看看我的海报' } = {}
) => {
	const path = String(filePath || '').trim();
	if (!path) {
		throw new Error('海报图片无效');
	}

	// #ifdef H5
	if (navigator.share && typeof fetch === 'function') {
		try {
			const response = await fetch(path);
			const blob = await response.blob();
			const file = new File([blob], `${fileNamePrefix}-${Date.now()}.png`, { type: 'image/png' });
			await navigator.share({
				title: shareTitle,
				text: shareText,
				files: [file]
			});
			return;
		} catch (e) {
			if (String(e?.name || '') === 'AbortError') {
				return;
			}
		}
	}
	await savePosterImage(path, { fileNamePrefix });
	uni.showToast({ title: '图片已下载，可手动分享', icon: 'none' });
	return;
	// #endif

	// #ifdef MP-WEIXIN
	await new Promise((resolve, reject) => {
		if (typeof wx !== 'undefined' && wx.showShareImageMenu) {
			wx.showShareImageMenu({
				path,
				success: resolve,
				fail: (err) => reject(new Error(parseSaveAlbumError(err)))
			});
			return;
		}
		reject(new Error('当前微信版本不支持图片分享'));
	});
	return;
	// #endif

	await savePosterImage(path, { fileNamePrefix });
	uni.showToast({ title: '图片已保存到相册', icon: 'none' });
};

/**
 * 构建 onShareAppMessage 转发配置（微信小程序右上角菜单 / open-type=share）
 */
export const buildPosterShareAppMessage = ({
	scene = 'report',
	imageUrl = '',
	month = '',
	title = '',
	inviteCode = ''
} = {}) => {
	const normalizedScene = String(scene || 'report').trim() || 'report';
	const monthKey = String(month || '').trim();
	const code = String(inviteCode || '').trim();

	let path = '/package/common/poster';
	if (normalizedScene === 'promo') {
		path = '/package/common/poster?scene=promo';
	} else if (monthKey) {
		path = `/package/common/poster?scene=report&month=${encodeURIComponent(monthKey)}`;
	} else {
		path = '/package/common/poster?scene=report';
	}

	let defaultTitle = '分享我的月度学习报告';
	if (normalizedScene === 'promo') {
		defaultTitle = code
			? `邀请你一起进步，我的邀请码：${code}`
			: '邀请你一起进步，开通会员双方都有奖励';
	}

	return {
		title: String(title || '').trim() || defaultTitle,
		path,
		imageUrl: String(imageUrl || '').trim()
	};
};
