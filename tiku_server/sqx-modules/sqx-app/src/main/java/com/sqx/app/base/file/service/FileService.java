package com.sqx.app.base.file.service;

import com.sqx.common.core.domain.Result;
import com.sqx.common.file.domain.UploadRes;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author chengming
 * @since 2025/4/29
 */
public interface FileService {
    Result<UploadRes> upload(MultipartFile file);

    /**
     * 上传图片（校验扩展名，供 OCR 等场景）
     */
    Result<UploadRes> uploadImage(MultipartFile file);
}
