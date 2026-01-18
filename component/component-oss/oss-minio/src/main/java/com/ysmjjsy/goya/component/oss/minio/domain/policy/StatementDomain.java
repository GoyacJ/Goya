package com.ysmjjsy.goya.component.oss.minio.domain.policy;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ysmjjsy.goya.component.core.pojo.IEntity;
import lombok.Data;

import java.io.Serial;
import java.util.List;

/**
 * <p>Minio 策略 StatementDomain</p>
 *
 * @author goya
 * @since 2025/11/1 15:49
 */
@Data
public class StatementDomain implements IEntity {

    @Serial
    private static final long serialVersionUID = -8253695493237063675L;

    @JsonProperty("Effect")
    private String effect = "Allow";

    @JsonProperty("Action")
    private List<String> actions;

    @JsonProperty("Resource")
    private List<String> resources;

    @JsonProperty("Principal")
    private PrincipalDomain principal = new PrincipalDomain();
}
