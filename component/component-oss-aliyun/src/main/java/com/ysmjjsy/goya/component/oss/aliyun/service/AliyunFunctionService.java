package com.ysmjjsy.goya.component.oss.aliyun.service;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.*;
import com.ysmjjsy.goya.component.framework.common.exception.GoyaException;
import com.ysmjjsy.goya.component.framework.common.pool.AbstractObjectPool;
import com.ysmjjsy.goya.component.oss.aliyun.definition.service.BaseAliyunService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * <p>Aliyun OSS 不便于归类操作放置 Service  </p>
 *
 * @author goya
 * @since 2023/7/23 22:46
 */
@Slf4j
@Service
public class AliyunFunctionService extends BaseAliyunService {

    public AliyunFunctionService(AbstractObjectPool<OSS> ossClientObjectPool) {
        super(ossClientObjectPool);
    }

    /**
     * 描述OSS服务器区域
     *
     * @param request {@link DescribeRegionsRequest}
     * @return {@link DescribeRegionsResult}
     */
    public DescribeRegionsResult describeRegions(DescribeRegionsRequest request) {
        String function = "describeRegions";

        OSS client = getClient();

        try {
            return client.describeRegions(request);
        } catch (ClientException e) {
            log.error("[Goya] |- Aliyun OSS catch ClientException in [{}].", function, e);
            throw new GoyaException(e.getMessage());
        } catch (OSSException e) {
            log.error("[Goya] |- Aliyun OSS catch OSSException in [{}].", function, e);
            throw new GoyaException(e.getMessage());
        } finally {
            close(client);
        }
    }

    public String generateRtmpUri(GenerateRtmpUriRequest request) {
        String function = "generateRtmpUri";

        OSS client = getClient();

        try {
            return client.generateRtmpUri(request);
        } catch (ClientException e) {
            log.error("[Goya] |- Aliyun OSS catch ClientException in [{}].", function, e);
            throw new GoyaException(e.getMessage());
        } catch (OSSException e) {
            log.error("[Goya] |- Aliyun OSS catch OSSException in [{}].", function, e);
            throw new GoyaException(e.getMessage());
        } finally {
            close(client);
        }
    }

    public VoidResult createSymlink(CreateSymlinkRequest request) {
        String function = "createSymlink";

        OSS client = getClient();

        try {
            return client.createSymlink(request);
        } catch (ClientException e) {
            log.error("[Goya] |- Aliyun OSS catch ClientException in [{}].", function, e);
            throw new GoyaException(e.getMessage());
        } catch (OSSException e) {
            log.error("[Goya] |- Aliyun OSS catch OSSException in [{}].", function, e);
            throw new GoyaException(e.getMessage());
        } finally {
            close(client);
        }
    }

    public OSSSymlink getSymlink(GenericRequest request) {
        String function = "getSymlink";

        OSS client = getClient();

        try {
            return client.getSymlink(request);
        } catch (ClientException e) {
            log.error("[Goya] |- Aliyun OSS catch ClientException in [{}].", function, e);
            throw new GoyaException(e.getMessage());
        } catch (OSSException e) {
            log.error("[Goya] |- Aliyun OSS catch OSSException in [{}].", function, e);
            throw new GoyaException(e.getMessage());
        } finally {
            close(client);
        }
    }

    public VoidResult generateVodPlaylist(GenerateVodPlaylistRequest request) {
        String function = "generateVodPlaylist";

        OSS client = getClient();

        try {
            return client.generateVodPlaylist(request);
        } catch (ClientException e) {
            log.error("[Goya] |- Aliyun OSS catch ClientException in [{}].", function, e);
            throw new GoyaException(e.getMessage());
        } catch (OSSException e) {
            log.error("[Goya] |- Aliyun OSS catch OSSException in [{}].", function, e);
            throw new GoyaException(e.getMessage());
        } finally {
            close(client);
        }
    }

    public OSSObject getVodPlaylist(GetVodPlaylistRequest request) {
        String function = "getVodPlaylist";

        OSS client = getClient();

        try {
            return client.getVodPlaylist(request);
        } catch (ClientException e) {
            log.error("[Goya] |- Aliyun OSS catch ClientException in [{}].", function, e);
            throw new GoyaException(e.getMessage());
        } catch (OSSException e) {
            log.error("[Goya] |- Aliyun OSS catch OSSException in [{}].", function, e);
            throw new GoyaException(e.getMessage());
        } finally {
            close(client);
        }
    }
}
