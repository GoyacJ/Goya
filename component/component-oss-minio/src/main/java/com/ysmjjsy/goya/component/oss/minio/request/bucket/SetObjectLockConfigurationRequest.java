package com.ysmjjsy.goya.component.oss.minio.request.bucket;

import com.ysmjjsy.goya.component.oss.minio.converter.retention.DomainToObjectLockConfigurationConverter;
import com.ysmjjsy.goya.component.oss.minio.definition.BucketRequest;
import com.ysmjjsy.goya.component.oss.minio.domain.ObjectLockConfigurationDomain;
import io.minio.SetObjectLockConfigurationArgs;
import io.minio.messages.ObjectLockConfiguration;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.core.convert.converter.Converter;

import java.io.Serial;

/**
 * <p> 设置存储桶对象锁定配置请求参数实体 </p>
 *
 * @author goya
 * @since 2023/6/6 23:01
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(name = "设置存储桶对象锁定配置请求参数实体", title = "设置存储桶对象锁定配置请求参数实体")
public class SetObjectLockConfigurationRequest extends BucketRequest<SetObjectLockConfigurationArgs.Builder, SetObjectLockConfigurationArgs> {

    @Serial
    private static final long serialVersionUID = 2147515738819358527L;

    private final Converter<ObjectLockConfigurationDomain, ObjectLockConfiguration> requestTo = new DomainToObjectLockConfigurationConverter();

    @Schema(name = "对象锁定配置", requiredMode = Schema.RequiredMode.REQUIRED, description = "既然是设置操作那么设置的值就不能为空")
    @NotNull(message = "对象锁定配置信息不能为空")
    private ObjectLockConfigurationDomain objectLock;

    @Override
    public void prepare(SetObjectLockConfigurationArgs.Builder builder) {
        // 既然是设置操作那么设置的值就不能为空
        builder.config(requestTo.convert(getObjectLock()));
        super.prepare(builder);
    }

    @Override
    public SetObjectLockConfigurationArgs.Builder getBuilder() {
        return SetObjectLockConfigurationArgs.builder();
    }
}
