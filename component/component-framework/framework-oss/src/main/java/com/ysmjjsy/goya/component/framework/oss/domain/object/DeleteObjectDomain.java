package com.ysmjjsy.goya.component.framework.oss.domain.object;

import com.ysmjjsy.goya.component.oss.core.arguments.object.DeletedObjectArguments;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * <p>批量删除对象中的结果对象</p>
 *
 * @author goya
 * @since 2025/11/1 14:32
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class DeleteObjectDomain extends DeletedObjectArguments {

    @Serial
    private static final long serialVersionUID = 86401786120877190L;

    public DeleteObjectDomain() {
        super();
    }

    public DeleteObjectDomain(String objectName) {
        super(objectName);
    }
}