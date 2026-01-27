package com.ysmjjsy.goya.component.oss.s3.service;

import com.ysmjjsy.goya.component.oss.s3.definition.pool.S3ClientObjectPool;
import com.ysmjjsy.goya.component.oss.s3.definition.service.BaseS3Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectAclRequest;
import software.amazon.awssdk.services.s3.model.GetObjectAclResponse;
import software.amazon.awssdk.services.s3.model.PutObjectAclRequest;
import software.amazon.awssdk.services.s3.model.PutObjectAclResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

/**
 * <p>Amazon S3 对象访问控制列表 Service </p>
 *
 * @author goya
 * @since 2023/7/16 21:12
 */
@Slf4j
@Service
public class S3ObjectAccessControlListService extends BaseS3Service {

    public S3ObjectAccessControlListService(S3ClientObjectPool s3ClientObjectPool) {
        super(s3ClientObjectPool);
    }

    /**
     * 获取对象访问控制列表
     *
     * @param request 获取对象ACL请求
     * @return 获取对象ACL响应
     * @throws S3Exception S3操作异常
     */
    public GetObjectAclResponse getObjectAcl(GetObjectAclRequest request) {
        String function = "getObjectAcl";
        S3Client client = getClient();
        try {
            return client.getObjectAcl(request);
        } catch (S3Exception e) {
            log.error("[Goya] |- S3 OSS catch S3Exception in [{}].", function, e);
            throw e;
        } finally {
            close(client);
        }
    }

    /**
     * 设置对象访问控制列表
     *
     * @param request 设置对象ACL请求
     * @return 设置对象ACL响应
     * @throws S3Exception S3操作异常
     */
    public PutObjectAclResponse putObjectAcl(PutObjectAclRequest request) {
        String function = "putObjectAcl";
        S3Client client = getClient();
        try {
            return client.putObjectAcl(request);
        } catch (S3Exception e) {
            log.error("[Goya] |- S3 OSS catch S3Exception in [{}].", function, e);
            throw e;
        } finally {
            close(client);
        }
    }
}
