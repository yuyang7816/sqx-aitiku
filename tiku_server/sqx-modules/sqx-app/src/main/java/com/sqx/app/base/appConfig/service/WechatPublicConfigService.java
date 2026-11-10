package com.sqx.app.base.appConfig.service;

import com.sqx.app.base.appConfig.domain.vo.WechatPublicConfigVO;
import com.sqx.app.base.auth.support.MpLoginConfigSupport;
import com.sqx.app.base.constants.SysConfigConstant;
import com.sqx.app.base.utils.SysConfigUtil;
import com.sqx.app.base.enums.AppError;
import com.sqx.common.core.exception.BizException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * 用户端微信公开配置（仅读取 sys_auth 登录认证项，不含 secret）
 */
@Service
public class WechatPublicConfigService {

    public WechatPublicConfigVO getPublicConfig() {
        WechatPublicConfigVO vo = new WechatPublicConfigVO();
        vo.setMpAppId(readAuthConfig(SysConfigConstant.Sys.SYS_USER_MP_APPID));
        vo.setMaAppId(readAuthConfig(SysConfigConstant.Sys.SYS_USER_MA_APPID));
        vo.setMpLoginEnabled(MpLoginConfigSupport.isEnabled());
        return vo;
    }

    private String readAuthConfig(String paramKey) {
        try {
            String value = SysConfigUtil.getConfigValue(paramKey);
            return StringUtils.isNotBlank(value) ? value.trim() : "";
        } catch (BizException ex) {
            if (ex.getErrorCode() == AppError.CONFIG_NOT_FOUND) {
                return "";
            }
            throw ex;
        }
    }
}
