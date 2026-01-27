package com.ysmjjsy.goya.component.oss.aliyun.service;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.*;
import com.ysmjjsy.goya.component.core.exception.CommonException;
import com.ysmjjsy.goya.component.core.pool.AbstractObjectPool;
import com.ysmjjsy.goya.component.oss.aliyun.definition.service.BaseAliyunService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * <p>Aliyun Java SDK 异步相关操作 Service </p>
 *
 * @author goya
 * @since 2023/7/23 21:50
 */
@Slf4j
@Service
public class AliyunAsyncService extends BaseAliyunService {

    public AliyunAsyncService(AbstractObjectPool<OSS> clientObjectPool) {
        super(clientObjectPool);
    }

    /**
     * 设置异步获取任务
     *
     * @param request {@link SetAsyncFetchTaskRequest}
     * @return {@link SetAsyncFetchTaskResult}
     */
    public SetAsyncFetchTaskResult setAsyncFetchTask(SetAsyncFetchTaskRequest request) {
        String function = "setAsyncFetchTask";

        OSS client = getClient();

        try {
            return client.setAsyncFetchTask(request);
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
     * 获取异步获取任务信息
     *
     * @param request {@link GetAsyncFetchTaskRequest}
     * @return {@link GetAsyncFetchTaskResult}
     */
    public GetAsyncFetchTaskResult getAsyncFetchTask(GetAsyncFetchTaskRequest request) {
        String function = "getAsyncFetchTask";

        OSS client = getClient();

        try {
            return client.getAsyncFetchTask(request);
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
     * 对指定的文件应用异步操作.
     *
     * @param request {@link AsyncProcessObjectRequest}
     * @return {@link AsyncProcessObjectResult}
     */
    public AsyncProcessObjectResult asyncProcessObject(AsyncProcessObjectRequest request) {
        String function = "asyncProcessObject";

        OSS client = getClient();

        try {
            return client.asyncProcessObject(request);
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
