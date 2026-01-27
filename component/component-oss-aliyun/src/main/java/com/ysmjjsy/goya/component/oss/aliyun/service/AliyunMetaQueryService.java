package com.ysmjjsy.goya.component.oss.aliyun.service;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.DoMetaQueryRequest;
import com.aliyun.oss.model.DoMetaQueryResult;
import com.aliyun.oss.model.GetMetaQueryStatusResult;
import com.aliyun.oss.model.VoidResult;
import com.ysmjjsy.goya.component.core.exception.CommonException;
import com.ysmjjsy.goya.component.core.pool.AbstractObjectPool;
import com.ysmjjsy.goya.component.oss.aliyun.definition.service.BaseAliyunService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * <p>Aliyun OSS MetaQuery Service </p>
 *
 * @author goya
 * @since 2023/7/23 22:05
 */
@Slf4j
@Service
public class AliyunMetaQueryService extends BaseAliyunService {

    public AliyunMetaQueryService(AbstractObjectPool<OSS> ossClientObjectPool) {
        super(ossClientObjectPool);
    }

    /**
     * 打开 OSS服务器 MetaQuery 配置
     *
     * @param bucketName 存储桶名称
     * @return {@link VoidResult}
     */
    public VoidResult openMetaQuery(String bucketName) {
        String function = "openMetaQuery";

        OSS client = getClient();

        try {
            return client.openMetaQuery(bucketName);
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

    /**
     * 获取 OSS服务器 MetaQueryStatus 配置
     *
     * @param bucketName 存储桶名称
     * @return {@link GetMetaQueryStatusResult}
     */
    public GetMetaQueryStatusResult getMetaQueryStatus(String bucketName) {
        String function = "getMetaQueryStatus";

        OSS client = getClient();

        try {
            return client.getMetaQueryStatus(bucketName);
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

    /**
     * 查询符合指定条件的文件
     *
     * @param request {@link DoMetaQueryRequest}
     * @return {@link DoMetaQueryResult}
     */
    public DoMetaQueryResult doMetaQuery(DoMetaQueryRequest request) {
        String function = "doMetaQuery";

        OSS client = getClient();

        try {
            return client.doMetaQuery(request);
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

    /**
     * 关闭元数据管理
     *
     * @param bucketName 存储桶名称
     * @return {@link VoidResult}
     */
    public VoidResult closeMetaQuery(String bucketName) {
        String function = "closeMetaQuery";

        OSS client = getClient();

        try {
            return client.closeMetaQuery(bucketName);
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
