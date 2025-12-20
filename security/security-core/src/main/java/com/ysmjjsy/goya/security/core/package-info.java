/**
 * <p>安全核心模块 - 提供安全认证授权的核心组件</p>
 *
 * <p>主要功能：</p>
 * <ul>
 *   <li>安全用户：SecurityUser安全用户主体定义</li>
 *   <li>Token解析：JWT和Opaque Token的解析和验证</li>
 *   <li>安全请求：SecurityRequest安全请求匹配器</li>
 *   <li>安全属性：SecurityAttribute安全属性定义</li>
 *   <li>安全权限：SecurityPermission、SecurityGrantedAuthority权限定义</li>
 *   <li>安全服务：ISecurityService安全服务接口</li>
 *   <li>安全工具：SecurityUtils、PrincipalUtils安全工具类</li>
 *   <li>双令牌策略：支持JWT和Opaque Token双令牌策略</li>
 * </ul>
 *
 * <p>主要类和接口：</p>
 * <ul>
 *   <li>{@link com.ysmjjsy.goya.security.core.domain.SecurityUser} - 安全用户主体</li>
 *   <li>{@link com.ysmjjsy.goya.security.core.api.ISecurityService} - 安全服务接口</li>
 *   <li>{@link com.ysmjjsy.goya.security.core.token.SecurityJwtTokenResolver} - JWT Token解析器</li>
 *   <li>{@link com.ysmjjsy.goya.security.core.token.SecurityServletOpaqueTokenResolver} - Opaque Token解析器</li>
 *   <li>{@link com.ysmjjsy.goya.security.core.utils.SecurityUtils} - 安全工具类</li>
 * </ul>
 *
 * <p>使用示例：</p>
 * <pre>{@code
 * // 获取当前用户
 * SecurityUser user = SecurityUtils.getCurrentUser();
 *
 * // 获取当前用户权限
 * Collection<GrantedAuthority> authorities = SecurityUtils.getAuthorities();
 * }</pre>
 *
 * @author goya
 * @since 2025/11/26
 */
package com.ysmjjsy.goya.security.core;


