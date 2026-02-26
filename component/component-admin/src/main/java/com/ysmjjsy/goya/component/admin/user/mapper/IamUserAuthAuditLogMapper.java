package com.ysmjjsy.goya.component.admin.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ysmjjsy.goya.component.admin.user.entity.IamUserAuthAuditLogEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>认证审计日志 mapper</p>
 *
 * @author goya
 * @since 2026/2/26
 */
@Mapper
public interface IamUserAuthAuditLogMapper extends BaseMapper<IamUserAuthAuditLogEntity> {
}
