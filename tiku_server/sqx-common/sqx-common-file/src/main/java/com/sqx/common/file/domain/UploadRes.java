package com.sqx.common.file.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author chengming
 * @since 2025/4/29
 */
@Data
public class UploadRes {

    /**
     * 文件访问 URL（完整地址，上传后可直接访问）
     */
    @Schema(description = "文件访问 URL")
    private String url;

    /**
     * 文件唯一标识（不含扩展名，可用于 OCR 等业务关联）
     */
    @Schema(description = "文件唯一标识")
    private String fileId;

    /**
     * 文件路径（兼容旧字段，与 url 一致）
     */
    @Schema(description = "文件路径")
    private String path;

    /**
     * 相对路径（如 /upload/2026/08/05/uuid.jpg）
     */
    @Schema(description = "相对路径")
    private String relativePath;

    /**
     * 存储类型
     * */
    @Schema(description = "存储类型")
    private String extension;

    /**
     * 文件大小
     * */
    @Schema(description = "文件大小")
    private Long size;
}
