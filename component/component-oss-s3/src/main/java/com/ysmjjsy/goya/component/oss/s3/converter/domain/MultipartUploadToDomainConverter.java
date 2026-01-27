package com.ysmjjsy.goya.component.oss.s3.converter.domain;

import com.ysmjjsy.goya.component.framework.common.utils.GoyaDateUtils;
import com.ysmjjsy.goya.component.framework.oss.domain.base.OwnerDomain;
import com.ysmjjsy.goya.component.framework.oss.domain.multipart.UploadDomain;
import org.springframework.core.convert.converter.Converter;
import software.amazon.awssdk.services.s3.model.Initiator;
import software.amazon.awssdk.services.s3.model.MultipartUpload;
import software.amazon.awssdk.services.s3.model.Owner;

/**
 * <p>MultipartUpload 转 UploadDomain 转换器</p>
 *
 * @author goya
 * @since 2023/8/14 20:35
 */
public class MultipartUploadToDomainConverter implements Converter<MultipartUpload, UploadDomain> {

    private final Converter<Owner, OwnerDomain> ownerConverter = new OwnerToDomainConverter();
    private final Converter<Initiator, OwnerDomain> initiatorConverter = new InitiatorToDomainConverter();


    @Override
    public UploadDomain convert(MultipartUpload source) {
        UploadDomain domain = new UploadDomain();
        domain.setKey(source.key());
        domain.setUploadId(source.uploadId());
        
        if (source.owner() != null) {
            domain.setOwner(ownerConverter.convert(source.owner()));
        }
        
        if (source.initiator() != null) {
            domain.setInitiator(initiatorConverter.convert(source.initiator()));
        }
        
        if (source.storageClass() != null) {
            domain.setStorageClass(source.storageClass().toString());
        }
        
        if (source.initiated() != null) {
            domain.setInitiated(GoyaDateUtils.toLocalDateTime(source.initiated()));
        }
        
        return domain;
    }
}
