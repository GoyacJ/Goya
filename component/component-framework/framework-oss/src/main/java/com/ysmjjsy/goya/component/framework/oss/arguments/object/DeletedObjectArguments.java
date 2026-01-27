package com.ysmjjsy.goya.component.framework.oss.arguments.object;

import com.ysmjjsy.goya.component.oss.core.core.arguments.OssArguments;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;

/**
 * <p>删除对象参数</p>
 *
 * @author goya
 * @since 2025/11/1 14:49
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeletedObjectArguments implements OssArguments {

    @Serial
    private static final long serialVersionUID = -7553683410183204179L;

    @Schema(name = "对象名称")
    private String objectName;

    @Schema(name = "对象版本ID")
    private String versionId;

    public DeletedObjectArguments(String objectName) {
        this.objectName = objectName;
    }
}
