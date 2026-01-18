package com.ysmjjsy.goya.component.oss.minio.request.object;

import com.ysmjjsy.goya.component.oss.minio.definition.ObjectConditionalReadRequest;
import com.ysmjjsy.goya.component.oss.minio.definition.ObjectReadRequest;
import io.minio.StatObjectArgs;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * <p> 获取对象信息和元数据请求参数 </p>
 *
 * @author goya
 * @since 2023/5/31 16:18
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(name = "获取对象信息和元数据请求参数")
public class StatObjectRequest extends ObjectConditionalReadRequest<StatObjectArgs.Builder, StatObjectArgs> {

    @Serial
    private static final long serialVersionUID = -2690714791663247578L;

    public StatObjectRequest(ObjectReadRequest<StatObjectArgs.Builder, StatObjectArgs> request) {
        this.setExtraHeaders(request.getExtraHeaders());
        this.setExtraQueryParams(request.getExtraQueryParams());
        this.setBucketName(request.getBucketName());
        this.setRegion(request.getRegion());
        this.setObjectName(request.getObjectName());
        this.setVersionId(request.getVersionId());
        this.setServerSideEncryptionCustomerKey(request.getServerSideEncryptionCustomerKey());
    }

    @Override
    public StatObjectArgs.Builder getBuilder() {
        return StatObjectArgs.builder();
    }
}
