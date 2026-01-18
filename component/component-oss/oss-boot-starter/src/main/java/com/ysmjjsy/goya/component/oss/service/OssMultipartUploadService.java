package com.ysmjjsy.goya.component.oss.service;

import com.ysmjjsy.goya.component.oss.business.CreateMultipartUploadBusiness;
import com.ysmjjsy.goya.component.oss.configuration.properties.OssProperties;
import com.ysmjjsy.goya.component.oss.core.arguments.object.GeneratePresignedUrlArguments;
import com.ysmjjsy.goya.component.oss.core.core.repository.OssMultipartUploadRepository;
import com.ysmjjsy.goya.component.oss.core.core.repository.OssObjectRepository;
import com.ysmjjsy.goya.component.oss.core.domain.base.ObjectWriteDomain;
import com.ysmjjsy.goya.component.oss.core.domain.multipart.CompleteMultipartUploadDomain;
import com.ysmjjsy.goya.component.oss.core.domain.multipart.ListPartsDomain;
import com.ysmjjsy.goya.component.oss.core.domain.multipart.PartSummaryDomain;
import com.ysmjjsy.goya.component.oss.core.enums.HttpMethodEnum;
import com.ysmjjsy.goya.component.oss.proxy.OssProxyAddressConverter;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>对象存储分片上传</p>
 *
 * @author goya
 * @since 2025/11/3 09:41
 */
@Service
public class OssMultipartUploadService {

    private final OssObjectRepository ossObjectRepository;
    private final OssMultipartUploadRepository ossMultipartUploadRepository;
    private final Converter<String, String> converter;

    public OssMultipartUploadService(OssObjectRepository ossObjectRepository, OssMultipartUploadRepository ossMultipartUploadRepository, OssProperties ossProperties) {
        this.ossObjectRepository = ossObjectRepository;
        this.ossMultipartUploadRepository = ossMultipartUploadRepository;
        this.converter = new OssProxyAddressConverter(ossProperties);
    }

    /**
     * 第一步：创建分片上传请求, 返回 UploadId
     *
     * @param bucketName 存储桶名称
     * @param objectName 对象名称
     * @return 大文件分片上传 UploadId
     */
    private String createUploadId(String bucketName, String objectName) {
        return ossMultipartUploadRepository.initiateMultipartUpload(bucketName, objectName);
    }

    /**
     * 第二步：创建文件预上传地址
     *
     * @param bucketName 存储桶名称
     * @param objectName 对象名称
     * @param uploadId   第一步中创建的 UploadId
     * @param partNumber 分片号
     * @return 预上传地址
     */
    private String createPresignedObjectUrl(String bucketName, String objectName, String uploadId, int partNumber) {
        Map<String, String> extraQueryParams = new HashMap<>();
        extraQueryParams.put("partNumber", String.valueOf(partNumber));
        extraQueryParams.put("uploadId", uploadId);

        GeneratePresignedUrlArguments arguments = new GeneratePresignedUrlArguments();
        arguments.setBucketName(bucketName);
        arguments.setObjectName(objectName);
        arguments.setMethod(HttpMethodEnum.PUT);
        arguments.setExtraQueryParams(extraQueryParams);
        arguments.setExpiration(Duration.ofHours(1));
        return ossObjectRepository.generatePresignedUrl(arguments);
    }

    /**
     * 第三步：获取指定 uploadId 下所有的分片文件
     *
     * @param bucketName 存储桶名称
     * @param objectName 对象名称
     * @param uploadId   第一步中创建的 UploadId
     * @return uploadId 对应的所有分片
     */
    private List<PartSummaryDomain> listParts(String bucketName, String objectName, String uploadId) {
        ListPartsDomain domain = ossMultipartUploadRepository.listParts(bucketName, objectName, uploadId);
        return domain.getParts();
    }

    /**
     * 创建大文件分片上传
     *
     * @param bucketName 存储桶名称
     * @param objectName 对象名称
     * @param totalParts 分片总数
     * @return {@link CreateMultipartUploadBusiness}
     */
    public CreateMultipartUploadBusiness createMultipartUpload(String bucketName, String objectName, int totalParts) {
        String uploadId = createUploadId(bucketName, objectName);
        CreateMultipartUploadBusiness entity = new CreateMultipartUploadBusiness(uploadId);

        // 从 1 开始才能保证 Minio 正确上传。
        for (int i = 1; i <= totalParts; i++) {
            String uploadUrl = createPresignedObjectUrl(bucketName, objectName, uploadId, i);
            entity.append(converter.convert(uploadUrl));
        }
        return entity;
    }

    /**
     * 合并已经上传完成的分片
     *
     * @param bucketName 存储桶名称
     * @param objectName 对象名称
     * @param uploadId   第一步中创建的 UploadId
     * @return {@link ObjectWriteDomain}
     */
    public CompleteMultipartUploadDomain completeMultipartUpload(String bucketName, String objectName, String uploadId) {
        List<PartSummaryDomain> parts = listParts(bucketName, objectName, uploadId);
        if (CollectionUtils.isNotEmpty(parts)) {
            return ossMultipartUploadRepository.completeMultipartUpload(bucketName, objectName, uploadId, parts);
        }

        return null;
    }
}
