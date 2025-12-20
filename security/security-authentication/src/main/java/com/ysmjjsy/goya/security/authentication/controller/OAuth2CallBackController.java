package com.ysmjjsy.goya.security.authentication.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

import static com.ysmjjsy.goya.security.authentication.constants.ISecurityAuthenticationConstants.OAUTH2_CALLBACK_PATH;

/**
 * <p>oauth2回调接口</p>
 *
 * @author goya
 * @since 2025/12/17 23:41
 */
@RestController
@RequestMapping(OAUTH2_CALLBACK_PATH)
public class OAuth2CallBackController {

    private final ClientRegistrationRepository clientRegistrationRepository;
    private final OAuth2TokenService tokenService;
    private final UserApplicationService userApplicationService;
    private final StateService stateService;
    private final Map<String, OAuth2UserInfoService> userInfoServiceMap;

    /**
     * 第三方 OAuth2 登录回调
     */
    @GetMapping("/{provider}/callback")
    public ResponseEntity<Map<String, Object>> oauth2Callback(
            @PathVariable String provider,
            @RequestParam String code,
            @RequestParam String state) {

        try {
            // ===== 1. 获取 ClientRegistration =====
            ClientRegistration registration = clientRegistrationRepository.findByRegistrationId(provider);
            if (registration == null) {
                return ResponseEntity.badRequest().body(createErrorResponse("不支持的OAuth2提供者: " + provider));
            }

            // ===== 2. 验证 state 并解析租户ID =====
            TenantId tenantId = stateService.validateAndParseTenant(state);
            if (tenantId == null) {
                return ResponseEntity.badRequest().body(createErrorResponse("租户ID不能为空或 state 无效"));
            }

            // ===== 3. 通过授权码换取第三方 access_token =====
            String thirdAccessToken = exchangeCodeForToken(registration, code);
            if (StringUtils.isBlank(thirdAccessToken)) {
                return ResponseEntity.status(500).body(createErrorResponse("无法获取第三方 access_token"));
            }

            // ===== 4. 获取第三方用户信息 =====
            OAuth2UserInfoService userInfoService = userInfoServiceMap.get(provider);
            if (userInfoService == null) {
                return ResponseEntity.badRequest().body(createErrorResponse("未配置用户信息服务: " + provider));
            }

            Map<String, Object> userInfo = userInfoService.getUserInfo(thirdAccessToken);
            if (userInfo == null || userInfo.get("id") == null) {
                return ResponseEntity.status(500).body(createErrorResponse("无法获取第三方用户信息"));
            }

            // ===== 5. 创建或获取系统用户 =====
            User user = userApplicationService.createUserFromOAuth2(
                    tenantId,
                    new UserApplicationService.CreateOAuth2UserCommand(
                            provider,
                            (String) userInfo.get("id"),
                            (String) userInfo.getOrDefault("username", userInfo.get("name")),
                            (String) userInfo.get("email"),
                            (String) userInfo.get("phone"),
                            (String) userInfo.get("avatar")
                    )
            );

            // ===== 6. 生成系统 Token（Authorization Server / 自定义 TokenService） =====
            OAuth2TokenResponse tokenResponse = tokenService.generateToken(user, tenantId);

            // ===== 7. 返回结果 =====
            Map<String, Object> result = new HashMap<>();
            result.put("message", "OAuth2登录成功");
            result.put("user", createUserResponse(user));
            result.put("token", tokenResponse);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("OAuth2登录失败", e);
            return ResponseEntity.status(500).body(createErrorResponse("OAuth2登录失败: " + e.getMessage()));
        }
    }

    private String exchangeCodeForToken(ClientRegistration registration, String code) {
        // TODO: 调用 provider 的 token endpoint，使用 RestTemplate/WebClient
        // 返回第三方 access_token
        return null;
    }

    private Map<String, Object> createErrorResponse(String msg) {
        Map<String, Object> map = new HashMap<>();
        map.put("error", msg);
        return map;
    }

    private Map<String, Object> createUserResponse(User user) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", user.getId());
        map.put("username", user.getUsername());
        map.put("email", user.getEmail());
        map.put("phone", user.getPhone());
        map.put("avatar", user.getAvatar());
        map.put("roles", user.getRoles());
        return map;
    }
}
