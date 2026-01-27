package com.ysmjjsy.goya.component.oss.minio.domain.policy;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;
import com.ysmjjsy.goya.component.framework.common.pojo.IEntity;
import lombok.Data;

import java.io.Serial;
import java.util.List;

/**
 * <p>Minio 策略 PrincipalDomain</p>
 *
 * @author goya
 * @since 2025/11/1 15:48
 */
@Data
public class PrincipalDomain implements IEntity {

    @Serial
    private static final long serialVersionUID = 2220153035220620143L;

    @JsonProperty("AWS")
    private List<String> aws = Lists.newArrayList("*");

}
