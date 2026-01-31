package com.ysmjjsy.goya.component.mybatisplus.tenant.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ysmjjsy.goya.component.mybatisplus.tenant.entity.TenantProfileEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>租户配置表 Mapper。</p>
 *
 * @author goya
 * @since 2026/1/31 12:10
 */
@Mapper
public interface TenantProfileMapper extends BaseMapper<TenantProfileEntity> {
}
