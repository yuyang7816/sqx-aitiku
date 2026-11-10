package com.sqx.app.base.file.service.impl;

import com.sqx.app.base.file.service.FileService;
import com.sqx.common.core.domain.Result;
import com.sqx.common.core.exception.BizException;
import com.sqx.common.file.domain.UploadRes;
import com.sqx.common.file.enums.FileError;
import com.sqx.common.file.factory.FileStorageFactory;
import com.sqx.common.file.storage.IFileStorage;
import com.sqx.common.file.support.FileUploadSupport;
import com.sqx.common.redis.utils.ConfigUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author chengming
 * @since 2025/4/29
 */
@Service
public class FileServiceImpl implements FileService {
    @Override
    public Result<UploadRes> upload(MultipartFile file) {
        return Result.success(doUpload(file));
    }

    @Override
    public Result<UploadRes> uploadImage(MultipartFile file) {
        FileUploadSupport.validateImageFile(file);
        return Result.success(doUpload(file));
    }

    private UploadRes doUpload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BizException(FileError.NONSUPPORT_EMPTY_FILE);
        }

        String fileStorageType = StringUtils.trimToEmpty(ConfigUtil.getConfigValue("file_storage_type"));
        IFileStorage fileStorage = FileStorageFactory.getFileStorage(fileStorageType);
        UploadRes uploadRes = fileStorage.upload(file);
        FileUploadSupport.enrichAccessUrl(uploadRes);
        return uploadRes;
    }
}
