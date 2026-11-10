package com.sqx.app.base.auth.support;

import com.sqx.app.base.auth.domain.vo.LoginProfileStateVO;
import com.sqx.app.base.enums.AppError;
import com.sqx.app.base.user.domain.entity.UserEntity;
import com.sqx.common.core.exception.BizException;
import org.springframework.stereotype.Component;

/**
 * 文档 23.1 登录规则校验：协议必选、profile.ready 门禁、退出不清学习数据（服务端侧说明见 docs/login-rules-api.md）
 */
@Component
public class LoginRulesSupport {

    /**
     * 规则 23.1-1：必须勾选并同意用户协议与隐私政策后才能登录
     */
    public void validateAgreementAccepted(Boolean agreedProtocol) {
        if (!Boolean.TRUE.equals(agreedProtocol)) {
            throw new BizException(AppError.AGREEMENT_NOT_ACCEPTED);
        }
    }

    /**
     * 规则 23.1-2：profile.ready 由 tb_user.profile_ready 决定；未完善时 uniapp 路由守卫跳转计划页
     */
    public boolean isProfileReady(UserEntity userEntity) {
        return userEntity != null
                && userEntity.getProfileReady() != null
                && userEntity.getProfileReady() == 1;
    }

    /**
     * 构建登录响应中的 profile 门禁字段（短信/微信登录均返回）
     */
    public LoginProfileStateVO buildProfileState(UserEntity userEntity) {
        LoginProfileStateVO profile = new LoginProfileStateVO();
        profile.setReady(isProfileReady(userEntity));
        return profile;
    }
}
