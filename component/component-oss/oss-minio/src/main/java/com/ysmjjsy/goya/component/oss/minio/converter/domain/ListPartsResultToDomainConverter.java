package com.ysmjjsy.goya.component.oss.minio.converter.domain;

import com.ysmjjsy.goya.component.oss.core.arguments.multipart.ListPartsArguments;
import com.ysmjjsy.goya.component.oss.core.domain.base.OwnerDomain;
import com.ysmjjsy.goya.component.oss.core.domain.multipart.ListPartsDomain;
import com.ysmjjsy.goya.component.oss.core.domain.multipart.PartSummaryDomain;
import io.minio.messages.Initiator;
import io.minio.messages.ListPartsResult;
import io.minio.messages.Owner;
import io.minio.messages.Part;
import org.springframework.core.convert.converter.Converter;

import java.util.List;

/**
 * <p>ListPartsResult 转 PartSummaryDomain</p>
 *
 * @author goya
 * @since 2025/11/1 16:19
 */
public class ListPartsResultToDomainConverter implements Converter<ListPartsResult, ListPartsDomain> {

    private final Converter<Owner, OwnerDomain> owner = new OwnerToDomainConverter();
    private final Converter<Initiator, OwnerDomain> initiator = new InitiatorToDomainConverter();
    private final Converter<List<Part>, List<PartSummaryDomain>> parts = new PartToDomainConverter();

    private final ListPartsArguments arguments;

    public ListPartsResultToDomainConverter(ListPartsArguments arguments) {
        this.arguments = arguments;
    }

    @Override
    public ListPartsDomain convert(ListPartsResult source) {

        ListPartsDomain domain = new ListPartsDomain();
        domain.setOwner(owner.convert(source.owner()));
        domain.setInitiator(initiator.convert(source.initiator()));
        domain.setStorageClass(source.storageClass());
        domain.setMaxParts(source.maxParts());
        domain.setPartNumberMarker(source.partNumberMarker());
        domain.setNextPartNumberMarker(source.nextPartNumberMarker());
        domain.setIsTruncated(source.isTruncated());
        domain.setParts(parts.convert(source.partList()));
        domain.setUploadId(arguments.getUploadId());
        domain.setBucketName(source.bucketName());
        domain.setObjectName(source.objectName());

        return domain;
    }
}