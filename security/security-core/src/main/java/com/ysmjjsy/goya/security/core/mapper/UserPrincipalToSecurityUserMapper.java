package com.ysmjjsy.goya.security.core.mapper;

import com.ysmjjsy.goya.component.auth.domain.UserPrincipal;
import com.ysmjjsy.goya.component.common.mapstruct.IConverter;
import com.ysmjjsy.goya.component.common.mapstruct.IMapStructSpringConfig;
import com.ysmjjsy.goya.security.core.domain.SecurityUser;
import org.mapstruct.Mapper;

/**
 * <p></p>
 *
 * @author goya
 * @since 2026/1/5 23:15
 */
@Mapper(config = IMapStructSpringConfig.class)
public interface UserPrincipalToSecurityUserMapper extends IConverter<UserPrincipal, SecurityUser> {

}
