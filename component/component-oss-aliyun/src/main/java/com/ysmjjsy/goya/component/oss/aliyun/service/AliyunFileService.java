package com.ysmjjsy.goya.component.oss.aliyun.service;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.DownloadFileRequest;
import com.aliyun.oss.model.DownloadFileResult;
import com.aliyun.oss.model.UploadFileRequest;
import com.aliyun.oss.model.UploadFileResult;
import com.ysmjjsy.goya.component.framework.common.exception.GoyaException;
import com.ysmjjsy.goya.component.framework.common.pool.AbstractObjectPool;
import com.ysmjjsy.goya.component.oss.aliyun.definition.service.BaseAliyunService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * <p>Aliyun OSS 文件操作 Service </p>
 *
 * @author goya
 * @since 2023/7/23 18:42
 */
@Slf4j
@Service
public class AliyunFileService extends BaseAliyunService {

    public AliyunFileService(AbstractObjectPool<OSS> ossClientObjectPool) {
        super(ossClientObjectPool);
    }

    public UploadFileResult uploadFile(UploadFileRequest request) {
        String function = "uploadFile";

        OSS client = getClient();

        try {
            return client.uploadFile(request);
        } catch (Throwable e) {
            log.error("[Goya] |- Aliyun OSS catch Throwable in [{}].", function, e);
            throw new GoyaException(e.getMessage());
        } finally {
            close(client);
        }
    }

    public DownloadFileResult downloadFile(DownloadFileRequest request) {
        String function = "downloadFile";

        OSS client = getClient();

        try {
            return client.downloadFile(request);
        } catch (Throwable e) {
            log.error("[Goya] |- Aliyun OSS catch Throwable in [{}].", function, e);
            throw new GoyaException(e.getMessage());
        } finally {
            close(client);
        }
    }
}
