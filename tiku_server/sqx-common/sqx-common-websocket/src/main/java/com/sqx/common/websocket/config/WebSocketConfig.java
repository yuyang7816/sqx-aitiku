package com.sqx.common.websocket.config;

import com.sqx.common.websocket.handler.WebSocketHandler;
import com.sqx.common.websocket.interceptor.WebSocketInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.HandshakeInterceptor;

import javax.annotation.Resource;

/**
 * @author chengming
 * @since 2024/12/31
 */
@Slf4j
@Configuration
@EnableWebSocket
public class WebSocketConfig {

    public static final String WS_PATH = "ws";

    @Bean
    public WebSocketConfigurer webSocketConfigurer(WebSocketHandler webSocketHandler, HandshakeInterceptor handshakeInterceptor) {
        // 返回一个WebSocketConfigurer对象，用于配置WebSocket
        log.info("[websocket] - 注册到路径: {}", WS_PATH);
        return registry -> registry
                // 添加WebSocket处理程序和拦截器到指定路径，设置允许的跨域来源
                .addHandler(webSocketHandler, WS_PATH)
                .addInterceptors(handshakeInterceptor)
                .setAllowedOrigins("*");
    }

    @Bean
    public HandshakeInterceptor handshakeInterceptor() {
        return new WebSocketInterceptor();
    }

    @Bean
    public WebSocketHandler webSocketHandler() {
        return new WebSocketHandler();
    }

    //@Bean
    //public WebSocketTopicListener topicListener() {
    //    return new WebSocketTopicListener();
    //}
}