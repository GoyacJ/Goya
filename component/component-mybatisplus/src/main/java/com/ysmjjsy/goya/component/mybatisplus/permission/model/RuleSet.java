package com.ysmjjsy.goya.component.mybatisplus.permission.model;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * <p>权限规则集</p>
 * <p>
 * 以 (tenantId, subjectId, resource) 为粒度加载。
 *
 * @author goya
 * @since 2026/1/28 22:29
 */
@Data
public class RuleSet implements Serializable {

    @Serial
    private static final long serialVersionUID = 5106322234813846319L;

    private String tenantId;
    private String subjectId;
    private String resource;
    private List<RuleDef> rules;
    private long version;
}
