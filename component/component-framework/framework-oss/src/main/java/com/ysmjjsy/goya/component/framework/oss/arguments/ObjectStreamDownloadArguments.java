package com.ysmjjsy.goya.component.framework.oss.arguments;

import com.ysmjjsy.goya.component.framework.oss.arguments.base.ObjectArguments;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * <p>对象流式下载请求参数实体</p>
 *
 * @author goya
 * @since 2025/11/3 09:44
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ObjectStreamDownloadArguments extends ObjectArguments {
    @Serial
    private static final long serialVersionUID = -2523343858716700087L;
}

