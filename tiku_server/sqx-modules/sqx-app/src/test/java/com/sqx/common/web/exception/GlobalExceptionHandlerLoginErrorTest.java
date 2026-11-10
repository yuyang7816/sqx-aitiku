package com.sqx.common.web.exception;

import com.sqx.app.base.enums.AppError;
import com.sqx.common.core.domain.Result;
import com.sqx.common.core.enums.SystemErrorCode;
import com.sqx.common.core.exception.BizException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 登录链路全局异常 HTTP 状态与 body.code 映射
 */
class GlobalExceptionHandlerLoginErrorTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void bizException_verifyCodeError_shouldReturn400And350004() {
        ResponseEntity<Result<String>> response = handler.handleBizException(
                new BizException(AppError.VERIFY_CODE_ERROR));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(350004, response.getBody().getCode());
        assertEquals("验证码不正确", response.getBody().getMsg());
    }

    @Test
    void bizException_userBanned_shouldReturn400And350002() {
        ResponseEntity<Result<String>> response = handler.handleBizException(
                new BizException(AppError.USER_BANNED));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(350002, response.getBody().getCode());
    }

    @Test
    void bizException_verifyCodeTooFrequent_shouldReturn400And350010() {
        ResponseEntity<Result<String>> response = handler.handleBizException(
                new BizException(AppError.VERIFY_CODE_SEND_TOO_FREQUENT));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(350010, response.getBody().getCode());
    }

    @Test
    void bizException_verifyCodeSendFailed_shouldReturn400And350017() {
        ResponseEntity<Result<String>> response = handler.handleBizException(
                new BizException(AppError.VERIFY_CODE_SEND_FAILED));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(350017, response.getBody().getCode());
    }

    @Test
    void bizException_wechatNotConfigured_shouldReturn400And350018() {
        ResponseEntity<Result<String>> response = handler.handleBizException(
                new BizException(AppError.WECHAT_NOT_CONFIGURED));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(350018, response.getBody().getCode());
    }

    @Test
    void validationException_shouldReturn400And50001() throws NoSuchMethodException {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "target");
        bindingResult.addError(new FieldError("target", "phone", "请输入正确的手机号"));
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

        ResponseEntity<Result<String>> response = handler.handleValidException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(SystemErrorCode.VALIDATE_FAILED.getCode(), response.getBody().getCode());
        assertEquals("请输入正确的手机号", response.getBody().getMsg());
    }
}
