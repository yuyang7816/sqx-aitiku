package com.sqx.app.vip.pay.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sqx.app.base.enums.AppError;
import com.sqx.app.base.user.db.service.IUserService;
import com.sqx.app.base.user.domain.entity.UserEntity;
import com.sqx.app.vip.pay.db.service.IVipOrderService;
import com.sqx.app.vip.pay.domain.bo.AppPayCreateBO;
import com.sqx.app.vip.pay.domain.bo.AppVipOrderQueryBO;
import com.sqx.app.vip.pay.domain.entity.VipOrder;
import com.sqx.app.vip.pay.domain.vo.AppPayCreateVO;
import com.sqx.app.vip.pay.domain.vo.AppVipOrderItemVO;
import com.sqx.app.vip.pay.enums.PayError;
import com.sqx.app.vip.pay.service.AppPayService;
import com.sqx.app.base.auth.support.MpLoginConfigSupport;
import com.sqx.app.vip.pay.support.AppPayWxConfigSupport;
import com.sqx.app.vip.plan.db.service.IVipPlanService;
import com.sqx.app.vip.plan.domain.entity.VipPlan;
import com.sqx.app.vip.plan.service.impl.VipPlanServiceImpl;
import com.sqx.common.core.exception.BizException;
import com.sqx.common.core.utils.GenUtil;
import com.sqx.common.pay.domain.UnifiedPayRequest;
import com.sqx.common.pay.enums.PayChannel;
import com.sqx.common.pay.enums.PayTradeType;
import com.sqx.common.pay.service.IPayService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户端会员支付下单
 */
@Slf4j
@Service
public class AppPayServiceImpl implements AppPayService {

    private static final String PAY_METHOD_WECHAT = "wechat";
    private static final String PAY_METHOD_ALIPAY = "alipay";
    /** 启用套餐 */
    private static final int STATUS_ENABLED = 1;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Resource
    private IVipPlanService iVipPlanService;
    @Resource
    private IVipOrderService iVipOrderService;
    @Resource
    private IUserService iUserService;
    @Resource
    private IPayService iPayService;
    @Resource
    private AppPayWxConfigSupport appPayWxConfigSupport;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public AppPayCreateVO createOrder(Long userId, AppPayCreateBO create) {
        UserEntity user = iUserService.getById(userId);
        if (user == null) {
            throw new BizException(AppError.USER_NOT_FOUND);
        }

        String payMethod = normalizePayMethod(create.getPayMethod());
        String clientPlatform = normalizeClientPlatform(create.getClientPlatform());
        if (PAY_METHOD_WECHAT.equals(payMethod)
                && "h5-wechat".equals(clientPlatform)
                && !MpLoginConfigSupport.isEnabled()) {
            throw new BizException(AppError.WECHAT_MP_LOGIN_DISABLED);
        }
        VipPlan plan = findEnabledPlan(create.getPlanName());
        int planDays = VipPlanServiceImpl.resolveDays(plan.getMonth());
        BigDecimal amount = plan.getAmount();

        String orderNo = GenUtil.genOrderNo();
        VipOrder order = new VipOrder();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setPlanName(plan.getName());
        order.setPlanDays(planDays);
        order.setAmount(amount);
        order.setPayChannel(payMethod);
        order.setPayStatus(VipOrder.STATUS_PENDING);
        iVipOrderService.save(order);

        String prepayJson = invokeUnifiedPay(user, order, payMethod, clientPlatform);
        Map<String, String> prepay = parsePrepayParams(prepayJson);

        AppPayCreateVO vo = new AppPayCreateVO();
        vo.setOrderNo(orderNo);
        vo.setPlanName(plan.getName());
        vo.setPlanDays(planDays);
        vo.setAmount(amount);
        vo.setPayMethod(payMethod);
        vo.setPrepay(prepay);
        return vo;
    }

