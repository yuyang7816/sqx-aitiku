package com.sqx.app.base.pay.business.service;

import com.sqx.app.base.pay.domain.BusinessParam;

/**
 * @author chengming
 * @since 2025/4/21
 */
public interface BusinessService {

    /**
     * 支付业务处理
     * @param payParam 支付参数
     */
    void process(BusinessParam payParam);
}
