package com.sqx.app.referral.withdraw.support;

import com.sqx.common.core.exception.BizException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 提现金额校验单元测试
 */
class WithdrawAmountValidatorTest {

    @Test
    void validate_whenAmountIsValid_shouldPass() {
        assertDoesNotThrow(() -> WithdrawAmountValidator.validate(
                new BigDecimal("100"), new BigDecimal("500"), "50", "5000"));
    }

    @Test
    void validate_whenBelowMin_shouldThrow() {
        BizException ex = assertThrows(BizException.class, () -> WithdrawAmountValidator.validate(
                new BigDecimal("30"), new BigDecimal("500"), "50", "5000"));
        assertEquals("最小提现金额为50元", ex.getMessage());
    }

    @Test
    void validate_whenNotStepMultiple_shouldThrow() {
        BizException ex = assertThrows(BizException.class, () -> WithdrawAmountValidator.validate(
                new BigDecimal("75"), new BigDecimal("500"), "50", "5000"));
        assertEquals("提现金额需为50元的整数倍", ex.getMessage());
    }

    @Test
    void validate_whenExceedBalance_shouldThrow() {
        BizException ex = assertThrows(BizException.class, () -> WithdrawAmountValidator.validate(
                new BigDecimal("100"), new BigDecimal("50"), "50", "5000"));
        assertEquals("提现金额不能超过可提现余额", ex.getMessage());
    }
}
