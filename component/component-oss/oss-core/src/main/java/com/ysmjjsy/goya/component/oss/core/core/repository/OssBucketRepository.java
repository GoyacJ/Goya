package com.ysmjjsy.goya.component.oss.core.core.repository;

import com.ysmjjsy.goya.component.oss.core.arguments.bucket.CreateBucketArguments;
import com.ysmjjsy.goya.component.oss.core.arguments.bucket.DeleteBucketArguments;
import com.ysmjjsy.goya.component.oss.core.domain.bucket.BucketDomain;

import java.util.List;

/**
 * <p>Java OSS API 存储桶操作抽象定义</p>
 *
 * @author goya
 * @since 2025/11/1 14:23
 */
public interface OssBucketRepository {

    /**
     * 检查指定的存储桶是否存在。使用此方法可以确定指定的存储桶名称是否已经存在，因此不能用于创建新的存储桶
     *
     * @param bucketName 存储桶名称
     * @return 如果指定名称的存储桶存在，则该值为true；如果指定名称的存储桶不存在，则值为false
     */
    boolean doesBucketExist(String bucketName);

    /**
     * 返回当前帐户的所有存储桶实例列表
     *
     * @return 存储桶 {@link BucketDomain} 列表
     */
    List<BucketDomain> listBuckets();

    /**
     * 创建存储桶实例
     * <p>
     * 说明：Minio 的 createBucket 方法没有返回值
     *
     * @param bucketName 存储桶名称
     * @return 存储桶信息 {@link BucketDomain}
     */
    BucketDomain createBucket(String bucketName);

    /**
     * 创建存储桶实例
     * <p>
     * 说明：Minio 的 createBucket 方法没有返回值
     *
     * @param arguments 参数实体 {@link CreateBucketArguments}
     * @return 存储桶信息 {@link BucketDomain}
     */
    BucketDomain createBucket(CreateBucketArguments arguments);

    /**
     * 删除存储桶实例
     * <p>
     * 说明：Aliyun 的 deleteBucket 方法会返回一个 VoidResult。目前用不到，等用到时再重构
     *
     * @param bucketName 存储桶名称
     */
    void deleteBucket(String bucketName);

    /**
     * 删除存储桶实例
     * <p>
     * 说明：Aliyun 的 deleteBucket 方法会返回一个 VoidResult。目前用不到，等用到时再重构
     *
     * @param arguments 参数实体 {@link DeleteBucketArguments}
     */
    void deleteBucket(DeleteBucketArguments arguments);
}
