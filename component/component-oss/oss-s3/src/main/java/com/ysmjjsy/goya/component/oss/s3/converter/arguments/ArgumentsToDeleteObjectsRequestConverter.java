package com.ysmjjsy.goya.component.oss.s3.converter.arguments;

import com.ysmjjsy.goya.component.oss.core.arguments.object.DeleteObjectsArguments;
import com.ysmjjsy.goya.component.oss.core.arguments.object.DeletedObjectArguments;
import com.ysmjjsy.goya.component.oss.s3.definition.arguments.ArgumentsToBucketConverter;
import org.apache.commons.collections4.CollectionUtils;
import software.amazon.awssdk.services.s3.model.Delete;
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>统一定义 DeleteObjectsArguments 转 S3 DeleteObjectsRequest 转换器</p>
 *
 * @author goya
 * @since 2023/8/14 21:40
 */
public class ArgumentsToDeleteObjectsRequestConverter extends ArgumentsToBucketConverter<DeleteObjectsArguments, DeleteObjectsRequest> {
    
    @Override
    public DeleteObjectsRequest getInstance(DeleteObjectsArguments arguments) {
        List<ObjectIdentifier> objectIdentifiers = new ArrayList<>();
        
        if (CollectionUtils.isNotEmpty(arguments.getObjects())) {
            for (DeletedObjectArguments deletedObject : arguments.getObjects()) {
                ObjectIdentifier.Builder identifierBuilder = ObjectIdentifier.builder()
                        .key(deletedObject.getObjectName());
                
                if (deletedObject.getVersionId() != null) {
                    identifierBuilder.versionId(deletedObject.getVersionId());
                }
                
                objectIdentifiers.add(identifierBuilder.build());
            }
        }

        Delete delete = Delete.builder()
                .objects(objectIdentifiers)
                .quiet(arguments.getQuiet() != null && arguments.getQuiet())
                .build();

        DeleteObjectsRequest.Builder builder = DeleteObjectsRequest.builder()
                .bucket(arguments.getBucketName())
                .delete(delete);

        if (arguments.getBypassGovernanceMode() != null && arguments.getBypassGovernanceMode()) {
            builder.bypassGovernanceRetention(arguments.getBypassGovernanceMode());
        }

        return builder.build();
    }
}
