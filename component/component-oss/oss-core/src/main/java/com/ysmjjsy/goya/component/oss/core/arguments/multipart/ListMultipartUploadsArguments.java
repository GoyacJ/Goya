package com.ysmjjsy.goya.component.oss.core.arguments.multipart;

import com.ysmjjsy.goya.component.oss.core.arguments.base.BucketArguments;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * <p>分片上传列表请求参数实体</p>
 *
 * @author goya
 * @since 2025/11/1 14:47
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(name = "分片上传列表请求参数实体", title = "分片上传列表请求参数实体")
public class ListMultipartUploadsArguments extends BucketArguments {

    @Serial
    private static final long serialVersionUID = 8139560749048241308L;

    @Schema(name = "分隔符")
    private String delimiter;

    @Schema(name = "前缀")
    private String prefix;

    @Schema(name = "要返回的最大上传次数")
    private Integer maxUploads;

    /**
     * 指示结果中开始列出的位置的关键标记.
     * <p>
     * 与上传ID标记一起，指定列表开始后的多部分上传.
     * <p>
     * 如果未指定 uploadId 标记，则列表中只会包括按字典顺序大于指定对象标记的对象.
     * <p>
     * 如果指定了 uploadId 标记，则对于等于对象标记的密钥的任何多部分上传也可以包括在内，前提是这些分片上传的uploadIdD在字典上大于指定的标记.
     */
    @Schema(name = "指示结果中开始列出的位置的关键标记")
    private String keyMarker;

    /**
     * 指示结果中开始列出的位置的上载ID标记
     */
    @Schema(name = "指示结果中开始列出的位置的上载ID标记")
    private String uploadIdMarker;

    /**
     * 可选参数，指示要应用于响应的编码方法。
     * 对象键可以包含任何Unicode字符；但是，XML1.0解析器无法解析某些字符，例如ASCII值为0到10的字符。
     * 对于XML1.0中不支持的字符，可以添加此参数来请求AmazonS3对响应中的密钥进行编码.
     */
    @Schema(name = "应用于响应的编码方法")
    private String encodingType;
}
