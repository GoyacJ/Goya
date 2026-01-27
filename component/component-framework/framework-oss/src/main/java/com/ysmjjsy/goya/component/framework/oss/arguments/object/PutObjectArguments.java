package com.ysmjjsy.goya.component.framework.oss.arguments.object;

import com.ysmjjsy.goya.component.oss.core.arguments.base.PutObjectBaseArguments;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.InputStream;
import java.io.Serial;

/**
 * <p>上传对象请求参数实体</p>
 *
 * @author goya
 * @since 2025/11/1 14:52
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class PutObjectArguments extends PutObjectBaseArguments {

    @Serial
    private static final long serialVersionUID = -9102952259500170232L;

    private InputStream inputStream;
}
