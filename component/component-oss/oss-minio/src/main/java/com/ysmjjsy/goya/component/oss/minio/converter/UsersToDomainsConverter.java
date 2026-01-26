package com.ysmjjsy.goya.component.oss.minio.converter;

import com.ysmjjsy.goya.component.oss.minio.domain.UserDomain;
import io.minio.admin.UserInfo;
import org.apache.commons.collections4.MapUtils;
import org.springframework.core.convert.converter.Converter;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * <p>UserInfo Map 转 List  转换器  </p>
 *
 * @author goya
 * @since 2025/11/1 16:32
 */
public class UsersToDomainsConverter implements Converter<Map<String, UserInfo>, List<UserDomain>> {

    private final Converter<UserInfo, UserDomain> toDomain = new UserInfoToDomainConverter();

    @Override
    public List<UserDomain> convert(Map<String, UserInfo> source) {
        if (MapUtils.isNotEmpty(source)) {
            return source.entrySet().stream().map(entry -> {
                UserDomain domain = toDomain.convert(entry.getValue());
                domain.setAccessKey(entry.getKey());
                return domain;
            }).toList();
        }

        return Collections.emptyList();
    }
}
