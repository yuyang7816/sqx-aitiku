package com.sqx.common.core.event;

import lombok.SneakyThrows;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * @author chengming
 * @since 2024/9/26
 */
@Component
public class AbstractEventListener {

    /**
     * 异步执行方法
     *
     * @param event 事件
     */
    @EventListener(value = AbstractEvent.class)
    @SneakyThrows
    @Async("asyncServiceExecutor")
    public void onApplicationEvent(AbstractEvent event) {
        event.execute();
    }
}
