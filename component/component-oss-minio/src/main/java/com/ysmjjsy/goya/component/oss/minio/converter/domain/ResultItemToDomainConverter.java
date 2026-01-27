package com.ysmjjsy.goya.component.oss.minio.converter.domain;

import com.ysmjjsy.goya.component.framework.common.exception.GoyaException;
import com.ysmjjsy.goya.component.framework.common.utils.GoyaDateUtils;
import com.ysmjjsy.goya.component.framework.oss.domain.base.OwnerDomain;
import com.ysmjjsy.goya.component.framework.oss.domain.object.ObjectDomain;
import io.minio.Result;
import io.minio.errors.*;
import io.minio.messages.Item;
import io.minio.messages.Owner;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * <p>Minio Item 转 ObjectDomain 转换器</p>
 *
 * @author goya
 * @since 2025/11/1 16:22
 */
public class ResultItemToDomainConverter implements Converter<Result<Item>, ObjectDomain> {

    private static final Logger log = LoggerFactory.getLogger(ResultItemToDomainConverter.class);

    private final String bucketName;

    public ResultItemToDomainConverter(String bucketName) {
        this.bucketName = bucketName;
    }

    @Override
    public ObjectDomain convert(Result<Item> result) {
        String function = "convert";

        try {
            Item item = result.get();
            ObjectDomain objectDomain = new ObjectDomain();
            objectDomain.setBucketName(bucketName);
            objectDomain.setObjectName(item.objectName());
            objectDomain.setIsDir(item.isDir());
            if (!item.isDir()) {
                objectDomain.setETag(item.etag());
                objectDomain.setLastModified(GoyaDateUtils.zonedDateTimeToLocalDateTime(item.lastModified()));
                if (ObjectUtils.isNotEmpty(item.owner())) {
                    Converter<Owner, OwnerDomain> toAttr = new OwnerToDomainConverter();
                    objectDomain.setOwnerAttribute(toAttr.convert(item.owner()));
                }
                objectDomain.setSize(item.size());
                objectDomain.setStorageClass(item.storageClass());

            }
            return objectDomain;
        } catch (ErrorResponseException e) {
            log.error("[Goya] |- Minio catch ErrorResponseException in [{}].", function, e);
            throw new GoyaException(e.getMessage());
        } catch (InsufficientDataException e) {
            log.error("[Goya] |- Minio catch InsufficientDataException in [{}].", function, e);
            throw new GoyaException(e.getMessage());
        } catch (InternalException e) {
            log.error("[Goya] |- Minio catch InternalException in [{}].", function, e);
            throw new GoyaException(e.getMessage());
        } catch (InvalidKeyException e) {
            log.error("[Goya] |- Minio catch InvalidKeyException in [{}].", function, e);
            throw new GoyaException(e.getMessage());
        } catch (InvalidResponseException e) {
            log.error("[Goya] |- Minio catch InvalidResponseException in [{}].", function, e);
            throw new GoyaException(e.getMessage());
        } catch (IOException e) {
            log.error("[Goya] |- Minio catch IOException in [{}].", function, e);
            throw new GoyaException(e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            log.error("[Goya] |- Minio catch NoSuchAlgorithmException in [{}].", function, e);
            throw new GoyaException(e.getMessage());
        } catch (ServerException e) {
            log.error("[Goya] |- Minio catch ServerException in [{}].", function, e);
            throw new GoyaException(e.getMessage());
        } catch (XmlParserException e) {
            log.error("[Goya] |- Minio catch XmlParserException in [{}].", function, e);
            throw new GoyaException(e.getMessage());
        }
    }
}
