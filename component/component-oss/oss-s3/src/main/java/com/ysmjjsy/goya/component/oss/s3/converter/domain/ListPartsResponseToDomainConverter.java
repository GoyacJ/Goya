package com.ysmjjsy.goya.component.oss.s3.converter.domain;

import com.ysmjjsy.goya.component.oss.core.domain.base.OwnerDomain;
import com.ysmjjsy.goya.component.oss.core.domain.multipart.ListPartsDomain;
import com.ysmjjsy.goya.component.oss.core.domain.multipart.PartSummaryDomain;
import com.ysmjjsy.goya.component.oss.core.utils.OssConverterUtils;
import org.springframework.core.convert.converter.Converter;
import software.amazon.awssdk.services.s3.model.Initiator;
import software.amazon.awssdk.services.s3.model.ListPartsResponse;
import software.amazon.awssdk.services.s3.model.Owner;
import software.amazon.awssdk.services.s3.model.Part;

/**
 * <p>ListPartsResponse 转 ListPartsDomain 转换器</p>
 *
 * @author goya
 * @since 2023/8/14 20:44
 */
public class ListPartsResponseToDomainConverter implements Converter<ListPartsResponse, ListPartsDomain> {

    private final Converter<Owner, OwnerDomain> ownerConverter = new OwnerToDomainConverter();
    private final Converter<Initiator, OwnerDomain> initiatorConverter = new InitiatorToDomainConverter();
    private final Converter<Part, PartSummaryDomain> partConverter = new PartToDomainConverter();

    @Override
    public ListPartsDomain convert(ListPartsResponse source) {
        ListPartsDomain domain = new ListPartsDomain();
        domain.setBucketName(source.bucket());
        domain.setObjectName(source.key());
        domain.setUploadId(source.uploadId());

        if (source.storageClass() != null) {
            domain.setStorageClass(source.storageClass().toString());
        }

        domain.setMaxParts(source.maxParts());
        domain.setPartNumberMarker(source.partNumberMarker());
        domain.setNextPartNumberMarker(source.nextPartNumberMarker());
        domain.setIsTruncated(source.isTruncated());

        if (source.owner() != null) {
            domain.setOwner(ownerConverter.convert(source.owner()));
        }

        if (source.initiator() != null) {
            domain.setInitiator(initiatorConverter.convert(source.initiator()));
        }

        if (source.parts() != null) {
            domain.setParts(OssConverterUtils.toDomains(source.parts(), partConverter));
        }

        return domain;
    }
}
