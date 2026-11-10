package com.sqx.common.file.domain;

import lombok.Data;

import java.io.InputStream;

/**
 * @author chengming
 * @since 2025/3/4
 */
@Data
public class UploadParam {

    /**
     * OSS endpoint
     */
    private String endpoint;

    /**
     * OSS 存储桶
     */
    private String bucket;

    private String accessKeyId;

    private String secretAccessKey;

    /**
     * 文件目录
     */
    private String dir;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 文件流
     */
    private InputStream inputStream;
}
