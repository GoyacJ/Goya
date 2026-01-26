package com.ysmjjsy.goya.component.oss.minio.domain;

import com.ysmjjsy.goya.component.core.pojo.IEntity;
import lombok.Data;

import java.io.Serial;

/**
 * <p>Minio VersioningConfiguration 对应 Domain Object</p>
 *
 * @author goya
 * @since 2025/11/1 15:54
 */
@Data
public class VersioningConfigurationDomain implements IEntity {

    @Serial
    private static final long serialVersionUID = -6139693750079156280L;
    
    private String status;

    private Boolean mfaDelete;
}
