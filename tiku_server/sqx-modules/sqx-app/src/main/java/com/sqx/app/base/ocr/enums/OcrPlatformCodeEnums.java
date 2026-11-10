package com.sqx.app.base.ocr.enums;

import lombok.Getter;

/**
 * OCR 供应商编码（与 sys_config.ocr_platform 对应）
 */
@Getter
public enum OcrPlatformCodeEnums {

    LOCAL_SIDECAR("local_sidecar", "本地 sidecar 文本（开发/测试）"),
    BAIDU("baidu", "百度通用 OCR"),
    ;

    private final String code;
    private final String desc;

    OcrPlatformCodeEnums(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
