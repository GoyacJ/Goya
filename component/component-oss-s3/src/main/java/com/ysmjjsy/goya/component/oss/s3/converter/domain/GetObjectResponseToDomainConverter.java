package com.ysmjjsy.goya.component.oss.s3.converter.domain;

import com.ysmjjsy.goya.component.oss.core.domain.object.GetObjectDomain;
import org.springframework.core.convert.converter.Converter;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

/**
 * <p>ResponseInputStream&lt;GetObjectResponse&gt; 转 GetObjectDomain 转换器</p>
 * <p>注意：GetObjectResponse 不包含 bucket 和 key 信息，需要在 Repository 中手动设置</p>
 *
 * @author goya
 * @since 2023/8/14 22:05
 */
public class GetObjectResponseToDomainConverter implements Converter<ResponseInputStream<GetObjectResponse>, GetObjectDomain> {
    
    @Override
    public GetObjectDomain convert(ResponseInputStream<GetObjectResponse> source) {
        GetObjectDomain domain = new GetObjectDomain();
        domain.setObjectContent(source);
        return domain;
    }
}