    @Override
    public List<AppVipOrderItemVO> listMyOrders(Long userId, AppVipOrderQueryBO query) {
        if (userId == null) {
            return Collections.emptyList();
        }
        AppVipOrderQueryBO safeQuery = query == null ? new AppVipOrderQueryBO() : query;
        LambdaQueryWrapper<VipOrder> wrapper = new LambdaQueryWrapper<VipOrder>()
                .eq(VipOrder::getUserId, userId);
        if (StringUtils.isNotBlank(safeQuery.getOrderNo())) {
            wrapper.like(VipOrder::getOrderNo, safeQuery.getOrderNo().trim());
        }
        if (StringUtils.isNotBlank(safeQuery.getPlanName())) {
            wrapper.eq(VipOrder::getPlanName, safeQuery.getPlanName().trim());
        }
        if (safeQuery.getPayStatus() != null) {
            wrapper.eq(VipOrder::getPayStatus, safeQuery.getPayStatus());
        }
        wrapper.orderByDesc(VipOrder::getCreateTime);
        return iVipOrderService.list(wrapper).stream()
                .map(this::toOrderItemVO)
                .collect(Collectors.toList());
    }

    private AppVipOrderItemVO toOrderItemVO(VipOrder order) {
        AppVipOrderItemVO vo = new AppVipOrderItemVO();
        vo.setOrderNo(order.getOrderNo());
        vo.setPlanName(order.getPlanName());
        vo.setPlanDays(order.getPlanDays());
        vo.setAmount(order.getAmount());
        vo.setPayChannel(order.getPayChannel());
        vo.setPayStatus(order.getPayStatus());
        vo.setCreateTime(order.getCreateTime());
        vo.setPayTime(order.getPayTime());
        return vo;
    }

    private VipPlan findEnabledPlan(String planName) {
        VipPlan plan = iVipPlanService.getOne(new LambdaQueryWrapper<VipPlan>()
                .eq(VipPlan::getName, planName)
                .eq(VipPlan::getStatus, STATUS_ENABLED)
                .last("LIMIT 1"));
        if (plan == null) {
            throw new BizException(PayError.PLAN_NOT_FOUND);
        }
        return plan;
    }

    private String normalizePayMethod(String payMethod) {
        if (PAY_METHOD_WECHAT.equalsIgnoreCase(payMethod)) {
            return PAY_METHOD_WECHAT;
        }
        if (PAY_METHOD_ALIPAY.equalsIgnoreCase(payMethod)) {
            return PAY_METHOD_ALIPAY;
        }
        throw new BizException(PayError.PAY_METHOD_UNSUPPORTED);
    }

    private String normalizeClientPlatform(String clientPlatform) {
        String normalized = StringUtils.defaultIfBlank(clientPlatform,
                StringUtils.defaultIfBlank(com.sqx.app.base.utils.RequestUtil.getHeader("clientPlatform"), "mp-weixin"));
        return normalized.trim().toLowerCase();
    }

    private String invokeUnifiedPay(UserEntity user, VipOrder order, String payMethod, String clientPlatform) {
        try {
            UnifiedPayRequest request = buildPayRequest(user, order, payMethod, clientPlatform);
            return iPayService.unifiedPay(request);
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            log.error("创建支付订单失败 orderNo={}", order.getOrderNo(), e);
            throw new BizException(PayError.PAY_CREATE_FAILED);
        }
    }

    private UnifiedPayRequest buildPayRequest(UserEntity user, VipOrder order, String payMethod, String clientPlatform) {
        UnifiedPayRequest.UnifiedPayRequestBuilder builder = UnifiedPayRequest.builder()
                .orderNo(order.getOrderNo())
                .amount(order.getAmount())
                .subject("会员-" + order.getPlanName())
                .clientIp(appPayWxConfigSupport.resolveClientIp());

        if (PAY_METHOD_WECHAT.equals(payMethod)) {
            builder.channel(PayChannel.WX.getValue())
                    .notifyUrl(appPayWxConfigSupport.buildNotifyUrl(PayChannel.WX.getValue()));
            applyWechatPayContext(builder, user, clientPlatform);
        } else {
            builder.channel(PayChannel.ALI.getValue())
                    .notifyUrl(appPayWxConfigSupport.buildNotifyUrl(PayChannel.ALI.getValue()));
            applyAlipayContext(builder, clientPlatform);
        }

        return builder.build();
    }

