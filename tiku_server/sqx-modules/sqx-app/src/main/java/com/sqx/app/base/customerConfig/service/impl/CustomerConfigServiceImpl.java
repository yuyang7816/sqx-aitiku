package com.sqx.app.base.customerConfig.service.impl;

import com.sqx.app.base.appConfig.domain.vo.SysConfigVO;
import com.sqx.app.base.customerConfig.constants.CustomerConfigConstant;
import com.sqx.app.base.customerConfig.domain.vo.CustomerConfigVO;
import com.sqx.app.base.customerConfig.service.CustomerConfigService;
import com.sqx.app.base.customerConfig.support.CustomerConfigValueReader;
import com.sqx.app.base.enums.AppError;
import com.sqx.common.core.exception.BizException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 客服配置服务实现（数据来自 sys_config / Redis 缓存）
 */
@Service
public class CustomerConfigServiceImpl implements CustomerConfigService {

    @Resource
    private CustomerConfigValueReader customerConfigValueReader;

    @Override
    public SysConfigVO getByLegacyTypeId(Long typeId) {
        String paramKey = CustomerConfigConstant.LEGACY_TYPE_KEY_MAP.get(typeId);
        if (paramKey == null) {
            throw new BizException(AppError.INFO_NOT_FOUND);
        }

        SysConfigVO vo = new SysConfigVO();
        vo.setValue(getConfigValueOrEmpty(paramKey));
        return vo;
    }

    @Override
    public CustomerConfigVO getCustomerConfig() {
        CustomerConfigVO vo = new CustomerConfigVO();
        vo.setEnabled(isKefuEnabled());
        vo.setType(getConfigValueOrEmpty(CustomerConfigConstant.APP_KEFU_TYPE));
        vo.setPhone(getConfigValueOrEmpty(CustomerConfigConstant.APP_KEFU_PHONE));
        vo.setWeworkUrl(getConfigValueOrEmpty(CustomerConfigConstant.APP_KEFU_WEWORK_URL));
        vo.setWeworkAppId(getConfigValueOrEmpty(CustomerConfigConstant.APP_KEFU_WEWORK_APPID));
        vo.setImEnabled(isImEnabled());
        vo.setImWelcome(getConfigValueOrEmpty(CustomerConfigConstant.APP_KEFU_IM_WELCOME));
        vo.setWechatId(getConfigValueOrEmpty(CustomerConfigConstant.APP_KEFU_WECHAT_ID));
        vo.setWechatQrUrl(getConfigValueOrEmpty(CustomerConfigConstant.APP_KEFU_WECHAT_QR_URL));
        return vo;
    }

    private boolean isImEnabled() {
        String value = customerConfigValueReader.getValueOrDefault(CustomerConfigConstant.APP_KEFU_IM_ENABLED);
        return "1".equals(value != null ? value.trim() : "");
    }

    private boolean isKefuEnabled() {
        String value = customerConfigValueReader.getValueOrDefault(CustomerConfigConstant.APP_STARTUP_KEFU_ENABLED);
        if (value == null || value.trim().isEmpty()) {
            return true;
        }
        return "1".equals(value.trim());
    }

    private String getConfigValueOrEmpty(String paramKey) {
        String value = customerConfigValueReader.getValueOrDefault(paramKey);
        return value != null ? value : "";
    }
}
