package com.ysmjjsy.goya.component.oss.minio.converter.domain;

import com.ysmjjsy.goya.component.core.utils.GoyaDateUtils;
import com.ysmjjsy.goya.component.oss.core.domain.base.OwnerDomain;
import com.ysmjjsy.goya.component.oss.core.domain.multipart.UploadDomain;
import io.minio.messages.Initiator;
import io.minio.messages.Owner;
import io.minio.messages.Upload;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.core.convert.converter.Converter;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>Upload 转 UploadDomain 转换器</p>
 *
 * @author goya
 * @since 2025/11/1 16:26
 */
public class UploadToDomainConverter implements Converter<List<Upload>, List<UploadDomain>> {

    private final Converter<Owner, OwnerDomain> owner = new OwnerToDomainConverter();
    private final Converter<Initiator, OwnerDomain> initiator = new InitiatorToDomainConverter();

    @Override
    public List<UploadDomain> convert(List<Upload> source) {
        if (CollectionUtils.isNotEmpty(source)) {
            return source.stream().map(this::convert).toList();
        }
        return new ArrayList<>();
    }

    private UploadDomain convert(Upload source) {
        UploadDomain domain = new UploadDomain();
        domain.setKey(source.objectName());
        domain.setUploadId(source.uploadId());
        domain.setOwner(owner.convert(source.owner()));
        domain.setInitiator(initiator.convert(source.initiator()));
        domain.setStorageClass(source.storageClass());
        domain.setInitiated(GoyaDateUtils.zonedDateTimeToLocalDateTime(source.initiated()));
        return domain;
    }
}