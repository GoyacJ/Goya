package com.ysmjjsy.goya.component.framework.oss.core.repository;

import com.ysmjjsy.goya.component.oss.core.arguments.object.*;
import com.ysmjjsy.goya.component.oss.core.domain.base.ObjectWriteDomain;
import com.ysmjjsy.goya.component.oss.core.domain.object.*;
import com.ysmjjsy.goya.component.oss.core.enums.HttpMethodEnum;
import org.apache.commons.lang3.StringUtils;

import java.time.Duration;
import java.util.List;

/**
 * <p>Java OSS API 对象操作抽象定义</p>
 *
 * @author goya
 * @since 2025/11/1 14:23
 */
public interface OssObjectRepository {

    /**
     * 根据存储桶名称获取对象列表
     *
     * @param bucketName 存储桶名称
     * @return 对象列表结果 {@link ListObjectsDomain}
     */
    default ListObjectsDomain listObjects(String bucketName) {
        return listObjects(bucketName, null);
    }

    /**
     * 根据存储桶名称和前缀获取对象列表
     *
     * @param bucketName 存储桶名
     * @param prefix     前缀
     * @return 对象列表结果 {@link ListObjectsDomain}
     */
    default ListObjectsDomain listObjects(String bucketName, String prefix) {
        ListObjectsArguments arguments = new ListObjectsArguments();
        arguments.setBucketName(bucketName);
        if (StringUtils.isNotBlank(prefix)) {
            arguments.setPrefix(prefix);
        }

        return listObjects(arguments);
    }

    /**
     * 获取对象列表
     *
     * @param arguments 对象列表请求参数 {@link ListObjectsArguments}
     * @return 对象列表结果 {@link ListObjectsDomain}
     */
    ListObjectsDomain listObjects(ListObjectsArguments arguments);

    /**
     * 根据存储桶名称和前缀获取对象列表V2
     *
     * @param bucketName 存储桶名
     * @return 对象列表结果 {@link ListObjectsDomain}
     */
    default ListObjectsV2Domain listObjectsV2(String bucketName) {
        return listObjectsV2(bucketName, null);
    }

    /**
     * 根据存储桶名称和前缀获取对象列表V2
     *
     * @param bucketName 存储桶名
     * @param prefix     前缀
     * @return 对象列表结果 {@link ListObjectsV2Domain}
     */
    default ListObjectsV2Domain listObjectsV2(String bucketName, String prefix) {
        ListObjectsV2Arguments arguments = new ListObjectsV2Arguments();
        arguments.setBucketName(bucketName);
        if (StringUtils.isNotBlank(prefix)) {
            arguments.setPrefix(prefix);
        }

        return listObjectsV2(arguments);
    }

    /**
     * 获取对象列表V2
     *
     * @param arguments 对象列表请求参数 {@link ListObjectsV2Arguments}
     * @return 对象列表结果 {@link ListObjectsV2Domain}
     */
    ListObjectsV2Domain listObjectsV2(ListObjectsV2Arguments arguments);

    /**
     * 删除一个对象
     *
     * @param bucketName 存储桶名称
     * @param objectName 对象名称
     */
    default void deleteObject(String bucketName, String objectName) {
        DeleteObjectArguments arguments = new DeleteObjectArguments();
        arguments.setBucketName(bucketName);
        arguments.setObjectName(objectName);
        deleteObject(arguments);
    }

    /**
     * 删除一个对象
     *
     * @param arguments 删除对象请求参数 {@link DeleteObjectArguments}
     */
    void deleteObject(DeleteObjectArguments arguments);

    /**
     * 批量删除对象
     *
     * @param arguments 批量删除对象请求参数 {@link DeleteObjectsArguments}
     * @return 批量删除对象结果对象
     */
    List<DeleteObjectDomain> deleteObjects(DeleteObjectsArguments arguments);

