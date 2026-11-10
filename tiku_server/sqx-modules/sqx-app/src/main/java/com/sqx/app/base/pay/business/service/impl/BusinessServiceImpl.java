package com.sqx.app.base.pay.business.service.impl;

import com.sqx.app.base.pay.business.service.BusinessService;
import com.sqx.app.base.pay.domain.BusinessParam;
import com.sqx.app.base.pay.enums.BusinessType;
import com.sqx.app.vip.pay.db.service.IVipOrderService;
import com.sqx.app.vip.pay.domain.entity.VipOrder;
import com.sqx.app.vip.pay.enums.PayError;
import com.sqx.app.vip.plan.db.service.IVipPlanService;
import com.sqx.app.vip.plan.domain.entity.VipPlan;
import com.sqx.app.vip.plan.service.impl.VipPlanServiceImpl;
import com.sqx.common.core.exception.BizException;
import com.sqx.common.core.utils.GenUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;
import java.util.Objects;

/**
 * 统一支付业务预处理（创建待支付 VIP 订单）
 */
@Slf4j
@Service
public class BusinessServiceImpl implements BusinessService {

    private static final int STATUS_ENABLED = 1;

    @Resource
    private IVipPlanService iVipPlanService;
    @Resource
    private IVipOrderService iVipOrderService;

    @Override
    public void process(BusinessParam param) {
        BusinessType businessType = BusinessType.fromCode(param.getBusinessType());
        if (!Objects.equals(businessType, BusinessType.VIP)) {
            return;
        }

        Map<String, Object> extensions = param.getExtensions();
        String planName = extensions != null ? String.valueOf(extensions.getOrDefault("planName", "")) : "";
        if (StringUtils.isBlank(planName)) {
            throw new BizException(PayError.PLAN_NOT_FOUND);
        }

        VipPlan plan = iVipPlanService.lambdaQuery()
                .eq(VipPlan::getName, planName)
                .eq(VipPlan::getStatus, STATUS_ENABLED)
                .last("LIMIT 1")
                .one();
        if (plan == null) {
            throw new BizException(PayError.PLAN_NOT_FOUND);
        }

        String orderNo = StringUtils.isBlank(param.getOrderNo()) ? GenUtil.genOrderNo() : param.getOrderNo();
        param.setOrderNo(orderNo);
        param.setAmount(plan.getAmount());
        param.setUserId(param.getUserId());

        VipOrder order = new VipOrder();
        order.setOrderNo(orderNo);
        order.setUserId(param.getUserId());
        order.setPlanName(plan.getName());
        order.setPlanDays(VipPlanServiceImpl.resolveDays(plan.getMonth()));
        order.setAmount(plan.getAmount());
        order.setPayChannel(StringUtils.defaultIfBlank(param.getChannel(), "wechat"));
        order.setPayStatus(VipOrder.STATUS_PENDING);
        iVipOrderService.save(order);

        log.info("VIP 待支付订单已创建 orderNo={} planName={}", orderNo, plan.getName());
    }
}
