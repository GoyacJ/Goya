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

import java.util.List;

/**
 * <p>Aliyun OSS 直播频道 Service </p>
 *
 * @author goya
 * @since 2023/7/23 21:31
 */
@Slf4j
@Service
public class AliyunLiveChannelService extends BaseAliyunService {

    public AliyunLiveChannelService(AbstractObjectPool<OSS> ossClientObjectPool) {
        super(ossClientObjectPool);
    }

    public CreateLiveChannelResult createLiveChannel(CreateLiveChannelRequest request) {
        String function = "createLiveChannel";

        OSS client = getClient();

        try {
            return client.createLiveChannel(request);
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

    public VoidResult setLiveChannelStatus(SetLiveChannelRequest request) {
        String function = "setLiveChannelStatus";

        OSS client = getClient();

        try {
            return client.setLiveChannelStatus(request);
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

    public LiveChannelInfo getLiveChannelInfo(LiveChannelGenericRequest request) {
        String function = "getLiveChannelInfo";

        OSS client = getClient();

        try {
            return client.getLiveChannelInfo(request);
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

    public LiveChannelStat getLiveChannelStat(LiveChannelGenericRequest request) {
        String function = "getLiveChannelStat";

        OSS client = getClient();

        try {
            return client.getLiveChannelStat(request);
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

    public VoidResult deleteLiveChannel(LiveChannelGenericRequest request) {
        String function = "deleteLiveChannel";

        OSS client = getClient();

        try {
            return client.deleteLiveChannel(request);
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

    public LiveChannelListing listLiveChannels(ListLiveChannelsRequest request) {
        String function = "listLiveChannels";

        OSS client = getClient();

        try {
            return client.listLiveChannels(request);
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

    public List<LiveRecord> getLiveChannelHistory(LiveChannelGenericRequest request) {
        String function = "getLiveChannelHistory";

        OSS client = getClient();

        try {
            return client.getLiveChannelHistory(request);
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
