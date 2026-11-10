package com.sqx.app.base.verifyCode.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sqx.app.base.enums.AppError;
import com.sqx.app.base.template.service.SmsTemplateService;
import com.sqx.app.base.verifyCode.db.service.IVerifyCodeService;
import com.sqx.app.base.verifyCode.domain.entity.VerifyCode;
import com.sqx.common.core.exception.BizException;
import com.sqx.common.mail.service.IMailService;
import com.sqx.common.sms.SmsService;
import com.sqx.common.sms.enums.SmsError;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VerifyCodeServiceImplTest {

    private static final String PHONE = "13800138000";

    @Mock
    private IVerifyCodeService iVerifyCodeService;
    @Mock
    private IMailService mailService;
    @Mock
    private SmsService smsService;
    @Mock
    private SmsTemplateService smsTemplateService;

    @InjectMocks
    private VerifyCodeServiceImpl verifyCodeService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(verifyCodeService, "smsMock", true);
        ReflectionTestUtils.setField(verifyCodeService, "smsMockCode", "123456");
    }

    @Test
    void sendAppVerifyCode_whenSentWithin60Seconds_shouldReject() {
        VerifyCode recent = new VerifyCode();
        recent.setPhone(PHONE);
        recent.setCreateTime(LocalDateTime.now().minusSeconds(30));
        when(iVerifyCodeService.getOne(any(LambdaQueryWrapper.class))).thenReturn(recent);

        BizException ex = assertThrows(BizException.class, () -> verifyCodeService.sendAppVerifyCode(PHONE));
        assertEquals(AppError.VERIFY_CODE_SEND_TOO_FREQUENT, ex.getErrorCode());
        verify(iVerifyCodeService, never()).save(any(VerifyCode.class));
    }

    @Test
    void sendProductionSms_whenGatewayFails_shouldThrowVerifyCodeSendFailed() {
        doThrow(new BizException(SmsError.SMS_SEND_ERROR))
                .when(smsService).sendSms(eq(PHONE), anyString());

        BizException ex = assertThrows(BizException.class, () ->
                ReflectionTestUtils.invokeMethod(verifyCodeService, "sendProductionSms", PHONE, "test content"));
        assertEquals(AppError.VERIFY_CODE_SEND_FAILED, ex.getErrorCode());
    }

    @Test
    void isSmsGatewayError_shouldDetectSmsProviderErrors() {
        Boolean smsError = ReflectionTestUtils.invokeMethod(verifyCodeService, "isSmsGatewayError",
                new BizException(SmsError.SMS_SEND_ERROR));
        Boolean verifyError = ReflectionTestUtils.invokeMethod(verifyCodeService, "isSmsGatewayError",
                new BizException(AppError.VERIFY_CODE_ERROR));
        assertTrue(smsError);
        assertFalse(verifyError);
    }

    @Test
    void createAppRandomCode_shouldBeFourToSixDigits() {
        for (int i = 0; i < 20; i++) {
            String code = ReflectionTestUtils.invokeMethod(verifyCodeService, "createAppRandomCode");
            assertTrue(code.matches("\\d{4,6}"), "code=" + code);
        }
    }
}