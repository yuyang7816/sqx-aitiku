package com.sqx.app.base.search.service;

import com.sqx.app.base.search.domain.bo.AppSearchRecordUpsertBO;
import com.sqx.app.base.search.domain.vo.AppSearchRecordItemVO;

import java.util.List;

/**
 * 用户端搜题记录云端同步
 */
public interface SearchRecordService {

    /**
     * 返回当前用户最近搜题记录（最多 20 条，按最近更新时间倒序）
     */
    List<AppSearchRecordItemVO> listRecords(Long userId);

    /**
     * 写入或更新单条记录：同 title 去重并置顶，超出上限删除最旧记录
     */
    AppSearchRecordItemVO upsertRecord(Long userId, AppSearchRecordUpsertBO upsert);

    /**
     * 清空当前用户全部搜题记录
     */
    void clearRecords(Long userId);
}
