package com.sqx.app.base.customerConfig.service.impl;

import com.sqx.app.base.appConfig.domain.vo.SysConfigVO;
import com.sqx.app.base.customerConfig.domain.vo.CustomerConfigVO;
import com.sqx.app.base.customerConfig.support.CustomerConfigValueReader;
import com.sqx.app.base.enums.AppError;
import com.sqx.common.core.exception.BizException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

/**
 * 客服配置 API 单元测试
 */
@ExtendWith(MockitoExtension.class)
class CustomerConfigServiceImplTest {

    @Mock
    private CustomerConfigValueReader customerConfigValueReader;

    @InjectMocks
    private CustomerConfigServiceImpl customerConfigService;

    @Test
    void getByLegacyTypeId_whenUnknownId_shouldThrow() {
        BizException ex = assertThrows(BizException.class, () -> customerConfigService.getByLegacyTypeId(999L));
        assertEquals(AppError.INFO_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void getByLegacyTypeId_whenKefuPhone_shouldReturnValue() {
        when(customerConfigValueReader.getValueOrDefault("app_kefu_phone")).thenReturn("400-123-4567");

        SysConfigVO vo = customerConfigService.getByLegacyTypeId(329L);

        assertEquals("400-123-4567", vo.getValue());
    }

    @Test
    void getByLegacyTypeId_whenConfigMissing_shouldReturnEmptyString() {
        when(customerConfigValueReader.getValueOrDefault("app_kefu_wework_url")).thenReturn(null);

        SysConfigVO vo = customerConfigService.getByLegacyTypeId(274L);

        assertEquals("", vo.getValue());
    }

    @Test
    void getCustomerConfig_shouldReturnStructuredJson() {
        when(customerConfigValueReader.getValueOrDefault("app_startup_kefu_enabled")).thenReturn("1");
        when(customerConfigValueReader.getValueOrDefault("app_kefu_type")).thenReturn("wework");
        when(customerConfigValueReader.getValueOrDefault("app_kefu_phone")).thenReturn("400-123-4567");
        when(customerConfigValueReader.getValueOrDefault("app_kefu_wework_url")).thenReturn("https://work.weixin.qq.com/kfid/xxx");
        when(customerConfigValueReader.getValueOrDefault("app_kefu_wework_appid")).thenReturn("wxcorp123");

        CustomerConfigVO vo = customerConfigService.getCustomerConfig();

        assertTrue(vo.getEnabled());
        assertEquals("wework", vo.getType());
        assertEquals("400-123-4567", vo.getPhone());
        assertEquals("https://work.weixin.qq.com/kfid/xxx", vo.getWeworkUrl());
        assertEquals("wxcorp123", vo.getWeworkAppId());
    }

    @Test
    void getCustomerConfig_whenKefuDisabled_shouldReturnEnabledFalse() {
        when(customerConfigValueReader.getValueOrDefault("app_startup_kefu_enabled")).thenReturn("0");
        when(customerConfigValueReader.getValueOrDefault("app_kefu_type")).thenReturn("");
        when(customerConfigValueReader.getValueOrDefault("app_kefu_phone")).thenReturn("");
        when(customerConfigValueReader.getValueOrDefault("app_kefu_wework_url")).thenReturn("");
        when(customerConfigValueReader.getValueOrDefault("app_kefu_wework_appid")).thenReturn("");

        CustomerConfigVO vo = customerConfigService.getCustomerConfig();

        assertFalse(vo.getEnabled());
    }
}
