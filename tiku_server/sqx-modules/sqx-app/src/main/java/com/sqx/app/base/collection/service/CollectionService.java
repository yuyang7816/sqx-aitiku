package com.sqx.app.base.collection.service;

import com.sqx.app.base.collection.domain.bo.AppCollectionCreateBO;
import com.sqx.app.base.collection.domain.bo.AppCollectionQueryBO;
import com.sqx.app.base.collection.domain.vo.AppCollectionItemVO;
import com.sqx.app.base.collection.domain.vo.AppCollectionToggleResultVO;

import java.util.List;

/**
 * 用户端题目收藏服务
 */
public interface CollectionService {

    /**
     * 当前用户收藏列表
     */
    List<AppCollectionItemVO> listCollections(Long userId, AppCollectionQueryBO query);

    /**
     * 新增收藏（userId + questionId 去重，已存在则更新 subject）
     */
    AppCollectionToggleResultVO addCollection(Long userId, AppCollectionCreateBO create);

    /**
     * 取消收藏
     */
    void removeCollection(Long userId, String questionId);
}
