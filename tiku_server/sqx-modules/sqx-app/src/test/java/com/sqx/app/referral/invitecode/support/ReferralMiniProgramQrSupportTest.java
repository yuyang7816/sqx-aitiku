package com.sqx.app.referral.invitecode.support;

import com.sqx.app.base.utils.wx.WxUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ReferralMiniProgramQrSupportTest {

    @Mock
    private WxUtil wxUtil;

    @Spy
    @InjectMocks
    private ReferralMiniProgramQrSupport referralMiniProgramQrSupport;

    @Test
    void isWechatErrorJson_whenValidPng_shouldReturnFalse() {
        byte[] pngHeader = new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47};
        assertFalse(ReferralMiniProgramQrSupport.isWechatErrorJson(pngHeader));
    }

    @Test
    void isWechatErrorJson_whenErrorJson_shouldReturnTrue() {
        byte[] json = "{\"errcode\":40001,\"errmsg\":\"invalid credential\"}".getBytes();
        assertTrue(ReferralMiniProgramQrSupport.isWechatErrorJson(json));
    }

    @Test
    void isWechatErrorJson_whenEmpty_shouldReturnTrue() {
        assertTrue(ReferralMiniProgramQrSupport.isWechatErrorJson(new byte[0]));
    }

    @Test
    void toSafeFileName_shouldSanitizeSpecialChars() {
        assertEquals("INV8000.png", ReferralMiniProgramQrSupport.toSafeFileName("INV8000"));
        assertEquals("AB_CD.png", ReferralMiniProgramQrSupport.toSafeFileName("AB-CD"));
    }

    @Test
    void buildQrRelativePath_shouldIncludeReferralDir() {
        assertEquals(
                "/upload/referral/qr/INV8000.png",
                ReferralMiniProgramQrSupport.buildQrRelativePath("/upload", "INV8000.png")
        );
    }

    @Test
    void resolveQrFilePath_shouldPointToReferralQrDir(@TempDir Path root) {
        Path file = ReferralMiniProgramQrSupport.resolveQrFilePath("/upload", root.toString(), "INV8000");
        assertTrue(file.toString().replace('\\', '/').endsWith("/upload/referral/qr/INV8000.png"));
    }

    @Test
    void resolveExistingQrCodeUrl_whenNoCachedFile_shouldNotExistOnDisk(@TempDir Path root) {
        Path target = ReferralMiniProgramQrSupport.resolveQrFilePath("/upload", root.toString(), "NOTEXIST999");
        assertFalse(Files.exists(target));
    }

    @Test
    void buildQrCodeUrl_whenCachedFileExists_shouldSkipWechatApi() {
        doReturn("http://localhost:9135/sqx_fast/upload/referral/qr/INV8000.png")
                .when(referralMiniProgramQrSupport)
                .resolveExistingQrCodeUrl("INV8000");

        String url = referralMiniProgramQrSupport.buildQrCodeUrl("INV8000");

        assertEquals("http://localhost:9135/sqx_fast/upload/referral/qr/INV8000.png", url);
        verify(wxUtil, never()).getWxMaUnlimitedQrCode(anyString(), anyString(), anyString());
    }

    @Test
    void normalizeUploadPath_shouldDefaultAndPrefixSlash() {
        assertEquals("/upload", ReferralMiniProgramQrSupport.normalizeUploadPath(null));
        assertEquals("/upload", ReferralMiniProgramQrSupport.normalizeUploadPath("upload"));
    }
}
