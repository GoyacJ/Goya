package com.ysmjjsy.goya.component.oss.minio.definition;

import io.minio.BaseArgs;

/**
 * <p> Minio 参数构建器 </p>
 *
 * @author goya
 * @since 2022/7/1 23:49
 */
public interface MinioRequestBuilder<B extends BaseArgs.Builder<B, A>, A extends BaseArgs> extends MinioRequest {

    /**
     * 参数准备
     *
     * @param builder Minio 参数构造器
     */
    void prepare(B builder);

    /**
     * 获取Builder
     *
     * @return builder
     */
    B getBuilder();

    /**
     * 构建 Minio 参数对象
     *
     * @return Minio 参数对象
     */
    default A build() {
        B builder = getBuilder();
        prepare(builder);
        return builder.build();
    }
}
