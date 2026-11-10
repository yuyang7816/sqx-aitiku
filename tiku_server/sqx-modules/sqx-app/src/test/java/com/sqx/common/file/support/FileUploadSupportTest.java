package com.sqx.common.file.support;

import com.sqx.common.core.exception.BizException;
import com.sqx.common.file.domain.UploadRes;
import com.sqx.common.file.enums.FileError;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 文件上传校验与出参补全单元测试
 */
class FileUploadSupportTest {

    @Test
    void validateImageFile_shouldAcceptPng() {
        MockMultipartFile file = new MockMultipartFile("file", "photo.png", "image/png", new byte[]{1, 2, 3});
        FileUploadSupport.validateImageFile(file);
    }

    @Test
    void validateImageFile_shouldRejectEmptyFile() {
        MockMultipartFile file = new MockMultipartFile("file", "photo.png", "image/png", new byte[0]);
        BizException ex = assertThrows(BizException.class, () -> FileUploadSupport.validateImageFile(file));
        assertEquals(FileError.NONSUPPORT_EMPTY_FILE.getCode(), ex.getErrorCode().getCode());
    }

    @Test
    void validateImageFile_shouldRejectUnsupportedExtension() {
        MockMultipartFile file = new MockMultipartFile("file", "doc.pdf", "application/pdf", new byte[]{1});
        BizException ex = assertThrows(BizException.class, () -> FileUploadSupport.validateImageFile(file));
        assertEquals(FileError.INVALID_IMAGE_TYPE.getCode(), ex.getErrorCode().getCode());
    }

    @Test
    void enrichAccessUrl_shouldMirrorPathToUrl() {
        UploadRes res = new UploadRes();
        res.setPath("http://localhost:9135/sqx_fast/upload/a.png");
        FileUploadSupport.enrichAccessUrl(res);
        assertEquals(res.getPath(), res.getUrl());
    }

    @Test
    void enrichAccessUrl_shouldMirrorUrlToPath() {
        UploadRes res = new UploadRes();
        res.setUrl("http://localhost:9135/sqx_fast/upload/b.jpg");
        FileUploadSupport.enrichAccessUrl(res);
        assertTrue(res.getPath().contains("/upload/b.jpg"));
    }
}
