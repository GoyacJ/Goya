/**
 * <p>安全认证模块 - 提供OAuth2认证功能</p>
 *
 * <p>主要功能：</p>
 * <ul>
 *   <li>OAuth2认证：基于Spring Authorization Server的OAuth2认证支持</li>
 *   <li>认证提供者：AbstractAuthenticationProvider抽象认证提供者</li>
 *   <li>用户详情服务：IEnhanceUserDetailsService增强用户详情服务</li>
 *   <li>Token定制：JWT和Opaque Token的定制化配置</li>
 *   <li>表单登录：OAuth2表单登录支持</li>
 *   <li>认证配置：OAuth2AuthenticationConfigurerManager认证配置管理器</li>
 *   <li>认证事件：DefaultOAuth2AuthenticationEventPublisher认证事件发布器</li>
 * </ul>
 *
 * <p>主要类和接口：</p>
 * <ul>
 *   <li>{@link com.ysmjjsy.goya.security.authentication.provider.AbstractAuthenticationProvider} - 抽象认证提供者</li>
 *   <li>{@link com.ysmjjsy.goya.security.authentication.provider.AbstractUserDetailsAuthenticationProvider} - 用户详情认证提供者</li>
 *   <li>{@link com.ysmjjsy.goya.security.authentication.configurer.OAuth2AuthenticationConfigurerManager} - OAuth2认证配置管理器</li>
 *   <li>{@link com.ysmjjsy.goya.security.authentication.cusumer.SecurityJwtTokenCustomizer} - JWT Token定制器</li>
 * </ul>
 *
 * <p>使用示例：</p>
 * <pre>{@code
 * // 实现用户详情服务
 * @Service
 * public class UserDetailsServiceImpl implements IEnhanceUserDetailsService {
 *     @Override
 *     public UserDetails loadUserByUsername(String username) {
 *         // 加载用户信息
 *     }
 * }
 * }</pre>
 *
 * @author goya
 * @since 2025/11/26
 */
package com.ysmjjsy.goya.security.authentication;


