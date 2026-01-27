package com.ysmjjsy.goya.component.oss.minio.domain;

import com.ysmjjsy.goya.component.oss.minio.domain.base.BaseRetentionDomain;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * <p>对象保留域对象</p>
 *
 * @author goya
 * @since 2025/11/1 15:51
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(name = "对象保留设置域对象")
public class RetentionDomain extends BaseRetentionDomain {

    @Serial
    private static final long serialVersionUID = 6378903728723775033L;

    @Schema(name = "保留截止日期")
    private String retainUntilDate;

}
