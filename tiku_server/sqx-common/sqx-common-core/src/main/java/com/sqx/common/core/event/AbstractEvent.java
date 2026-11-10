package com.sqx.common.core.event;

import org.springframework.context.ApplicationEvent;

/**
 * @author chengming
 * @since 2024/9/26
 */
public abstract class AbstractEvent extends ApplicationEvent {

    public AbstractEvent(Object source) {
        super(source);
    }

    /**
     * 事件执行方法
     */
    public abstract void execute() throws InterruptedException;
}
