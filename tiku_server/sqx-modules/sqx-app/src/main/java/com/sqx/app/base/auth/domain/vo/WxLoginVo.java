package com.sqx.app.base.auth.domain.vo;

import com.sqx.app.base.user.domain.entity.UserEntity;
import lombok.Data;

/**
 * @author chengming
 * @since 2025/8/5
 */
@Data
public class WxLoginVo {

    /**
     * 会话密钥
     */
    private String sessionKey;

    /**
     * 用户在开放平台的唯一标识符，若当前小程序已绑定到微信开放平台帐号下会返回
     */
    private String unionid;

    /**
     * 错误信息，请求失败时返回
     */
    private String errmsg;

    /**
     * 用户唯一标识
     */
    private String openid;

    /**
     * 错误码，请求失败时返回
     */
    private Integer errcode;

    /**
     * 是否绑定手机号
     */
    private Boolean bindPhone;

    /**
     * 用户信息
     */
    private UserEntity userEntity;

    private LoginVO loginVO;
}