    /**
     * 获取对象元信息
     *
     * @param bucketName 存储桶名称
     * @param objectName 对象名称
     * @return 对象元信息结果对象 {@link ObjectMetadataDomain}
     */
    default ObjectMetadataDomain getObjectMetadata(String bucketName, String objectName) {
        GetObjectMetadataArguments arguments = new GetObjectMetadataArguments();
        arguments.setBucketName(bucketName);
        arguments.setObjectName(objectName);
        return getObjectMetadata(arguments);
    }

    /**
     * 获取对象元信息
     *
     * @param arguments 获取对象元信息 {@link DeleteObjectsArguments}
     * @return 对象元信息结果对象 {@link ObjectMetadataDomain}
     */
    ObjectMetadataDomain getObjectMetadata(GetObjectMetadataArguments arguments);

    /**
     * 获取（下载）对象
     *
     * @param bucketName 存储桶名称
     * @param objectName 对象名称
     * @return 获取（下载）对象结果域对象 {@link GetObjectDomain}
     */
    default GetObjectDomain getObject(String bucketName, String objectName) {
        GetObjectArguments arguments = new GetObjectArguments();
        arguments.setBucketName(bucketName);
        arguments.setObjectName(objectName);
        return getObject(arguments);
    }

    /**
     * 获取（下载）对象
     *
     * @param arguments 获取（下载）对象请求参数实体 {@link GetObjectArguments}
     * @return 获取（下载）对象结果域对象 {@link GetObjectDomain}
     */
    GetObjectDomain getObject(GetObjectArguments arguments);

    /**
     * 放置（上传）对象
     *
     * @param arguments 放置（上传）对象请求参数实体 {@link PutObjectArguments}
     * @return 放置（上传）对象结果域对象 {@link PutObjectDomain}
     */
    PutObjectDomain putObject(PutObjectArguments arguments);


    /**
     * 创建预签名 URL
     *
     * @param bucketName 存储桶名称
     * @param objectName 对象名称
     * @param expiration 中止时间
     * @return 预签名地址
     */
    default String generatePresignedUrl(String bucketName, String objectName, Duration expiration) {
        return generatePresignedUrl(bucketName, objectName, expiration, HttpMethodEnum.GET);
    }


    /**
     * 创建预签名 URL
     *
     * @param bucketName 存储桶名称
     * @param objectName 对象名称
     * @param expiration 中止时间
     * @param method     http 请求类型
     * @return 预签名地址
     */
    default String generatePresignedUrl(String bucketName, String objectName, Duration expiration, HttpMethodEnum method) {
        GeneratePresignedUrlArguments arguments = new GeneratePresignedUrlArguments();
        arguments.setBucketName(bucketName);
        arguments.setObjectName(objectName);
        arguments.setExpiration(expiration);
        arguments.setMethod(method);
        return generatePresignedUrl(arguments);
    }

    /**
     * 创建预签名 URL
     *
     * @param arguments 创建预签名 URL 请求参数 {@link GeneratePresignedUrlArguments}
     * @return 预签名地址
     */
    String generatePresignedUrl(GeneratePresignedUrlArguments arguments);

    /**
     * 下载对象
     * <p>
     * 该方法与<code>getObject</code>不同，该方法要指明具体的文件{@link java.io.File} 或者文件名。这就意味着可以把该方法理解为服务端下载操作。
     *
     * @param arguments 下载对象请求参数实体 {@link DownloadObjectArguments}
     * @return 下载对象结果域对象 {@link ObjectMetadataDomain}
     */
    ObjectMetadataDomain download(DownloadObjectArguments arguments);

    /**
     * 上传对象
     * <p>
     * 该方法与<code>putObject</code>不同，该方法要指明具体的文件{@link java.io.File} 或者文件名。这就意味着可以把该方法理解为服务端下载操作
     *
     * @param arguments 下载对象请求参数实体 {@link UploadObjectArguments}
     * @return 下载对象结果域对象 {@link ObjectWriteDomain}
     */
    ObjectWriteDomain upload(UploadObjectArguments arguments);
}
