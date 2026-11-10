package com.sqx.common.websocket.handler;

import com.alibaba.fastjson2.JSONObject;
import com.sqx.common.core.utils.SpringContextHolder;
import com.sqx.common.websocket.constants.WebSocketConstants;
import com.sqx.common.websocket.domain.MessageDto;
import com.sqx.common.websocket.enums.MessageTypeEnum;
import com.sqx.common.websocket.event.MessageEvent;
import com.sqx.common.websocket.holder.WebSocketSessionHolder;
import com.sqx.common.websocket.utils.WebSocketUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import java.io.IOException;
import java.util.Objects;

/**
 * @author chengming
 * @since 2024/12/31
 */
@Slf4j
public class WebSocketHandler extends AbstractWebSocketHandler {

    /**
     * 连接成功后
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws IOException {
        String sessionKey = (String) session.getAttributes().get(WebSocketConstants.LOGIN_KEY);
        if (StringUtils.isBlank(sessionKey)) {
            session.close(CloseStatus.BAD_DATA);
            log.error("[websocket] - [失败] 错误的token. sessionId: {}", session.getId());
            return;
        }

        WebSocketSessionHolder.addSession(Long.valueOf(sessionKey), session);
        log.info("[websocket] - [成功] - sessionId: {}, userId:{}", session.getId(), sessionKey);
    }

    /**
     * 处理接收到的文本消息
     *
     * @param session WebSocket会话
     * @param message 接收到的文本消息
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        String payload = message.getPayload();
        MessageDto messageDto = JSONObject.parseObject(payload, MessageDto.class);
        if (Objects.equals(MessageTypeEnum.PING.code(), messageDto.getMessageType())) {
            String sessionKey = (String) session.getAttributes().get(WebSocketConstants.LOGIN_KEY);
            WebSocketSession webSocketSession = WebSocketSessionHolder.getSessions(Long.valueOf(sessionKey));
            WebSocketUtils.sendPongMessage(webSocketSession);
        } else {
            WebSocketSession sessions = WebSocketSessionHolder.getSessions(Long.valueOf(messageDto.getTargetId()));
            WebSocketUtils.sendMessage(sessions, messageDto.getContent());

            MessageEvent messageEvent = new MessageEvent(this, messageDto);
            SpringContextHolder.publishEvent(messageEvent);
        }
    }

    /**
     * 处理接收到的二进制消息
     *
     * @param session WebSocket会话
     * @param message 接收到的二进制消息
     * @throws Exception 处理消息过程中可能抛出的异常
     */
    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {
        super.handleBinaryMessage(session, message);
    }

    /**
     * 处理接收到的Pong消息（心跳监测）
     *
     * @param session WebSocket会话
     * @param message 接收到的Pong消息
     */
    @Override
    protected void handlePongMessage(WebSocketSession session, PongMessage message) {
        WebSocketUtils.sendPongMessage(session);
    }

    /**
     * 处理WebSocket传输错误
     *
     * @param session   WebSocket会话
     * @param exception 发生的异常
     */
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.error("[websocket] - [传输错误] - sessionId: {} , exception:{}", session.getId(), exception.getMessage());
    }

    /**
     * 在WebSocket连接关闭后执行清理操作
     *
     * @param session WebSocket会话
     * @param status  关闭状态信息
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String sessionKey = (String) session.getAttributes().get(WebSocketConstants.LOGIN_KEY);
        if (StringUtils.isBlank(sessionKey)) {
            log.info("[websocket] - [连接关闭] - [失败] - 错误的token - sessionId: {}", session.getId());
            return;
        }

        WebSocketSessionHolder.removeSession(Long.valueOf(sessionKey));
        log.info("[websocket] - [连接关闭] - [成功] - sessionId: {}, userId:{}", session.getId(), sessionKey);
    }

    /**
     * 指示处理程序是否支持接收部分消息
     *
     * @return 如果支持接收部分消息，则返回true；否则返回false
     */
    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
}
