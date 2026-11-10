package com.sqx.app.base.customerConfig.service;

import com.sqx.app.base.appConfig.domain.vo.SysConfigVO;
import com.sqx.app.base.customerConfig.domain.vo.CustomerConfigVO;

/**
 * 客服配置服务
 */
public interface CustomerConfigService {

    /**
     * legacy：按 common/type ID 返回单项配置值
     */
    SysConfigVO getByLegacyTypeId(Long typeId);

    /**
     * 返回客服电话、在线客服链接等结构化配置
     */
    CustomerConfigVO getCustomerConfig();
}
