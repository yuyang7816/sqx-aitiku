package com.sqx.app.base.ocr.strategy;

import com.sqx.app.base.ocr.enums.OcrError;
import com.sqx.app.base.ocr.support.OcrImagePathSupport;
import com.sqx.common.core.exception.BizException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 本地 sidecar OCR：读取与图片同目录的 {fileId}.ocr.txt（开发/测试用）
 */
@Slf4j
public class LocalSidecarOcrStrategy implements IOcrPlatformStrategy {

    @Override
    public String recognizeText(String imageUrl) {
        Path imagePath = OcrImagePathSupport.resolveLocalPath(imageUrl);
        if (imagePath == null) {
            throw new BizException(OcrError.IMAGE_NOT_ACCESSIBLE);
        }
        String fileName = imagePath.getFileName().toString();
        int dot = fileName.lastIndexOf('.');
        String baseName = dot > 0 ? fileName.substring(0, dot) : fileName;
        Path sidecar = imagePath.getParent().resolve(baseName + ".ocr.txt");
        if (!Files.exists(sidecar)) {
            log.warn("OCR sidecar 不存在: {}", sidecar);
            throw new BizException(OcrError.RECOGNITION_FAILED);
        }
        try {
            String text = new String(Files.readAllBytes(sidecar), StandardCharsets.UTF_8).trim();
            if (StringUtils.isBlank(text)) {
                throw new BizException(OcrError.RECOGNITION_FAILED);
            }
            return text;
        } catch (BizException ex) {
            throw ex;
        } catch (Exception e) {
            log.error("读取 OCR sidecar 失败", e);
            throw new BizException(OcrError.RECOGNITION_FAILED);
        }
    }
}
