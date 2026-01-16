package com.ysmjjsy.goya.component.log.enums;

import com.ysmjjsy.goya.component.core.enums.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <p></p>
 *
 * @author goya
 * @since 2025/10/8 21:09
 */
@Getter
@AllArgsConstructor
public enum OperatorTypeEnum implements IEnum<Integer> {

    /**
     * 其它
     */
    OTHER(0,"其它"),

    /**
     * 新增
     */
    INSERT(1,"新增"),

    /**
     * 修改
     */
    UPDATE(2,"修改"),

    /**
     * 删除
     */
    DELETE(3,"删除"),

    /**
     * 授权
     */
    GRANT(4,"授权"),

    /**
     * 导出
     */
    EXPORT(5,"导出"),

    /**
     * 导入
     */
    IMPORT(6,"导入"),

    /**
     * 强退
     */
    FORCE(7,"强退"),

    /**
     * 生成代码
     */
    GENCODE(8,"生成代码"),

    /**
     * 清空数据
     */
    CLEAN(9,"清空数据"),
    ;

    private final Integer code;
    private final String description;
}
