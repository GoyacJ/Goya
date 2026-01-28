package com.ysmjjsy.goya.component.mybatisplus.permission.model;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * <p>权限谓词定义（结构化）</p>
 * <p>
 * 表示一个可编译为 SQL 条件的最小单元。
 *
 * <p><b>示例：</b>
 * <pre>
 * fieldKey = "deptId"
 * type = IN
 * values = ["${deptIds}"]
 * </pre>
 *
 * @author goya
 * @since 2026/1/28 22:28
 */
@Data
public class PredicateDef implements Serializable {

    @Serial
    private static final long serialVersionUID = -4263420403215314354L;

    private String fieldKey;
    private PredicateType type;
    private List<Serializable> values;

    public void validate() {
        Objects.requireNonNull(fieldKey, "fieldKey 不能为空");
        Objects.requireNonNull(type, "type 不能为空");
        Objects.requireNonNull(values, "values 不能为空");
    }
}