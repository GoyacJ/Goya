package com.ysmjjsy.goya.component.oss.minio.bo;

import com.ysmjjsy.goya.component.framework.common.pojo.IEntity;
import com.ysmjjsy.goya.component.oss.minio.enums.RetentionModeEnums;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.util.Map;

/**
 * <p> 存储桶基础信息返回实体 </p>
 *
 * @author goya
 * @since 2023/6/11 10:02
 */
@Data
public class ObjectSettingBusiness implements IEntity {

    @Serial
    private static final long serialVersionUID = 5326809030217413794L;
    
    @Schema(name = "标签")
    private Map<String, String> tags;
    @Schema(name = "保留模式")
    private RetentionModeEnums retentionMode;
    @Schema(name = "保留截止日期")
    private String retentionRetainUntilDate;
    @Schema(name = "是否合规持有")
    private Boolean legalHold;
    @Schema(name = "是否标记删除")
    private Boolean deleteMarker;
    @Schema(name = "ETag")
    private String etag;
    @Schema(name = "最后修改时间")
    private String lastModified;
    @Schema(name = "对象大小")
    private Long size;
    @Schema(name = "用户自定义元数据")
    private Map<String, String> userMetadata;
}
