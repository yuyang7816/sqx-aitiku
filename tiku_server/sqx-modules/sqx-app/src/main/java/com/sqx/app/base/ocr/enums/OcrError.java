package com.sqx.app.base.ocr.enums;

import com.sqx.common.core.enums.IErrorCode;

/**
 * OCR 搜题业务错误码
 */
public enum OcrError implements IErrorCode {

    SEARCH_INPUT_EMPTY(1050004, "请提供图片 url 或题干文本"),
    RECOGNITION_FAILED(1050005, "图片识别失败，请重试或改用手动输入题干"),
    IMAGE_NOT_ACCESSIBLE(1050006, "无法读取图片，请确认已上传并可访问"),
    OCR_PLATFORM_NOT_FOUND(1050007, "OCR 供应商配置无效"),
    QUOTA_EXCEEDED(1050008, "今日拍照搜题次数已用完，开通会员可无限次使用"),
    ;

    private final Integer code;
    private final String message;

    OcrError(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public Integer getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
