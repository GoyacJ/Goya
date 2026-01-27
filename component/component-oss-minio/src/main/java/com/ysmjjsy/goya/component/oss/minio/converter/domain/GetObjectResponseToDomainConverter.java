package com.ysmjjsy.goya.component.oss.minio.converter.domain;

import com.ysmjjsy.goya.component.oss.core.domain.object.GetObjectDomain;
import io.minio.GetObjectResponse;
import org.springframework.core.convert.converter.Converter;

/**
 * <p>Minio GetObjectResponse 转 GetObjectDomain 转换器</p>
 *
 * @author goya
 * @since 2025/11/1 16:17
 */
public class GetObjectResponseToDomainConverter implements Converter<GetObjectResponse, GetObjectDomain> {
    @Override
    public GetObjectDomain convert(GetObjectResponse source) {

        GetObjectDomain domain = new GetObjectDomain();
        domain.setObjectContent(source);
        domain.setBucketName(source.bucket());
        domain.setRegion(source.region());
        domain.setObjectName(source.object());

        return domain;
    }
}