package com.sqx.system.db.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sqx.system.db.entity.SysConfigEntity;

/**
 * 系统配置信息
 *
 */
public interface ISysConfigService extends IService<SysConfigEntity> {


    String getConfigValue(String configKey);

    void loadSysConfig();
}
