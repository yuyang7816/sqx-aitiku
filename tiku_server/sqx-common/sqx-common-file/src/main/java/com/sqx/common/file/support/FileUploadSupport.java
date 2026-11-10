package com.sqx.common.file.support;

import com.sqx.common.core.exception.BizException;
import com.sqx.common.file.domain.UploadRes;
import com.sqx.common.file.enums.FileError;
import com.sqx.common.file.utils.FileUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传通用校验与出参补全
 */
public final class FileUploadSupport {

    private FileUploadSupport() {
    }

    /**
     * 校验图片文件（扩展名）
     */
    public static void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BizException(FileError.NONSUPPORT_EMPTY_FILE);
        }
        if (!FileUtil.isAllowedImageExtension(file.getOriginalFilename())) {
            throw new BizException(FileError.INVALID_IMAGE_TYPE);
        }
    }

    /**
     * 补全 url 字段，兼容旧客户端读取 path
     */
    public static void enrichAccessUrl(UploadRes uploadRes) {
        if (uploadRes == null) {
            return;
        }
        if (StringUtils.isBlank(uploadRes.getUrl()) && StringUtils.isNotBlank(uploadRes.getPath())) {
            uploadRes.setUrl(uploadRes.getPath());
        }
        if (StringUtils.isBlank(uploadRes.getPath()) && StringUtils.isNotBlank(uploadRes.getUrl())) {
            uploadRes.setPath(uploadRes.getUrl());
        }
    }
}
