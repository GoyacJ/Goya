package com.ysmjjsy.goya.component.oss.minio.converter.domain;

import com.ysmjjsy.goya.component.core.exception.CommonException;
import com.ysmjjsy.goya.component.oss.core.domain.object.DeleteObjectDomain;
import com.ysmjjsy.goya.component.oss.minio.domain.DeleteErrorDomain;
import io.minio.Result;
import io.minio.errors.*;
import io.minio.messages.DeleteError;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * <p>Result  转 DeleteErrorEntity 转换器  </p>
 *
 * @author goya
 * @since 2025/11/1 16:21
 */
public class ResultDeleteErrorToDomainConverter implements Converter<Result<DeleteError>, DeleteObjectDomain> {

    private static final Logger log = LoggerFactory.getLogger(ResultDeleteErrorToDomainConverter.class);

    @Override
    public DeleteObjectDomain convert(Result<DeleteError> result) {
        String function = "converter";

        try {
            DeleteError deleteError = result.get();

            DeleteErrorDomain domain = new DeleteErrorDomain();
            if (ObjectUtils.isNotEmpty(deleteError)) {
                domain.setCode(deleteError.code());
                domain.setMessage(deleteError.message());
                domain.setBucketName(deleteError.bucketName());
                domain.setObjectName(deleteError.objectName());
                domain.setResource(deleteError.resource());
                domain.setRequestId(deleteError.requestId());
                domain.setHostId(deleteError.hostId());
            }
            return domain;
        } catch (ErrorResponseException e) {
            log.error("[Goya] |- Minio catch ErrorResponseException in [{}].", function, e);
            throw new CommonException(e.getMessage());
        } catch (InsufficientDataException e) {
            log.error("[Goya] |- Minio catch InsufficientDataException in [{}].", function, e);
            throw new CommonException(e.getMessage());
        } catch (InternalException e) {
            log.error("[Goya] |- Minio catch InternalException in [{}].", function, e);
            throw new CommonException(e.getMessage());
        } catch (InvalidKeyException e) {
            log.error("[Goya] |- Minio catch InvalidKeyException in [{}].", function, e);
            throw new CommonException(e.getMessage());
        } catch (InvalidResponseException e) {
            log.error("[Goya] |- Minio catch InvalidResponseException in [{}].", function, e);
            throw new CommonException(e.getMessage());
        } catch (IOException e) {
            log.error("[Goya] |- Minio catch IOException in [{}].", function, e);
            throw new CommonException(e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            log.error("[Goya] |- Minio catch NoSuchAlgorithmException in [{}].", function, e);
            throw new CommonException(e.getMessage());
        } catch (ServerException e) {
            log.error("[Goya] |- Minio catch ServerException in [{}].", function, e);
            throw new CommonException(e.getMessage());
        } catch (XmlParserException e) {
            log.error("[Goya] |- Minio catch XmlParserException in [{}].", function, e);
            throw new CommonException(e.getMessage());
        }
    }
}