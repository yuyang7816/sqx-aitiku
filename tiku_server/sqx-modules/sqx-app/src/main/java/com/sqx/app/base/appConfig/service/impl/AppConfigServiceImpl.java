package com.sqx.app.base.appConfig.service.impl;

import com.sqx.app.base.appConfig.domain.bo.SysConfigInfoBO;
import com.sqx.app.base.appConfig.domain.vo.SysConfigVO;
import com.sqx.app.base.appConfig.service.AppConfigService;
import com.sqx.app.base.enums.AppError;
import com.sqx.app.base.utils.SysConfigUtil;
import com.sqx.common.core.exception.BizException;
import com.sqx.common.redis.utils.ConfigUtil;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * @author chengming
 * @since 2025/6/9
 */
@Service
public class AppConfigServiceImpl implements AppConfigService {
    @Override
    public SysConfigVO info(SysConfigInfoBO sysConfigInfoBO) {
        String cacheConfigValue = SysConfigUtil.getConfigValue(sysConfigInfoBO.getKey());
        if (Objects.nonNull(cacheConfigValue)) {
            SysConfigVO vo = new SysConfigVO();
            vo.setValue(cacheConfigValue);
            return vo;
        }

        String configValue = ConfigUtil.getConfigValue(sysConfigInfoBO.getKey());
        if (Objects.isNull(configValue)) {
            throw new BizException(AppError.CONFIG_NOT_FOUND);
        }

        SysConfigVO vo = new SysConfigVO();
        vo.setValue(configValue);
        return vo;
    }
}
