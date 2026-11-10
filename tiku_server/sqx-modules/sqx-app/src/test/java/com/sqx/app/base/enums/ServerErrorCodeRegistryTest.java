package com.sqx.app.base.enums;

import com.sqx.app.base.ai.enums.AiError;
import com.sqx.app.base.ocr.enums.OcrError;
import com.sqx.app.vip.pay.enums.PayError;
import com.sqx.common.core.enums.SystemErrorCode;
import com.sqx.common.file.enums.FileError;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 统一错误码注册表校验（与 docs/error-codes.md 同步）
 */
class ServerErrorCodeRegistryTest {

    @Test
    void clientFacingCodes_shouldBeDistinct() {
        Set<Integer> codes = ServerErrorCodeRegistry.clientFacingCodes();
        assertEquals(codes.size(), new HashSet<>(codes).size(), "clientFacing 错误码存在重复: " + codes);
    }

    @Test
    void find_shouldReturnKnownCodes() {
        ServerErrorCodeRegistry.Entry ocrQuota = ServerErrorCodeRegistry.find(OcrError.QUOTA_EXCEEDED.getCode());
        assertNotNull(ocrQuota);
        assertEquals(ServerErrorCodeRegistry.ToastType.DIALOG_VIP, ocrQuota.getToastType());
        assertEquals(OcrError.QUOTA_EXCEEDED.getMessage(), ocrQuota.getMessage());

        ServerErrorCodeRegistry.Entry aiSensitive = ServerErrorCodeRegistry.find(AiError.SENSITIVE_INPUT.getCode());
        assertNotNull(aiSensitive);
        assertEquals(ServerErrorCodeRegistry.ToastType.SILENT, aiSensitive.getToastType());

        ServerErrorCodeRegistry.Entry loginBanned = ServerErrorCodeRegistry.find(AppError.USER_BANNED.getCode());
        assertNotNull(loginBanned);
        assertEquals(ServerErrorCodeRegistry.ToastType.ACCOUNT_BANNED, loginBanned.getToastType());
        assertTrue(loginBanned.isHttpBadRequest());
    }

    @Test
    void allClientFacing_shouldCoverFeatureModules() {
        Set<Integer> codes = ServerErrorCodeRegistry.clientFacingCodes();
        assertTrue(codes.contains(FileError.INVALID_IMAGE_TYPE.getCode()));
        assertTrue(codes.contains(PayError.PLAN_NOT_FOUND.getCode()));
        assertTrue(codes.contains(SystemErrorCode.VALIDATE_FAILED.getCode()));
        assertTrue(codes.contains(401));
    }

    @Test
    void nonLoginFeatureErrors_shouldNotMapToBadRequest() {
        ServerErrorCodeRegistry.Entry entry = ServerErrorCodeRegistry.find(OcrError.QUOTA_EXCEEDED.getCode());
        assertNotNull(entry);
        assertFalse(entry.isHttpBadRequest());
    }

    @Test
    void registrySize_shouldMatchDocumentedClientFacingCount() {
        assertTrue(ServerErrorCodeRegistry.allClientFacing().size() >= 40,
                "clientFacing 条目数异常偏少，请核对 docs/error-codes.md");
    }
}
