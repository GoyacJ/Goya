package com.ysmjjsy.goya.component.oss.core.properties;

import com.ysmjjsy.goya.component.core.pool.Pool;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * <p>对象存储通用属性提取抽象类</p>
 *
 * @author goya
 * @since 2025/11/1 16:05
 */
@Data
public abstract class AbstractOssProperties implements Serializable {

    @Serial
    private static final long serialVersionUID = -5672374309223310533L;

    /**
     * Oss Server endpoint
     */
    private String endpoint;

    /**
     * Oss Server accessKey
     */
    private String accessKey;

    /**
     * Oss Server secretKey
     */
    private String secretKey;

    /**
     * 自定义 OSS 对象池参数配置
     */
    private Pool pool = new Pool();
}
