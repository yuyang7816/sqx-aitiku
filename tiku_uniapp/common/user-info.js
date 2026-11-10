/**
 * 登录后拉取 GET /app/user，合并至 profile 与 aiQuestionAuthState
 * 模块二十二 P1#13 · vip/vipExpireAt 合并经 saveMemberState 触发 expireAt 降级校验
 */
import { getToken } from '@/common/auth-token';
import { syncNotebookAndCollectionFromApi } from '@/common/cloud-sync';
import {
	applyPostLoginProfile,
	getAuthState,
	getMemberState,
	getUserProfile,
	saveAuthState,
	saveMemberState,
	saveUserProfile,
	toAvatarText
} from '@/common/app-data';
import { resolveMemberLevel } from '@/common/member-rules';
import { get, postJson } from '@/common/api';

/**
 * 将 GET /app/user 出参映射为本地 Storage 补丁（服务端 grade/subjects 覆盖本地）
 * @param {object} data - AppUserProfileVO
 */
export const mapServerUserToLocal = (data = {}) => {
	const profilePatch = {};

	if (typeof data.ready === 'boolean') {
		profilePatch.ready = data.ready;
	}
	if (data.grade != null) {
		profilePatch.grade = data.grade;
	}
	if (data.gradeShort != null) {
		profilePatch.gradeShort = data.gradeShort;
	}
	if (data.version != null) {
		profilePatch.version = data.version;
	}
	if (Array.isArray(data.subjects)) {
		profilePatch.subjects = data.subjects;
	}
	if (data.nickname) {
		profilePatch.name = data.nickname;
		profilePatch.avatarText = toAvatarText(data.nickname);
	}
	if (data.avatar) {
		profilePatch.avatar = data.avatar;
	}
	if (data.userId != null) {
		profilePatch.userId = data.userId;
	}

	const authPatch = {};
	if (data.phone) {
		authPatch.phone = data.phone;
	}

	const memberPatch = {};
	if (typeof data.vip === 'boolean') {
		memberPatch.isVip = data.vip;
		memberPatch.level = resolveMemberLevel(data.vip);
	}
	if (data.vipExpireAt) {
		memberPatch.expireAt = String(data.vipExpireAt).trim().slice(0, 10);
	}

	return { profilePatch, authPatch, memberPatch };
};

/**
 * 本地仅保留 token 时：GET /app/user 成功后补写 loggedIn，供闪屏/守卫按 ready 路由
 * @param {object} data - AppUserProfileVO
 */
export const restoreAuthStateFromServerUser = (data = {}) => {
	const auth = getAuthState();
	if (auth.loggedIn === true) {
		return auth;
	}
	return saveAuthState({
		...auth,
		loggedIn: true,
		phone: data.phone || auth.phone || '',
		loginType: auth.loginType || 'sms'
	});
};

/** 合并服务端用户资料到 aiQuestionUserProfile / aiQuestionAuthState / 会员态 */
export const mergeServerUserInfo = (data = {}) => {
	const { profilePatch, authPatch, memberPatch } = mapServerUserToLocal(data);

	if (Object.keys(profilePatch).length) {
		saveUserProfile(profilePatch);
	}
	if (Object.keys(authPatch).length) {
		saveAuthState({
			...getAuthState(),
			...authPatch
		});
	}
	if (Object.keys(memberPatch).length) {
		const current = getMemberState();
		// 支付回调未落库：服务端 vip=false 但本地 expireAt 仍有效时保留本地会员态
		if (memberPatch.isVip === false && current.isVip === true && current.expireAt) {
			const expire = new Date(String(current.expireAt).trim());
			if (!Number.isNaN(expire.getTime())) {
				expire.setHours(23, 59, 59, 999);
				if (expire.getTime() >= Date.now()) {
					delete memberPatch.isVip;
					delete memberPatch.level;
				}
			}
		}
		saveMemberState({
			...current,
			...memberPatch
		});
	}

	return { profilePatch, authPatch, memberPatch };
};

/**
 * 拉取 GET /app/user 并合并到本地 Storage
 * @returns {Promise<object|null>} 成功返回服务端 data，失败返回 null
 */
