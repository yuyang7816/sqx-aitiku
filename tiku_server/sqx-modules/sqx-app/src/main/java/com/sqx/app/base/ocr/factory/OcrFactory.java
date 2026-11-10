package com.sqx.app.base.ocr.factory;

import com.sqx.app.base.ocr.enums.OcrError;
import com.sqx.app.base.ocr.enums.OcrPlatformCodeEnums;
import com.sqx.app.base.ocr.strategy.BaiduOcrPlatformStrategy;
import com.sqx.app.base.ocr.strategy.IOcrPlatformStrategy;
import com.sqx.app.base.ocr.strategy.LocalSidecarOcrStrategy;
import com.sqx.app.base.ocr.support.OcrConfigReader;
import com.sqx.common.core.exception.BizException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * OCR 供应商工厂
 */
@Component
public class OcrFactory {

    private static final Map<String, IOcrPlatformStrategy> STATIC_PLATFORM_MAP = new ConcurrentHashMap<>();

    static {
        STATIC_PLATFORM_MAP.put(OcrPlatformCodeEnums.LOCAL_SIDECAR.getCode(), new LocalSidecarOcrStrategy());
    }

    @Resource
    private OcrConfigReader ocrConfigReader;

    public IOcrPlatformStrategy getPlatform() {
        String code = ocrConfigReader.getPlatform();
        if (StringUtils.isBlank(code)) {
            code = OcrPlatformCodeEnums.LOCAL_SIDECAR.getCode();
        }
        if (OcrPlatformCodeEnums.BAIDU.getCode().equals(code)) {
            return new BaiduOcrPlatformStrategy(ocrConfigReader.getBaiduApiKey(), ocrConfigReader.getBaiduSecretKey());
        }
        IOcrPlatformStrategy strategy = STATIC_PLATFORM_MAP.get(code);
        if (strategy == null) {
            throw new BizException(OcrError.OCR_PLATFORM_NOT_FOUND);
        }
        return strategy;
    }
}
