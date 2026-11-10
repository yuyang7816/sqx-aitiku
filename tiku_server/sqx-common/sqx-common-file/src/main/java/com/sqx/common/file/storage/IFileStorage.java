package com.sqx.common.file.storage;

import com.sqx.common.file.domain.UploadParam;
import com.sqx.common.file.domain.UploadRes;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author chengming
 * @since 2024/10/25
 */
public interface IFileStorage {

    /**
     * 文件上传接口
     * @param file 文件
     */
    UploadRes upload(MultipartFile file);

    /**
     * 文件上传
     * @return
     */
    UploadRes upload(UploadParam uploadParam);
}
