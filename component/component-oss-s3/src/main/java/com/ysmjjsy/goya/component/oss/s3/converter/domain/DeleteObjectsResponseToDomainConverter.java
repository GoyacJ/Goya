package com.ysmjjsy.goya.component.oss.s3.converter.domain;

import com.ysmjjsy.goya.component.framework.oss.domain.object.DeleteObjectDomain;
import org.springframework.core.convert.converter.Converter;
import software.amazon.awssdk.services.s3.model.DeleteObjectsResponse;
import software.amazon.awssdk.services.s3.model.DeletedObject;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>DeleteObjectsResponse 转 List&lt;DeleteObjectDomain&gt; 转换器</p>
 *
 * @author goya
 * @since 2023/8/14 21:45
 */
public class DeleteObjectsResponseToDomainConverter implements Converter<DeleteObjectsResponse, List<DeleteObjectDomain>> {
    
    @Override
    public List<DeleteObjectDomain> convert(DeleteObjectsResponse source) {
        List<DeleteObjectDomain> domains = new ArrayList<>();
        
        if (source.deleted() != null) {
            for (DeletedObject deleted : source.deleted()) {
                DeleteObjectDomain domain = new DeleteObjectDomain(deleted.key());
                domain.setVersionId(deleted.versionId());
                domains.add(domain);
            }
        }
        
        return domains;
    }
}
