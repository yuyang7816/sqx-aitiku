/**
 * 模块十九 · Store 入口：hydrate 全量业务态
 */
export { pinia } from '@/stores/pinia';
export { useUserStore } from '@/stores/user';
export { useMemberStore } from '@/stores/member';
export { useNotebookStore } from '@/stores/notebook';

import { pinia } from '@/stores/pinia';
import { useUserStore } from '@/stores/user';
import { useMemberStore } from '@/stores/member';
import { useNotebookStore } from '@/stores/notebook';

/** App onLaunch 调用：从 Storage 灌入 user / member / notebook 内存态 */
export const hydrateAllStores = () => {
	useUserStore(pinia).hydrateFromStorage();
	useMemberStore(pinia).hydrateFromStorage();
	useNotebookStore(pinia).hydrateFromStorage();
};