export const fetchAndMergeUserInfo = async (options = {}) => {
	if (!getToken()) {
		return null;
	}
	try {
		const res = await get('/app/user', {}, {
			loading: false,
			toast: false,
			skipUnauthorizedRedirect: options.skipUnauthorizedRedirect === true
		});
		if (res && res.code === 0 && res.data) {
			mergeServerUserInfo(res.data);
			restoreAuthStateFromServerUser(res.data);
			return res.data;
		}
		return null;
	} catch (e) {
		return null;
	}
};

/**
 * 模块二十二 P1#14 · 计划页确认/编辑保存：POST /app/user 同步 grade/subjects/version/ready
 * 成功时 mergeServerUserInfo 写入本地 Storage；失败时不改 Storage
 * @returns {Promise<{ success: boolean, data?: object, msg?: string }>}
 */
export const syncUserProfileToServer = async ({
	grade,
	version,
	subjects,
	ready = true
} = {}) => {
	if (!getToken()) {
		return { success: false, msg: '请先登录' };
	}
	const subjectList = Array.isArray(subjects) ? subjects.filter(Boolean) : [];
	if (!subjectList.length) {
		return { success: false, msg: '请至少选择一门学科' };
	}
	try {
		const res = await postJson('/app/user', {
			grade,
			version,
			subjects: subjectList,
			ready: Boolean(ready)
		}, { toast: false });
		if (res && res.code === 0 && res.data) {
			mergeServerUserInfo(res.data);
			return { success: true, data: res.data };
		}
		return {
			success: false,
			msg: (res && res.msg) || '学习计划保存失败，请稍后重试'
		};
	} catch (e) {
		return { success: false, msg: '网络异常，请检查网络后重试' };
	}
};

/**
 * 头像昵称编辑：POST /app/user 同步 nickname/avatar，并携带当前学习计划字段
 * @returns {Promise<{ success: boolean, data?: object, msg?: string }>}
 */
export const updateUserAvatarNickname = async ({ nickname, avatar } = {}) => {
	if (!getToken()) {
		return { success: false, msg: '请先登录' };
	}

	const trimmedNickname = String(nickname || '').trim();
	if (!trimmedNickname) {
		return { success: false, msg: '请输入昵称' };
	}
	if (trimmedNickname.length > 20) {
		return { success: false, msg: '昵称最多 20 个字' };
	}

	const profile = getUserProfile();
	const subjectList = Array.isArray(profile.subjects) ? profile.subjects.filter(Boolean) : [];
	if (!profile.grade || !profile.version || !subjectList.length) {
		return { success: false, msg: '请先完善学习计划后再修改资料' };
	}

	const payload = {
		nickname: trimmedNickname,
		grade: profile.grade,
		version: profile.version,
		subjects: subjectList,
		ready: Boolean(profile.ready)
	};
	const avatarUrl = String(avatar || '').trim();
	if (avatarUrl) {
		payload.avatar = avatarUrl;
	}

	try {
		const res = await postJson('/app/user', payload, { toast: false });
		if (res && res.code === 0 && res.data) {
			mergeServerUserInfo(res.data);
			return { success: true, data: res.data };
		}
		return {
			success: false,
			msg: (res && res.msg) || '资料保存失败，请稍后重试'
		};
	} catch (e) {
		return { success: false, msg: '网络异常，请检查网络后重试' };
	}
};

/**
 * 登录成功后：优先 GET /app/user 合并资料；失败时回退登录接口 profilePatch
 */
export const syncUserInfoAfterLogin = async ({
	profilePatch,
	identityKey = '',
	prevIdentityKey = ''
} = {}) => {
	const userInfo = await fetchAndMergeUserInfo({ skipUnauthorizedRedirect: true });
	if (!userInfo) {
		applyPostLoginProfile({
			profilePatch,
			identityKey,
			prevIdentityKey
		});
	}
	// 云端同步异步执行，避免 401 或网络慢阻塞登录跳转
	syncNotebookAndCollectionFromApi().catch((e) => {
		console.warn('[syncUserInfoAfterLogin] cloud sync failed', e);
	});
	return userInfo;
};
