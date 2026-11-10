package com.sqx.common.websocket.event;

import com.sqx.common.core.event.AbstractEvent;
import com.sqx.common.core.utils.SpringContextHolder;
import com.sqx.common.websocket.domain.MessageDto;
import com.sqx.common.websocket.handler.ICustomMessageHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * @author chengming
 * @since 2025/6/4
 */
@Slf4j
public class MessageEvent extends AbstractEvent {

    private final MessageDto messageDto;

    public MessageEvent(Object source, MessageDto messageDto) {
        super(source);
        this.messageDto = messageDto;
    }

    @Override
    public void execute() {
        ICustomMessageHandler customMessageHandler = SpringContextHolder.getBean(ICustomMessageHandler.class);

        customMessageHandler.sendMessage(messageDto);
    }
}
