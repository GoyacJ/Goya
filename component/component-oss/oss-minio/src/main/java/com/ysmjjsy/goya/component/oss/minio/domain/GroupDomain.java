package com.ysmjjsy.goya.component.oss.minio.domain;

import com.ysmjjsy.goya.component.core.pojo.IEntity;
import io.minio.admin.Status;
import lombok.Data;

import java.io.Serial;
import java.util.List;

/**
 * <p>Minio Group Domain</p>
 *
 * @author goya
 * @since 2025/11/1 15:50
 */
@Data
public class GroupDomain implements IEntity {

    @Serial
    private static final long serialVersionUID = 7240377773697092444L;

    private String name;

    private Status status;

    private List<String> members;

    private String policy;
}
