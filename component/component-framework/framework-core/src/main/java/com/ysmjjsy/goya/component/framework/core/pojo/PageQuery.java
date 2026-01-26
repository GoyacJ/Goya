package com.ysmjjsy.goya.component.framework.core.pojo;

import com.ysmjjsy.goya.component.framework.common.pojo.DTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.util.List;

/**
 * <p>page query</p>
 *
 * @author goya
 * @since 2025/12/19 22:47
 */
@Data
public class PageQuery implements DTO {

    @Serial
    private static final long serialVersionUID = 6799106609109018666L;

    /**
     * 默认每页记录数
     */
    private static final int DEFAULT_PAGE_SIZE = 10;

    /**
     * 每页记录数
     */
    @NotNull(message = "{page.size.notNull}")
    @Min(value = 1, message = "{page.size.min}")
    @Max(value = 1000, message = "{page.size.max}")
    @Schema(description = "每页数据条数", type = "integer", minimum = "0", maximum = "1000", defaultValue = "10")
    private Integer pageSize = DEFAULT_PAGE_SIZE;

    /**
     * 当前页码
     */
    @NotNull(message = "{page.index.notNull}")
    @Min(value = 0, message = "{page.index.min}")
    @Schema(description = "页码", type = "integer", minimum = "1", defaultValue = "1")
    private Integer pageIndex = 1;

    /**
     * 多字段排序，支持：["createdTime,desc", "name,asc"]
     */
    @Schema(description = "排序字段，格式：字段名=方向（asc/desc）")
    private List<String> sortOrders = List.of("createdTime=desc");
}
