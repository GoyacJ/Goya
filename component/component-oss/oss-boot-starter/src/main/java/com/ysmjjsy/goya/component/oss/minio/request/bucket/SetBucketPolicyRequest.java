package com.ysmjjsy.goya.component.oss.minio.request.bucket;

import com.google.common.collect.Lists;
import com.ysmjjsy.goya.component.framework.json.GoyaJson;
import com.ysmjjsy.goya.component.oss.minio.definition.BucketRequest;
import com.ysmjjsy.goya.component.oss.minio.domain.policy.PolicyDomain;
import com.ysmjjsy.goya.component.oss.minio.domain.policy.StatementDomain;
import com.ysmjjsy.goya.component.oss.minio.enums.PolicyEnums;
import io.minio.SetBucketPolicyArgs;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.util.List;

/**
 * <p> 设置存储桶访问策略请求参数实体 </p>
 *
 * @author goya
 * @since 2023/6/6 21:42
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(name = "设置存储桶访问策略请求参数实体", title = "设置存储桶访问策略请求参数实体")
public class SetBucketPolicyRequest extends BucketRequest<SetBucketPolicyArgs.Builder, SetBucketPolicyArgs> {

    private static final String DEFAULT_RESOURCE_PREFIX = "arn:aws:s3:::";
    private static final List<String> DEFAULT_ACTION_FOR_BUCKET = Lists.newArrayList("s3:GetBucketLocation", "s3:ListBucket", "s3:ListBucketMultipartUploads");
    private static final List<String> DEFAULT_ACTION_FOR_OBJECT = Lists.newArrayList("s3:DeleteObject", "s3:GetObject", "s3:ListMultipartUploadParts", "s3:PutObject", "s3:AbortMultipartUpload");
    @Serial
    private static final long serialVersionUID = 6289370632818769575L;


    @Schema(name = "访问策略类型", requiredMode = Schema.RequiredMode.REQUIRED)
    private PolicyEnums type;

    @Schema(name = "访问策略配置", description = "如果为自定义类型那么必需输入配置信息")
    private PolicyDomain config;

    @Override
    public void prepare(SetBucketPolicyArgs.Builder builder) {

        PolicyDomain policyDomain;

        switch (getType()) {
            case PUBLIC -> policyDomain = getPublicPolicy();
            case CUSTOM -> policyDomain = getConfig();
            default -> policyDomain = getPrivatePolicy(getBucketName());
        }

        builder.config(GoyaJson.toJson(policyDomain));
        super.prepare(builder);
    }

    @Override
    public SetBucketPolicyArgs.Builder getBuilder() {
        return SetBucketPolicyArgs.builder();
    }

    private PolicyDomain getPublicPolicy() {
        return new PolicyDomain();
    }

    private PolicyDomain getPrivatePolicy(String bucketName) {
        StatementDomain bucketStatement = new StatementDomain();
        bucketStatement.setActions(DEFAULT_ACTION_FOR_BUCKET);
        bucketStatement.setResources(getDefaultResource(bucketName, true));

        StatementDomain objectStatement = new StatementDomain();
        objectStatement.setActions(DEFAULT_ACTION_FOR_OBJECT);
        objectStatement.setResources(getDefaultResource(bucketName, false));

        PolicyDomain policy = new PolicyDomain();
        policy.setStatements(Lists.newArrayList(bucketStatement, objectStatement));
        return policy;
    }

    private List<String> getDefaultResource(String bucketName, boolean isForBucket) {
        String suffix = isForBucket ? "" : "/*";
        return Lists.newArrayList(DEFAULT_RESOURCE_PREFIX + bucketName + suffix);
    }
}
