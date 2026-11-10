package com.sqx.app.base.auth.auth.interceptor;


import com.sqx.app.base.auth.auth.annotation.Login;
import com.sqx.app.base.auth.auth.constant.AppAuthConstants;
import com.sqx.common.core.exception.SqxException;
import com.sqx.common.shiro.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * APP 端 JWT 鉴权拦截器（/app/** 需登录接口）
 * <p>
 * Token 请求头格式（推荐）：{@code Authorization: Bearer {token}}
 * 兼容 legacy 头：{@code token: {token}}（配置项 sqx.jwt.header）
 */
@Component
public class AuthorizationInterceptor extends HandlerInterceptorAdapter {
    @Autowired
    private JwtUtil jwtUtil;

    public static final String USER_KEY = "userId";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Login annotation;
        if(handler instanceof HandlerMethod) {
            annotation = ((HandlerMethod) handler).getMethodAnnotation(Login.class);
        }else{
            return true;
        }

        if(annotation == null){
            return true;
        }

        String token = jwtUtil.resolveToken(request);

        if(StringUtils.isBlank(token)){
            throwUnauthorized();
        }

        Claims claims = jwtUtil.getClaimByToken(token);
        if(claims == null || jwtUtil.isTokenExpired(claims.getExpiration())){
            throwUnauthorized();
        }

        String subject = claims.getSubject();
        request.setAttribute(USER_KEY, Long.valueOf(subject));

        return true;
    }

    private void throwUnauthorized() {
        throw new SqxException(AppAuthConstants.UNAUTHORIZED_MSG, AppAuthConstants.UNAUTHORIZED_CODE);
    }
}
