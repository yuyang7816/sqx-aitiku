package com.sqx.app.vip.pay.db.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sqx.app.vip.pay.db.mapper.VipOrderMapper;
import com.sqx.app.vip.pay.db.service.IVipOrderService;
import com.sqx.app.vip.pay.domain.entity.VipOrder;
import org.springframework.stereotype.Service;

/**
 * 会员支付订单 DB 服务
 */
@Service
public class IVipOrderServiceImpl extends ServiceImpl<VipOrderMapper, VipOrder> implements IVipOrderService {
}
