package com.sqx.common.file.enums;

import com.sqx.common.core.enums.IErrorCode;

/**
 * @author chengming
 * @since 2025/5/14
 */
public enum FileError implements IErrorCode {

    UPLOAD_FAILED(1050001, "文件上传失败"),
    NONSUPPORT_EMPTY_FILE(1050002, "不能上传空文件"),
    INVALID_IMAGE_TYPE(1050003, "仅支持 jpg/jpeg/png/gif/webp/bmp 图片"),
    ;

    private final Integer code;
    private final String message;

    FileError(Integer code, String message) {
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
