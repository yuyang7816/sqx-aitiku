package com.sqx.app.base.utils.wx;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;

/**
 * 微信公众号 OAuth2 网页授权 access_token 响应
 */
@Data
public class WxMpOAuthRes {

    @JSONField(name = "access_token")
    private String accessToken;

    @JSONField(name = "expires_in")
    private Integer expiresIn;

    @JSONField(name = "refresh_token")
    private String refreshToken;

    private String openid;

    private String scope;

    private String unionid;

    private Integer errcode;

    private String errmsg;
}
