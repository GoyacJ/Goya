package com.ysmjjsy.goya.component.oss.minio.converter;

import com.ysmjjsy.goya.component.oss.minio.domain.GroupDomain;
import io.minio.admin.GroupInfo;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.core.convert.converter.Converter;

/**
 * <p>GroupInfo 转 GroupDomain 转换器</p>
 *
 * @author goya
 * @since 2025/11/1 16:31
 */
public class GroupInfoToDomainConverter implements Converter<GroupInfo, GroupDomain> {
    @Override
    public GroupDomain convert(GroupInfo groupInfo) {

        GroupDomain domain = new GroupDomain();

        if (ObjectUtils.isNotEmpty(groupInfo)) {
            domain.setName(groupInfo.name());
            domain.setStatus(groupInfo.status());
            domain.setMembers(groupInfo.members());
            domain.setPolicy(groupInfo.policy());
        }

        return domain;
    }
}
