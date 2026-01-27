package com.ysmjjsy.goya.component.framework.oss.domain.base;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * <p>基础分片域对象</p>
 *
 * @author goya
 * @since 2025/11/1 14:27
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class PartDomain extends BasePartDomain {

    @Serial
    private static final long serialVersionUID = 8401367364763237310L;

    /**
     * 此分片的大小，以字节为单位
     */
    @Schema(name = "分片数据大小", description = "单位为字节")
    private long partSize;

    @Schema(name = "新对象的上次修改日期")
    private LocalDateTime lastModifiedDate;
}
