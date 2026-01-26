package com.ysmjjsy.goya.component.framework.core.pojo;

import com.fasterxml.jackson.core.type.TypeReference;
import com.ysmjjsy.goya.component.framework.common.pojo.VO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>page vo</p>
 *
 * @author goya
 * @since 2025/12/19 23:02
 */
@Data
public class PageVO<T extends Serializable> implements VO {

    @Serial
    private static final long serialVersionUID = -9083818121262969426L;

    @Schema(title = "总记录数")
    private Long totalCount;

    @Schema(title = "总页数")
    private Integer totalPages;

    @Schema(title = "每页记录数")
    private Integer pageSize;

    @Schema(title = "当前页码")
    private Integer pageIndex;

    @Schema(title = "当前页记录数")
    private Integer pageCount;

    @Schema(title = "数据")
    private Collection<T> data;

    // --------------------------------------------------------------------------------------------
    // 构造器 & 基础构建方法
    // --------------------------------------------------------------------------------------------

    public PageVO() {
    }

    public PageVO(long totalCount, int pageIndex, int pageSize, Collection<T> data) {
        this.totalCount = totalCount;
        this.pageIndex = pageIndex;
        this.pageSize = pageSize;
        this.data = data;
        this.pageCount = data != null ? data.size() : 0;
        this.calculateTotalPages();
    }

    public static <T extends Serializable> PageVO<T> build(PageQuery pageQuery) {
        PageVO<T> page = new PageVO<>();
        page.pageIndex = pageQuery.getPageIndex();
        page.pageSize = pageQuery.getPageSize();
        return page;
    }

    public static <T extends Serializable> PageVO<T> build(Collection<T> data) {
        return new PageVO<>(data.size(), 1, data.size(), data);
    }

    public static <T extends Serializable> PageVO<T> build(
            long totalCount,
            int totalPages,
            int pageSize,
            int pageIndex,
            int pageCount,
            Collection<T> data
    ) {
        PageVO<T> page = new PageVO<>();
        page.totalCount = totalCount;
        page.totalPages = totalPages;
        page.pageSize = pageSize;
        page.pageIndex = pageIndex;
        page.pageCount = pageCount;
        page.data = data;
        return page;
    }

    public static <T extends Serializable> PageVO<T> empty() {
        return new PageVO<>(0, 1, 0, List.of());
    }

    // --------------------------------------------------------------------------------------------
    // 基础工具方法
    // --------------------------------------------------------------------------------------------

    public boolean isEmpty() {
        return data == null || data.isEmpty();
    }

    public boolean isNotEmpty() {
        return !isEmpty();
    }

    public void normalize() {
        if (pageIndex == null || pageIndex < 1) {
            pageIndex = 1;
        }
        if (pageSize == null || pageSize < 1) {
            pageSize = 10;
        }
    }

    public void calculateTotalPages() {
        if (totalCount != null && pageSize != null && pageSize > 0) {
            this.totalPages = (int) ((totalCount + pageSize - 1) / pageSize);
        }
    }

    public PageVO<T> withData(Collection<T> data) {
        this.data = data;
        this.pageCount = data != null ? data.size() : 0;
        return this;
    }

    // --------------------------------------------------------------------------------------------
    // map 转换方法族（重点增强）
    // --------------------------------------------------------------------------------------------

    /**
     * 基础 map：转为新的 PageVO<D>，保留分页信息
     */
    public <D extends Serializable> PageVO<D> map(Function<T, D> convert) {
        PageVO<D> result = new PageVO<>();
        result.totalCount = this.totalCount;
        result.totalPages = this.totalPages;
        result.pageSize = this.pageSize;
        result.pageIndex = this.pageIndex;

        if (this.data != null) {
            List<D> mapped = this.data.stream().map(convert).toList();
            result.data = mapped;
            result.pageCount = mapped.size();
        }

        return result;
    }

    /**
     * mapList：只映射 data，不映射分页
     */
    public <D extends Serializable> List<D> mapList(Function<T, D> convert) {
        if (this.data == null) {
            return List.of();
        }
        return this.data.stream().map(convert).toList();
    }

    /**
     * mapToSet：转换为 Set
     */
    public <D extends Serializable> Set<D> mapToSet(Function<T, D> convert) {
        if (this.data == null) {
            return Set.of();
        }
        return this.data.stream().map(convert).collect(Collectors.toSet());
    }

    /**
     * mapToLinkedList：保持数据顺序
     */
    public <D extends Serializable> List<D> mapToLinkedList(Function<T, D> convert) {
        LinkedList<D> list = new LinkedList<>();
        if (this.data != null) {
            for (T item : this.data) {
                list.add(convert.apply(item));
            }
        }
        return list;
    }

    /**
     * mapToCollection：适配任何 Collection 类型
     */
    public <D extends Serializable, C extends Collection<D>> C mapToCollection(
            C target,
            Function<T, D> convert
    ) {
        if (this.data != null) {
            for (T item : this.data) {
                target.add(convert.apply(item));
            }
        }
        return target;
    }

    /**
     * mapFlat：一对多转换 (例如 User -> List<Role>)
     */
    public <D extends Serializable> PageVO<D> flatMap(Function<T, Collection<D>> converter) {
        PageVO<D> result = new PageVO<>();
        result.totalCount = this.totalCount;
        result.pageIndex = this.pageIndex;
        result.pageSize = this.pageSize;

        if (this.data != null) {
            List<D> mapped = this.data.stream()
                    .map(converter)
                    .filter(Objects::nonNull)
                    .flatMap(Collection::stream)
                    .toList();
            result.data = mapped;
            result.pageCount = mapped.size();
            result.calculateTotalPages();
        }

        return result;
    }

    /**
     * mapWithIndex：映射时带序号
     */
    public <D extends Serializable> PageVO<D> mapWithIndex(BiFunctionWithIndex<T, D> convert) {
        PageVO<D> result = new PageVO<>();
        result.totalCount = this.totalCount;
        result.totalPages = this.totalPages;
        result.pageSize = this.pageSize;
        result.pageIndex = this.pageIndex;

        if (this.data != null) {
            int index = 0;
            List<D> list = new ArrayList<>();

            for (T item : this.data) {
                list.add(convert.apply(item, index++));
            }

            result.data = list;
            result.pageCount = list.size();
        }

        return result;
    }

    public interface BiFunctionWithIndex<T, R> {
        R apply(T value, int index);
    }

    public static <D extends Serializable> TypeReference<PageVO<D>> of(Class<D> clazz) {
        return new TypeReference<>() {
        };
    }
}
