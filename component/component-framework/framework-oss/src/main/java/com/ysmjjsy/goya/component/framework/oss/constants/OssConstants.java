package com.ysmjjsy.goya.component.framework.oss.constants;


import com.ysmjjsy.goya.component.core.enums.RegexPoolEnum;
import com.ysmjjsy.goya.component.framework.constants.PropertyConst;

/**
 * <p></p>
 *
 * @author goya
 * @since 2025/11/1 14:38
 */
public interface OssConstants {

    // allowed maximum object size is 5TiB.
    /**
     * 允许的对象大小，最大为 5T
     */
    long MAX_OBJECT_SIZE = 5L * 1024 * 1024 * 1024 * 1024;
    // allowed minimum part size is 5MiB in multipart upload.
    /**
     * 分片上传中，允许的分片大小最小为 5M
     */
    int MIN_MULTIPART_SIZE = 5 * 1024 * 1024;
    /**
     * 分片上传中，允许的分片大小最大为 5G
     */
    long MAX_PART_SIZE = 5L * 1024 * 1024 * 1024;
    /**
     * 分片上传中，允许的最大分片数量为 1000
     */
    int MAX_MULTIPART_COUNT = 10000;

    String PROPERTY_PREFIX_OSS = PropertyConst.PROPERTY_GOYA + ".oss";
    String PROPERTY_PREFIX_OSS_TYPE = PROPERTY_PREFIX_OSS + ".type";

    String PROPERTY_OSS_MINIO = PROPERTY_PREFIX_OSS + ".minio";
    String PROPERTY_OSS_S3 = PROPERTY_PREFIX_OSS + ".s3";
    String PROPERTY_OSS_ALIYUN = PROPERTY_PREFIX_OSS + ".aliyun";

    String ITEM_OSS_DIALECT = PROPERTY_PREFIX_OSS + ".dialect";

    String PROPERTY_OSS_PROXY = PROPERTY_PREFIX_OSS + ".proxy";

    String OSS_MULTIPART_UPLOAD_REQUEST_MAPPING = "/oss/multipart-upload";
    String OSS_PRESIGNED_OBJECT_REQUEST_MAPPING = "/presigned";
    String OSS_PRESIGNED_OBJECT_PROXY_REQUEST_MAPPING = OSS_PRESIGNED_OBJECT_REQUEST_MAPPING + "/*/*";
    String PRESIGNED_OBJECT_URL_PROXY = OSS_MULTIPART_UPLOAD_REQUEST_MAPPING + OSS_PRESIGNED_OBJECT_REQUEST_MAPPING;

    /**
     * @see RegexPoolEnum#DNS_COMPATIBLE
     */
    String DNS_COMPATIBLE = "^[a-z0-9][a-z0-9\\.\\-]+[a-z0-9]$";
}
