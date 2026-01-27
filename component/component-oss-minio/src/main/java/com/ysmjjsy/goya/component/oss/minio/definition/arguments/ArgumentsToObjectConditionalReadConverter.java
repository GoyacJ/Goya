package com.ysmjjsy.goya.component.oss.minio.definition.arguments;

import com.ysmjjsy.goya.component.framework.common.constants.SymbolConst;
import com.ysmjjsy.goya.component.framework.common.utils.GoyaDateUtils;
import com.ysmjjsy.goya.component.framework.oss.arguments.base.ObjectConditionalReadArguments;
import io.minio.ObjectConditionalReadArgs;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>统一定义对象条件请求参数转换为 Minio 参数转换器</p>
 *
 * @author goya
 * @since 2025/11/1 15:56
 */
public abstract class ArgumentsToObjectConditionalReadConverter<S extends ObjectConditionalReadArguments, T extends ObjectConditionalReadArgs, B extends ObjectConditionalReadArgs.Builder<B, T>> extends ArgumentsToObjectReadConverter<S, T, B> {

    @Override
    public void prepare(S arguments, B builder) {

        if (ObjectUtils.isNotEmpty(arguments.getLength()) && arguments.getLength() >= 0) {
            builder.length(arguments.getLength());
        }

        if (ObjectUtils.isNotEmpty(arguments.getOffset()) && arguments.getOffset() >= 0) {
            builder.offset(arguments.getOffset());
        }

        if (CollectionUtils.isNotEmpty(arguments.getMatchEtag())) {
            builder.matchETag(StringUtils.join(arguments.getMatchEtag(), SymbolConst.COMMA));
        }

        if (CollectionUtils.isNotEmpty(arguments.getNotMatchEtag())) {
            builder.notMatchETag(StringUtils.join(arguments.getNotMatchEtag(), SymbolConst.COMMA));
        }

        if (ObjectUtils.isNotEmpty(arguments.getModifiedSince())) {
            builder.modifiedSince(GoyaDateUtils.dateToZonedDateTime(arguments.getModifiedSince()));
        }

        if (ObjectUtils.isNotEmpty(arguments.getUnmodifiedSince())) {
            builder.unmodifiedSince(GoyaDateUtils.dateToZonedDateTime(arguments.getUnmodifiedSince()));
        }

        super.prepare(arguments, builder);
    }
}
