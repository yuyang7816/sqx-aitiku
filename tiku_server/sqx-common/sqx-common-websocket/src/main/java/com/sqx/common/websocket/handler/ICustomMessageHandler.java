package com.sqx.common.websocket.handler;

import com.sqx.common.websocket.domain.MessageDto;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

/**
 * @author chengming
 * @since 2025/6/4
 */
public interface ICustomMessageHandler {

    void sendMessage(MessageDto messageDto);
}
