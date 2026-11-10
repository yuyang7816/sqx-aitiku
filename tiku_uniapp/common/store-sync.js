/**
 * 模块十九 · app-data 与 Pinia 双向同步（域外安全调用）
 */
import { pinia } from '@/stores/pinia';
import { useUserStore } from '@/stores/user';
import { useMemberStore } from '@/stores/member';
import { useNotebookStore } from '@/stores/notebook';

const safeRun = (fn) => {
	try {
		fn();
	} catch (e) {
		// Pinia 未挂载时忽略，回退 Storage 直读
	}
};

export const syncUserStoreAuth = (auth) => {
	safeRun(() => useUserStore(pinia).setAuth(auth));
};

export const syncUserStoreProfile = (profile) => {
	safeRun(() => useUserStore(pinia).setProfile(profile));
};

export const syncUserStoreResetAuth = () => {
	safeRun(() => useUserStore(pinia).resetAuth());
};

export const syncMemberStore = (member) => {
	safeRun(() => useMemberStore(pinia).setMember(member));
};

export const syncNotebookStoreList = (list) => {
	safeRun(() => useNotebookStore(pinia).setList(list));
};

export const readUserStoreProfile = () => {
	try {
		const store = useUserStore(pinia);
		if (store.hydrated) {
			return {
				...store.profile
			};
		}
	} catch (e) {
		// ignore
	}
	return null;
};

export const readUserStoreAuth = () => {
	try {
		const store = useUserStore(pinia);
		if (store.hydrated) {
			return {
				...store.auth
			};
		}
	} catch (e) {
		// ignore
	}
	return null;
};

export const readMemberStore = () => {
	try {
		const store = useMemberStore(pinia);
		if (store.hydrated) {
			return {
				...store.member
			};
		}
	} catch (e) {
		// ignore
	}
	return null;
};

export const readNotebookStoreList = () => {
	try {
		const store = useNotebookStore(pinia);
		if (store.hydrated) {
			return [...store.list];
		}
	} catch (e) {
		// ignore
	}
	return null;
};
