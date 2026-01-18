package com.ysmjjsy.goya.component.oss.core.arguments.object;

import com.ysmjjsy.goya.component.oss.core.arguments.base.ObjectVersionArguments;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * <p>删除对象请求参数实体</p>
 *
 * @author goya
 * @since 2025/11/1 14:49
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(name = "删除对象请求参数实体", title = "删除对象请求参数实体")
public class DeleteObjectArguments extends ObjectVersionArguments {

    @Serial
    private static final long serialVersionUID = -1608012400936779079L;

    @Schema(name = "使用治理模式进行删除", description = "治理模式用户不能覆盖或删除对象版本或更改其锁定设置，可通过设置该参数进行强制操作")
    private Boolean bypassGovernanceMode;
}
