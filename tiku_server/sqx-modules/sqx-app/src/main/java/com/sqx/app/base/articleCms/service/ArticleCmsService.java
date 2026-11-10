package com.sqx.app.base.articleCms.service;

import com.sqx.app.base.articleCms.domain.vo.ArticleCmsVO;

/**
 * 协议与政策 CMS 服务
 */
public interface ArticleCmsService {

    /**
     * 按文章类型返回标题、副标题与富文本正文
     *
     * @param type agreement / privacy / about
     */
    ArticleCmsVO getByType(String type);
}
