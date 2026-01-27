package com.ysmjjsy.goya.component.oss.minio.converter;

import com.ysmjjsy.goya.component.oss.minio.domain.UserDomain;
import io.minio.admin.Status;
import io.minio.admin.UserInfo;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.core.convert.converter.Converter;

/**
 * <p>UserInfo 转 UserDomain 转换器</p>
 *
 * @author goya
 * @since 2025/11/1 16:32
 */
public class UserInfoToDomainConverter implements Converter<UserInfo, UserDomain> {
    @Override
    public UserDomain convert(UserInfo userInfo) {

        UserDomain domain = new UserDomain();

        if (ObjectUtils.isNotEmpty(userInfo)) {
            domain.setSecretKey(userInfo.secretKey());
            domain.setPolicyName(userInfo.policyName());
            domain.setMemberOf(userInfo.memberOf());
            domain.setStatus(Status.fromString(userInfo.status().name()));
        }

        return domain;
    }
}

