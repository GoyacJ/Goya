package com.ysmjjsy.goya.component.oss.minio.converter.sse;

import com.ysmjjsy.goya.component.core.exception.CommonException;
import com.ysmjjsy.goya.component.core.utils.GoyaCryptoUtils;
import io.minio.ServerSideEncryptionCustomerKey;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.convert.converter.Converter;

import javax.crypto.SecretKey;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * <p>Minio Request 转 ServerSideEncryptionCustomerKey 转换器</p>
 *
 * @author goya
 * @since 2025/11/1 16:30
 */
@Slf4j
public class RequestToServerSideEncryptionCustomerKeyConverter implements Converter<String, ServerSideEncryptionCustomerKey> {

    @Override
    public ServerSideEncryptionCustomerKey convert(String customerKey) {
        if (StringUtils.isNotBlank(customerKey)) {
            SecretKey secretKey = GoyaCryptoUtils.generateAesKey(256);

            try {
                return new ServerSideEncryptionCustomerKey(secretKey);
            } catch (InvalidKeyException e) {
                log.error("[Goya] |- Minio catch InvalidKeyException in ObjectReadRequest prepare.", e);
                throw new CommonException(e.getMessage());
            } catch (NoSuchAlgorithmException e) {
                log.error("[Goya] |- Minio catch NoSuchAlgorithmException in ObjectReadRequest prepare.", e);
                throw new CommonException(e.getMessage());
            }
        }

        return null;
    }
}

