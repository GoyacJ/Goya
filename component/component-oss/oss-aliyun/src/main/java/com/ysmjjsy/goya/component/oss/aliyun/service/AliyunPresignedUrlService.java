package com.ysmjjsy.goya.component.oss.aliyun.service;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.GeneratePresignedUrlRequest;
import com.ysmjjsy.goya.component.core.exception.CommonException;
import com.ysmjjsy.goya.component.core.pool.AbstractObjectPool;
import com.ysmjjsy.goya.component.oss.aliyun.definition.service.BaseAliyunService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URL;

/**
 * <p>Aliyun OSS 预下载 Service </p>
 *
 * @author goya
 * @since 2023/7/23 16:45
 */
@Slf4j
@Service
public class AliyunPresignedUrlService extends BaseAliyunService {

    public AliyunPresignedUrlService(AbstractObjectPool<OSS> ossClientObjectPool) {
        super(ossClientObjectPool);
    }

    public URL generatePresignedUrl(GeneratePresignedUrlRequest request) {
        String function = "generatePresignedUrl";

        OSS client = getClient();

        try {
            return client.generatePresignedUrl(request);
        } catch (ClientException e) {
            log.error("[Goya] |- Aliyun OSS catch ClientException in [{}].", function, e);
            throw new CommonException(e.getMessage());
        } catch (OSSException e) {
            log.error("[Goya] |- Aliyun OSS catch OSSException in [{}].", function, e);
            throw new CommonException(e.getMessage());
        } finally {
            close(client);
        }
    }
}
