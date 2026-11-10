package com.sqx.common.pay.factory;

import com.sqx.common.core.exception.BizException;
import com.sqx.common.pay.notify.IPayNotifyHandler;
import com.sqx.common.pay.operation.IPayOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author chengming
 * @since 2026/1/9
 */
@Slf4j
@Component
public class PayOperationFactory {

    @Resource
    private List<IPayOperation> payOperations;
    @Resource
    private List<IPayNotifyHandler> payNotifyHandlers;

    /**
     * 获取支付平台
     *
     * @param channel 支付渠道
     */
    public IPayOperation getPayOperation(String channel) {
        for (IPayOperation payOperation : payOperations) {
            if (payOperation.support(channel)) {
                return payOperation;
            }
        }

        log.error("不支持的支付渠道: {}", channel);
        throw new BizException("不支持的支付渠道");
    }

    public IPayNotifyHandler getPayNotifyHandler(String channel) {
        for (IPayNotifyHandler payNotifyHandler : payNotifyHandlers) {
            if (payNotifyHandler.support(channel)) {
                return payNotifyHandler;
            }
        }

        log.error("不支持的支付回调渠道: {}", channel);
        throw new BizException("不支持的支付回调渠道");
    }
}
