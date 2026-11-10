/**
 * 模块二十二 P1#11 · 错题本/收藏云端同步
 * 错题本：已登录用户 GET /app/notebook 为唯一数据源
 * 触发：登录 syncUserInfoAfterLogin · App onLaunch · 错题本/收藏页 onShow · 交卷 syncWrongNotebook
 */
import {
	canSyncCollectionToApi,
	canSyncNotebookToApi,
	isAutoSyncEnabled,
	syncCollectionListFromApi,
	syncNotebookListFromApi
} from '@/common/app-data';

export {
	canSyncNotebookToApi,
	canSyncCollectionToApi,
	isAutoSyncEnabled,
	syncNotebookListFromApi,
	syncCollectionListFromApi
};

/** 并行拉取错题本与收藏云端列表（登录 / 启动 / 错题本页 onShow 时调用） */
export const syncNotebookAndCollectionFromApi = () => Promise.all([
	syncNotebookListFromApi(),
	syncCollectionListFromApi()
]);
