package com.sqx.system.service;


import com.sqx.common.core.domain.Result;
import com.sqx.common.mybatis.utils.PageUtils;
import com.sqx.system.db.entity.SysConfigEntity;
import com.sqx.system.domain.res.SysConfigListRes;

import java.util.List;
import java.util.Map;

/**
 * 系统配置信息
 *
 */
public interface SysConfigService {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 保存配置信息
     */
    void saveConfig(SysConfigEntity config);

    /**
     * 更新配置信息
     */
    void update(SysConfigEntity config);

    /**
     * 删除配置信息
     */
    void deleteBatch(Long[] ids);

    Result<List<SysConfigListRes>> listInfo(Map<String, Object> params);

    Result<Void> refresh();

    Result info(Long id);
}