    private void applyWechatPayContext(UnifiedPayRequest.UnifiedPayRequestBuilder builder, UserEntity user, String clientPlatform) {
        switch (clientPlatform) {
            case "h5-wechat":
                builder.tradeType(PayTradeType.JSAPI.getCode())
                        .appId(appPayWxConfigSupport.getMpAppId())
                        .openId(requireMpOpenId(user));
                break;
            case "app-plus":
                builder.tradeType(PayTradeType.APP.getCode())
                        .appId(appPayWxConfigSupport.getAppAppId());
                break;
            case "h5":
                builder.tradeType(PayTradeType.H5.getCode())
                        .appId(appPayWxConfigSupport.getMpAppId());
                break;
            case "mp-weixin":
            default:
                builder.tradeType(PayTradeType.JSAPI.getCode())
                        .appId(appPayWxConfigSupport.getMaAppId())
                        .openId(requireMaOpenId(user));
                break;
        }
    }

    private void applyAlipayContext(UnifiedPayRequest.UnifiedPayRequestBuilder builder, String clientPlatform) {
        if ("app-plus".equals(clientPlatform)) {
            builder.tradeType(PayTradeType.APP.getCode());
            return;
        }
        builder.tradeType(PayTradeType.H5.getCode());
    }

    private String requireMaOpenId(UserEntity user) {
        if (StringUtils.isNotBlank(user.getMaOpenId())) {
            return user.getMaOpenId();
        }
        throw new BizException(PayError.WECHAT_OPEN_ID_MISSING);
    }

    private String requireMpOpenId(UserEntity user) {
        if (StringUtils.isNotBlank(user.getMpOpenId())) {
            return user.getMpOpenId();
        }
        if (StringUtils.isNotBlank(user.getMaOpenId())) {
            return user.getMaOpenId();
        }
        throw new BizException(PayError.WECHAT_OPEN_ID_MISSING);
    }

    private Map<String, String> parsePrepayParams(String prepayRaw) {
        if (StringUtils.isBlank(prepayRaw)) {
            return new HashMap<>();
        }
        String trimmed = prepayRaw.trim();
        // 支付宝 WAP/APP 返回 HTML form 或 orderInfo 字符串，不是 JSON
        if (!trimmed.startsWith("{")) {
            Map<String, String> prepay = new HashMap<>();
            prepay.put("orderInfo", trimmed);
            return prepay;
        }
        try {
            Map<String, Object> raw = OBJECT_MAPPER.readValue(trimmed, new TypeReference<Map<String, Object>>() {
            });
            Map<String, String> prepay = new HashMap<>();
            for (Map.Entry<String, Object> entry : raw.entrySet()) {
                if (entry.getValue() != null) {
                    prepay.put(entry.getKey(), String.valueOf(entry.getValue()));
                }
            }
            // 兼容 uni.requestPayment 的 package 字段名
            if (prepay.containsKey("packageValue") && !prepay.containsKey("package")) {
                prepay.put("package", prepay.get("packageValue"));
            }
            if (prepay.containsKey("mwebUrl") && !prepay.containsKey("mweb_url")) {
                prepay.put("mweb_url", prepay.get("mwebUrl"));
            }
            return prepay;
        } catch (Exception e) {
            log.warn("解析 prepay JSON 失败，按 orderInfo 回传: {}", prepayRaw, e);
            Map<String, String> prepay = new HashMap<>();
            prepay.put("orderInfo", trimmed);
            return prepay;
        }
    }
}
