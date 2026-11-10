package com.sqx.app.base.verifyCode.domain.bo;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * TC-M02-001 发送验证码入参手机号校验（与 AppSendVerifyCodeBO @Pattern 一致）
 */
class AppSendVerifyCodeBOValidationTest {

    private static Validator validator;

    @BeforeAll
    static void initValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void invalidPhone_shouldFailValidation() {
        AppSendVerifyCodeBO bo = new AppSendVerifyCodeBO();
        bo.setPhone("12345");

        Set<ConstraintViolation<AppSendVerifyCodeBO>> violations = validator.validate(bo);
        assertFalse(violations.isEmpty());
    }

    @Test
    void validPhone_shouldPassValidation() {
        AppSendVerifyCodeBO bo = new AppSendVerifyCodeBO();
        bo.setPhone("13800138000");

        Set<ConstraintViolation<AppSendVerifyCodeBO>> violations = validator.validate(bo);
        assertTrue(violations.isEmpty());
    }
}
