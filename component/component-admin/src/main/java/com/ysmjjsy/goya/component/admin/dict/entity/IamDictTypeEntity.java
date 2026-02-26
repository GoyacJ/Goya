package com.ysmjjsy.goya.component.admin.dict.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ysmjjsy.goya.component.mybatisplus.definition.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * <p>字典类型实体</p>
 *
 * @author goya
 * @since 2026/2/26
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("iam_dict_type")
public class IamDictTypeEntity extends BaseEntity {

    @Serial
    private static final long serialVersionUID = -4657493609946084599L;

    @TableField("dict_type_code")
    private String dictTypeCode;

    @TableField("dict_type_name")
    private String dictTypeName;

    @TableField("status")
    private String status;

    @TableField("remark")
    private String remark;
}
