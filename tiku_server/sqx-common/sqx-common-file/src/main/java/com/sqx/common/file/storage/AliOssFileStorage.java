package com.sqx.common.file.storage;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.PutObjectRequest;
import com.aliyun.oss.model.PutObjectResult;
import com.sqx.common.core.exception.BizException;
import com.sqx.common.file.domain.UploadParam;
import com.sqx.common.file.domain.UploadRes;
import com.sqx.common.file.enums.FileError;
import com.sqx.common.file.utils.FileUtil;
import com.sqx.common.redis.utils.ConfigUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * @author chengming
 * @since 2025/1/17
 */
@Slf4j
public class AliOssFileStorage implements IFileStorage {

    @Override
    public UploadRes upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BizException(FileError.NONSUPPORT_EMPTY_FILE);
        }

        // 设置 OSS Endpoint 和 Bucket 名称
        String endpoint = ConfigUtil.getConfigValue("file_endpoint");
        String bucketName = ConfigUtil.getConfigValue("file_bucket");
        String accessKeyId = ConfigUtil.getConfigValue("file_ali_oss_accessKeyId");
        String secretAccessKey = ConfigUtil.getConfigValue("file_ali_oss_secretAccessKey");

        // OSS object key 必须使用正斜杠，避免 Windows 下 File.separator 导致 CDN 404
        String extension = FileUtil.getFileExtension(file.getOriginalFilename());
        String fileId = UUID.randomUUID().toString();
        String fileName = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"))
                + "/"
                + fileId
                + "."
                + extension;

        // 创建 OSSClient 实例
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, secretAccessKey);
        try {
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, fileName, new ByteArrayInputStream(file.getBytes()));
            PutObjectResult result = ossClient.putObject(putObjectRequest);
            log.info("文件上传成功, eTag: {}", result.getETag());
        } catch (OSSException oe) {
            log.error("文件上传失败, 错误信息{}", oe.getErrorMessage());
            log.error("Request ID: {}", oe.getRequestId());
            log.error("Host ID: {}", oe.getHostId());
            throw new BizException(FileError.UPLOAD_FAILED);
        } catch (IOException e) {
            log.error("文件读取失败: {}", e.getMessage());
            throw new BizException(FileError.UPLOAD_FAILED);
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }

        String domain = ConfigUtil.getConfigValue("file_cdn_domain");
        String accessUrl = domain + "/" + fileName;
        UploadRes res = new UploadRes();
        res.setFileId(fileId);
        res.setRelativePath("/" + fileName);
        res.setExtension(extension);
        res.setSize(file.getSize());
        res.setUrl(accessUrl);
        res.setPath(accessUrl);
        return res;
    }

    @Override
    public UploadRes upload(UploadParam uploadParam) {
        String extension = FileUtil.getFileExtension(uploadParam.getFileName());
        String fileName = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"))
                + "/"
                + UUID.randomUUID()
                + "."
                + extension;

        // 创建 OSSClient 实例
        OSS ossClient = new OSSClientBuilder().build(uploadParam.getEndpoint(), uploadParam.getAccessKeyId(), uploadParam.getSecretAccessKey());
        try {
            PutObjectRequest putObjectRequest = new PutObjectRequest(uploadParam.getBucket(), fileName, uploadParam.getInputStream());
            PutObjectResult result = ossClient.putObject(putObjectRequest);
            log.info("文件上传成功, eTag: {}", result.getETag());
        } catch (OSSException oe) {
            log.error("文件上传失败, 错误信息{}", oe.getErrorMessage());
            log.error("Request ID: {}", oe.getRequestId());
            log.error("Host ID: {}", oe.getHostId());
            throw new BizException(FileError.UPLOAD_FAILED);
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }

        UploadRes res = new UploadRes();
        res.setPath(fileName);
        return res;
    }
}
