package com.sqx.common.websocket.interceptor;

import com.sqx.common.core.exception.SqxException;
import com.sqx.common.core.utils.SpringContextHolder;
import com.sqx.common.shiro.utils.JwtUtil;
import com.sqx.common.websocket.constants.WebSocketConstants;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Objects;

/**
 * @author chengming
 * @since 2024/12/31
 */
@Slf4j
public class WebSocketInterceptor implements HandshakeInterceptor {

    /**
     * WebSocket握手之前执行的前置处理方法
     *
     * @param request    WebSocket握手请求
     * @param response   WebSocket握手响应
     * @param wsHandler  WebSocket处理程序
     * @param attributes 与WebSocket会话关联的属性
     * @return 如果允许握手继续进行，则返回true；否则返回false
     */
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        String subject = checkLogin();
        attributes.put(WebSocketConstants.LOGIN_KEY, subject);
        return true;
    }

    private String checkLogin() {
        JwtUtil jwtUtil = SpringContextHolder.getBean(JwtUtil.class);
        HttpServletRequest httpServletRequest = getRequest();
        String token = jwtUtil.resolveToken(httpServletRequest);

        if (StringUtils.isBlank(token)) {
            log.error("[websocket] - token为空, 拒绝连接");
            throw new SqxException("暂未登录或token已经过期", HttpStatus.UNAUTHORIZED.value());
        }

        Claims claims = jwtUtil.getClaimByToken(token);
        if (claims == null || jwtUtil.isTokenExpired(claims.getExpiration())) {
            log.error("[websocket] - token已失效, 请重新登录");
            throw new SqxException("暂未登录或token已经过期", HttpStatus.UNAUTHORIZED.value());
        }

        Long subject = claims.get("subject", Long.class);
        return String.valueOf(subject);
    }

    /**
     * WebSocket握手成功后执行的后置处理方法
     *
     * @param request   WebSocket握手请求
     * @param response  WebSocket握手响应
     * @param wsHandler WebSocket处理程序
     * @param exception 握手过程中可能出现的异常
     */
    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {
        log.info("[websocket] - [握手完成]");
    }

    private HttpServletRequest getRequest() {
        return Objects.requireNonNull(getRequestAttributes()).getRequest();
    }

    private ServletRequestAttributes getRequestAttributes() {
        try {
            RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
            return (ServletRequestAttributes) attributes;
        } catch (Exception e) {
            return null;
        }
    }
}
