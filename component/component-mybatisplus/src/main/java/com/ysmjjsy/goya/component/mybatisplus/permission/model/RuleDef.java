package com.ysmjjsy.goya.component.mybatisplus.permission.model;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * <p>权限规则定义</p>
 * <p>
 * 一条规则由多个谓词组成，并通过 AND / OR 组合。
 *
 * @author goya
 * @since 2026/1/28 22:28
 */
@Data
public class RuleDef implements Serializable {

    @Serial
    private static final long serialVersionUID = -3858510627545707274L;

    private List<PredicateDef> predicates;
    private CombineType combine = CombineType.AND;
    private int priority;
}
