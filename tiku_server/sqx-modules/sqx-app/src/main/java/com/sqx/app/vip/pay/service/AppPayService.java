package com.sqx.app.vip.pay.service;

import com.sqx.app.vip.pay.domain.bo.AppPayCreateBO;
import com.sqx.app.vip.pay.domain.bo.AppVipOrderQueryBO;
import com.sqx.app.vip.pay.domain.vo.AppPayCreateVO;
import com.sqx.app.vip.pay.domain.vo.AppVipOrderItemVO;

import java.util.List;

/**
 * 用户端会员支付
 */
public interface AppPayService {

    /**
     * 创建会员支付订单并返回微信 prepay 参数
     *
     * @param userId 当前登录用户
     * @param create 套餐名称 + 支付方式
     */
    AppPayCreateVO createOrder(Long userId, AppPayCreateBO create);

    /**
     * 当前用户会员订单列表（按创建时间倒序）
     *
     * @param userId 当前登录用户
     * @param query  业务筛选项：orderNo / planName / payStatus
     */
    List<AppVipOrderItemVO> listMyOrders(Long userId, AppVipOrderQueryBO query);
}
