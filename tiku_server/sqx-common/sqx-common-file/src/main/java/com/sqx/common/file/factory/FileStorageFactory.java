package com.sqx.common.file.factory;

import com.sqx.common.core.exception.SqxException;
import com.sqx.common.file.enums.StorageTypeEnum;
import com.sqx.common.file.storage.AliOssFileStorage;
import com.sqx.common.file.storage.IFileStorage;
import com.sqx.common.file.storage.LocalFileStorage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author chengming
 * @since 2024/10/25
 */
@Slf4j
public class FileStorageFactory {

    private static final Map<String, IFileStorage> STORAGE_MAP = new ConcurrentHashMap<>();

    static {
        STORAGE_MAP.put(StorageTypeEnum.LOCAL.code(), new LocalFileStorage());
        STORAGE_MAP.put(StorageTypeEnum.ALIOSS.code(), new AliOssFileStorage());
    }

    public static IFileStorage getFileStorage(String storageType) {
        String normalizedType = StringUtils.trimToEmpty(storageType);
        IFileStorage fileStorage = STORAGE_MAP.get(normalizedType);
        if (Objects.isNull(fileStorage)) {
            log.error("获取文件存储平台失败: {}", storageType);
            throw new SqxException("上传文件失败");
        }

        return fileStorage;
    }
}
