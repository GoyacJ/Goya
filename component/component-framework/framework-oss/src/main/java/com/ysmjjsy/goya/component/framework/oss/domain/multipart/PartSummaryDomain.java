package com.ysmjjsy.goya.component.framework.oss.domain.multipart;

import com.ysmjjsy.goya.component.framework.oss.domain.base.PartDomain;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * <p>分片详情</p>
 *
 * @author goya
 * @since 2025/11/1 14:31
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class PartSummaryDomain extends PartDomain {
    @Serial
    private static final long serialVersionUID = -9116855786948033286L;
}
