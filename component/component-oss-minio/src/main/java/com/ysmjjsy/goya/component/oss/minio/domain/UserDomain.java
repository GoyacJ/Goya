package com.ysmjjsy.goya.component.oss.minio.domain;

import com.ysmjjsy.goya.component.framework.common.pojo.IEntity;
import io.minio.admin.Status;
import lombok.Data;

import java.io.Serial;
import java.util.List;

/**
 * <p>Minio User Domain</p>
 *
 * @author goya
 * @since 2025/11/1 15:53
 */
@Data
public class UserDomain implements IEntity {

    @Serial
    private static final long serialVersionUID = 508428246501664929L;

    private String accessKey;

    private String secretKey;

    private String policyName;

    private List<String> memberOf;

    private Status status;
}
