package com.sqx.app.vip.pay.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.vip.pay.domain.bo.AdminVipOrderInfoBo;
import com.sqx.app.vip.pay.domain.bo.AdminVipOrderPageBo;
import com.sqx.app.vip.pay.domain.vo.AdminVipOrderVo;

public interface AdminVipOrderService {

    IPage<AdminVipOrderVo> pageInfo(AdminVipOrderPageBo pageBo);

    AdminVipOrderVo info(AdminVipOrderInfoBo infoBo);
}
