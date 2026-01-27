package com.ysmjjsy.goya.component.oss.minio.request.object;

import com.ysmjjsy.goya.component.oss.minio.converter.retention.DomainToRetentionConverter;
import com.ysmjjsy.goya.component.oss.minio.definition.ObjectVersionRequest;
import com.ysmjjsy.goya.component.oss.minio.domain.RetentionDomain;
import io.minio.SetObjectRetentionArgs;
import io.minio.messages.Retention;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.core.convert.converter.Converter;

import java.io.Serial;

/**
 * <p> SetObjectRetentionRequest </p>
 *
 * @author goya
 * @since 2023/4/18 16:03
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(name = "设置对象保留请求参数实体", title = "设置对象保留请求参数实体")
public class SetObjectRetentionRequest extends ObjectVersionRequest<SetObjectRetentionArgs.Builder, SetObjectRetentionArgs> {

    @Serial
    private static final long serialVersionUID = 8665141280701241403L;
    private final Converter<RetentionDomain, Retention> toRetention = new DomainToRetentionConverter();

    @Schema(name = "保留配置", requiredMode = Schema.RequiredMode.REQUIRED, description = "既然是设置操作那么设置值就不能为空")
    private RetentionDomain retention;

    @Schema(name = "使用Governance模式")
    private Boolean bypassGovernanceMode;

    @Override
    public void prepare(SetObjectRetentionArgs.Builder builder) {
        builder.config(toRetention.convert(getRetention()));

        if (ObjectUtils.isNotEmpty(getBypassGovernanceMode())) {
            builder.bypassGovernanceMode(getBypassGovernanceMode());
        }
        super.prepare(builder);
    }

    @Override
    public SetObjectRetentionArgs.Builder getBuilder() {
        return SetObjectRetentionArgs.builder();
    }
}
