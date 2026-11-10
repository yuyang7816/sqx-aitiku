package com.sqx.app.vip.pay.service.impl;

import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.sqx.app.base.enums.AppError;
import com.sqx.app.base.user.db.service.IUserService;
import com.sqx.app.base.user.domain.entity.UserEntity;
import com.sqx.app.vip.pay.db.service.IVipOrderService;
import com.sqx.app.vip.pay.domain.bo.AppPayCreateBO;
import com.sqx.app.vip.pay.domain.entity.VipOrder;
import com.sqx.app.vip.pay.domain.vo.AppPayCreateVO;
import com.sqx.app.vip.pay.enums.PayError;
import com.sqx.app.vip.pay.support.AppPayWxConfigSupport;
import com.sqx.app.vip.plan.db.service.IVipPlanService;
import com.sqx.app.vip.plan.domain.entity.VipPlan;
import com.sqx.common.core.exception.BizException;
import com.sqx.common.pay.service.IPayService;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * POST /app/pay 单元测试
 */
@ExtendWith(MockitoExtension.class)
class AppPayServiceImplTest {

    @Mock
    private IVipPlanService iVipPlanService;
    @Mock
    private IVipOrderService iVipOrderService;
    @Mock
    private IUserService iUserService;
    @Mock
    private IPayService iPayService;
    @Mock
    private AppPayWxConfigSupport appPayWxConfigSupport;

    @InjectMocks
    private AppPayServiceImpl appPayService;

    @BeforeEach
    void initEntityCache() {
        Configuration configuration = new Configuration();
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(configuration, "test");
        TableInfoHelper.initTableInfo(assistant, VipOrder.class);
        TableInfoHelper.initTableInfo(assistant, UserEntity.class);
    }

    @Test
    void createOrder_whenUserNotFound_shouldThrow() {
        when(iUserService.getById(1L)).thenReturn(null);

        AppPayCreateBO create = buildCreateBO();
        BizException ex = assertThrows(BizException.class, () -> appPayService.createOrder(1L, create));
        assertEquals(AppError.USER_NOT_FOUND.getCode(), ex.getErrorCode().getCode());
    }

    @Test
    void createOrder_whenPlanNotFound_shouldThrow() {
        when(iUserService.getById(1L)).thenReturn(buildUser());
        when(iVipPlanService.getOne(any())).thenReturn(null);

        BizException ex = assertThrows(BizException.class, () -> appPayService.createOrder(1L, buildCreateBO()));
        assertEquals(PayError.PLAN_NOT_FOUND.getCode(), ex.getErrorCode().getCode());
    }

    @Test
    void createOrder_whenWechatWithoutOpenId_shouldThrow() {
        when(iUserService.getById(1L)).thenReturn(buildUserWithoutOpenId());
        when(iVipPlanService.getOne(any())).thenReturn(buildPlan());
        when(iVipOrderService.save(any())).thenReturn(true);

        BizException ex = assertThrows(BizException.class, () -> appPayService.createOrder(1L, buildCreateBO()));
        assertEquals(PayError.WECHAT_OPEN_ID_MISSING.getCode(), ex.getErrorCode().getCode());
    }

    @Test
    void createOrder_whenWechatSuccess_shouldReturnPrepay() throws Exception {
        when(iUserService.getById(1L)).thenReturn(buildUser());
        when(iVipPlanService.getOne(any())).thenReturn(buildPlan());
        when(iVipOrderService.save(any())).thenReturn(true);
        when(appPayWxConfigSupport.resolveClientIp()).thenReturn("127.0.0.1");
        when(appPayWxConfigSupport.getMaAppId()).thenReturn("wx123");
        when(appPayWxConfigSupport.buildNotifyUrl("wx")).thenReturn("https://example.com/app/base/pay/notify/wx");
        when(iPayService.unifiedPay(any())).thenReturn(
                "{\"appId\":\"wx123\",\"timeStamp\":\"123\",\"nonceStr\":\"abc\",\"packageValue\":\"prepay_id=wx\",\"signType\":\"MD5\",\"paySign\":\"sign\"}"
        );

        AppPayCreateVO result = appPayService.createOrder(1L, buildCreateBO());

        assertEquals("月卡", result.getPlanName());
        assertEquals(31, result.getPlanDays());
        assertEquals(new BigDecimal("30.00"), result.getAmount());
        assertNotNull(result.getOrderNo());
        assertNotNull(result.getPrepay());
        assertEquals("prepay_id=wx", result.getPrepay().get("package"));

        ArgumentCaptor<VipOrder> captor = ArgumentCaptor.forClass(VipOrder.class);
        verify(iVipOrderService).save(captor.capture());
        assertEquals(VipOrder.STATUS_PENDING, captor.getValue().getPayStatus());
        assertEquals("wechat", captor.getValue().getPayChannel());
    }

    @Test
    void createOrder_whenUnsupportedPayMethod_shouldThrow() {
        when(iUserService.getById(1L)).thenReturn(buildUser());

        AppPayCreateBO create = buildCreateBO();
        create.setPayMethod("unionpay");

        BizException ex = assertThrows(BizException.class, () -> appPayService.createOrder(1L, create));
        assertEquals(PayError.PAY_METHOD_UNSUPPORTED.getCode(), ex.getErrorCode().getCode());
    }

    @Test
    void createOrder_whenAlipayH5_shouldReturnOrderInfoPrepay() throws Exception {
        when(iUserService.getById(1L)).thenReturn(buildUser());
        when(iVipPlanService.getOne(any())).thenReturn(buildPlan());
        when(iVipOrderService.save(any())).thenReturn(true);
        when(appPayWxConfigSupport.resolveClientIp()).thenReturn("127.0.0.1");
        when(appPayWxConfigSupport.buildNotifyUrl("ali")).thenReturn("https://example.com/app/base/pay/notify/ali");
        String alipayForm = "<form name=\"punchout_form\" method=\"post\" action=\"https://openapi.alipay.com/gateway.do\">"
                + "<input type=\"hidden\" name=\"biz_content\" value=\"test\" /></form>";
        when(iPayService.unifiedPay(any())).thenReturn(alipayForm);

        AppPayCreateBO create = buildCreateBO();
        create.setPayMethod("alipay");
        create.setClientPlatform("h5");

        AppPayCreateVO result = appPayService.createOrder(1L, create);

        assertEquals("alipay", result.getPayMethod());
        assertNotNull(result.getPrepay());
        assertEquals(alipayForm, result.getPrepay().get("orderInfo"));
    }

    private AppPayCreateBO buildCreateBO() {
        AppPayCreateBO create = new AppPayCreateBO();
        create.setPlanName("月卡");
        create.setPayMethod("wechat");
        return create;
    }

    private UserEntity buildUser() {
        UserEntity user = new UserEntity();
        user.setUserId(1L);
        user.setMaOpenId("oTestOpenId");
        return user;
    }

    private UserEntity buildUserWithoutOpenId() {
        UserEntity user = new UserEntity();
        user.setUserId(1L);
        return user;
    }

    private VipPlan buildPlan() {
        VipPlan plan = new VipPlan();
        plan.setName("月卡");
        plan.setMonth(1);
        plan.setAmount(new BigDecimal("30.00"));
        plan.setStatus(1);
        return plan;
    }
}
