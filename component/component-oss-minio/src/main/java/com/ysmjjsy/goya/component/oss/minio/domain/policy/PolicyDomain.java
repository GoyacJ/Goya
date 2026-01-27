package com.ysmjjsy.goya.component.oss.minio.domain.policy;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ysmjjsy.goya.component.framework.common.pojo.IEntity;
import lombok.Data;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>Minio 访问策略实体</p>
 *
 * @author goya
 * @since 2025/11/1 15:48
 */
@Data
public class PolicyDomain implements IEntity {

    @Serial
    private static final long serialVersionUID = -5312351652697703743L;

    @JsonProperty("Version")
    private String version = "2012-10-17";

    @JsonProperty("Statement")
    private List<StatementDomain> statements = new ArrayList<>();
}
