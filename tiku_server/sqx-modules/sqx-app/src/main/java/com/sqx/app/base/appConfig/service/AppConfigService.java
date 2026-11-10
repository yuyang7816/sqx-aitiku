package com.sqx.app.base.appConfig.service;

import com.sqx.app.base.appConfig.domain.bo.SysConfigInfoBO;
import com.sqx.app.base.appConfig.domain.vo.SysConfigVO;

/**
 * @author chengming
 * @since 2025/6/9
 */
public interface AppConfigService {
    SysConfigVO info(SysConfigInfoBO sysConfigInfoBO);
}
