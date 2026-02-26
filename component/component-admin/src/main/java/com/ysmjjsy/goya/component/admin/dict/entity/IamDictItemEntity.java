package com.ysmjjsy.goya.component.admin.dict.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ysmjjsy.goya.component.mybatisplus.definition.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * <p>字典条目实体</p>
 *
 * @author goya
 * @since 2026/2/26
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("iam_dict_item")
public class IamDictItemEntity extends BaseEntity {

    @Serial
    private static final long serialVersionUID = -7085095546818315262L;

    @TableField("dict_type_code")
    private String dictTypeCode;

    @TableField("item_code")
    private String itemCode;

    @TableField("item_label")
    private String itemLabel;

    @TableField("item_value")
    private String itemValue;

    @TableField("sort")
    private Integer sort;

    @TableField("status")
    private String status;

    @TableField("remark")
    private String remark;
}
