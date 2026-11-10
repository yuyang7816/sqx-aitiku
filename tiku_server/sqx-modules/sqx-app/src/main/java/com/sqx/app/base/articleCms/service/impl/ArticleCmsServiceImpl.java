package com.sqx.app.base.articleCms.service.impl;

import com.sqx.app.base.articleCms.constants.ArticleCmsConstant;
import com.sqx.app.base.articleCms.constants.ArticleCmsConstant.ArticleTypeDef;
import com.sqx.app.base.articleCms.domain.vo.ArticleCmsVO;
import com.sqx.app.base.articleCms.service.ArticleCmsService;
import com.sqx.app.base.articleCms.support.ArticleConfigValueReader;
import com.sqx.app.base.enums.AppError;
import com.sqx.common.core.exception.BizException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 协议与政策 CMS 服务实现（数据来源 sys_config / Redis 缓存）
 */
@Service
public class ArticleCmsServiceImpl implements ArticleCmsService {

    @Resource
    private ArticleConfigValueReader articleConfigValueReader;

    @Override
    public ArticleCmsVO getByType(String type) {
        ArticleTypeDef def = ArticleCmsConstant.findByType(type);
        if (def == null) {
            throw new BizException(AppError.INFO_NOT_FOUND);
        }

        ArticleCmsVO vo = new ArticleCmsVO();
        vo.setType(def.getType());
        vo.setTitle(resolveValue(def.getTitleKey(), def.getDefaultTitle()));
        vo.setSub(resolveValue(def.getSubKey(), def.getDefaultSub()));
        vo.setContent(resolveValue(def.getContentKey(), def.getDefaultContent()));
        return vo;
    }

    private String resolveValue(String paramKey, String defaultValue) {
        String value = articleConfigValueReader.getValueOrDefault(paramKey);
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        return value;
    }
}
