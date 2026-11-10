package com.sqx.common.file.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 文件工具类单元测试
 */
class FileUtilTest {

    @Test
    void isAllowedImageExtension_shouldAcceptCommonFormats() {
        assertTrue(FileUtil.isAllowedImageExtension("a.JPG"));
        assertTrue(FileUtil.isAllowedImageExtension("b.png"));
        assertTrue(FileUtil.isAllowedImageExtension("c.webp"));
    }

    @Test
    void isAllowedImageExtension_shouldRejectOthers() {
        assertFalse(FileUtil.isAllowedImageExtension("a.pdf"));
        assertFalse(FileUtil.isAllowedImageExtension(""));
        assertFalse(FileUtil.isAllowedImageExtension("noext"));
    }
}
