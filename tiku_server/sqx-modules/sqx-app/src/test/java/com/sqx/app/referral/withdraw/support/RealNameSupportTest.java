package com.sqx.app.referral.withdraw.support;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 实名认证辅助单元测试
 */
class RealNameSupportTest {

    @Test
    void validateIdCardNo_when18DigitValid_shouldPass() {
        assertDoesNotThrow(() -> RealNameSupport.validateIdCardNo("110101199001011234"));
    }

    @Test
    void validateIdCardNo_when18DigitWithX_shouldPass() {
        assertDoesNotThrow(() -> RealNameSupport.validateIdCardNo("11010119900101123X"));
    }

    @Test
    void validateIdCardNo_whenInvalid_shouldThrow() {
        assertThrows(IllegalArgumentException.class, () -> RealNameSupport.validateIdCardNo("12345"));
    }

    @Test
    void maskIdCardNo_shouldMaskMiddle() {
        assertEquals("1101**********1234", RealNameSupport.maskIdCardNo("110101199001011234"));
    }
}
