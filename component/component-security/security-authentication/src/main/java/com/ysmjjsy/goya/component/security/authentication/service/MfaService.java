package com.ysmjjsy.goya.component.security.authentication.service;

import com.ysmjjsy.goya.component.framework.cache.api.CacheService;
import com.ysmjjsy.goya.component.framework.common.utils.GoyaIdUtils;
import com.ysmjjsy.goya.component.framework.core.json.GoyaJson;
import com.ysmjjsy.goya.component.framework.servlet.utils.WebUtils;
import com.ysmjjsy.goya.component.security.authentication.configuration.properties.SecurityAuthenticationProperties;
import com.ysmjjsy.goya.component.security.authentication.constants.SecurityAuthCacheNames;
import com.ysmjjsy.goya.component.security.authentication.dto.AuthResult;
import com.ysmjjsy.goya.component.security.authentication.dto.MfaChallengeRequest;
import com.ysmjjsy.goya.component.security.authentication.dto.MfaVerifyRequest;
import com.ysmjjsy.goya.component.security.authentication.service.model.MfaChallengePayload;
import com.ysmjjsy.goya.component.security.authentication.service.model.PreAuthCodePayload;
import com.ysmjjsy.goya.component.security.core.domain.SecurityUser;
import com.ysmjjsy.goya.component.security.core.enums.ClientTypeEnum;
import com.ysmjjsy.goya.component.security.core.enums.MfaType;
import com.ysmjjsy.goya.component.security.core.error.SecurityErrorCode;
import com.ysmjjsy.goya.component.security.core.exception.SecurityAuthenticationException;
import com.ysmjjsy.goya.component.security.core.manager.SecurityUserManager;
import com.ysmjjsy.goya.component.security.core.service.IOtpService;
import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.code.HashingAlgorithm;
import dev.samstevens.totp.time.SystemTimeProvider;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

/**
 * <p>MFA服务</p>
 *
 * @author goya
 * @since 2026/2/10
 */
public class MfaService {

    private final CacheService cacheService;
    private final PreAuthCodeService preAuthCodeService;
    private final ObjectProvider<IOtpService> otpServiceProvider;
    private final SecurityUserManager securityUserManager;
    private final DeviceTrustService deviceTrustService;
    private final SecurityAuthenticationProperties securityAuthenticationProperties;

    public MfaService(CacheService cacheService,
                      PreAuthCodeService preAuthCodeService,
                      ObjectProvider<IOtpService> otpServiceProvider,
                      SecurityUserManager securityUserManager,
                      DeviceTrustService deviceTrustService,
                      SecurityAuthenticationProperties securityAuthenticationProperties) {
        this.cacheService = cacheService;
        this.preAuthCodeService = preAuthCodeService;
        this.otpServiceProvider = otpServiceProvider;
        this.securityUserManager = securityUserManager;
        this.deviceTrustService = deviceTrustService;
        this.securityAuthenticationProperties = securityAuthenticationProperties;
    }

    public String issueChallenge(PreAuthCodePayload preAuthCodePayload,
                                 MfaType preferredType,
                                 String target,
                                 String totpSecret) {
        MfaType mfaType = preferredType == null ? securityAuthenticationProperties.defaultMfaType() : preferredType;
        String resolvedTarget = resolveMfaTarget(preAuthCodePayload, target);

        if (mfaType == MfaType.SMS) {
            IOtpService otpService = otpServiceProvider.getIfAvailable();
            if (otpService == null) {
                throw new SecurityAuthenticationException(
                        SecurityErrorCode.SECURITY_SERVICE_UNAVAILABLE,
                        "OTP服务未配置"
                );
            }
            if (StringUtils.isBlank(resolvedTarget)) {
                throw new SecurityAuthenticationException(SecurityErrorCode.OTP_INVALID, "缺少MFA目标账号");
            }
            otpService.send(preAuthCodePayload.tenantId(), resolvedTarget);
        }

        String challengeId = "mfa_" + GoyaIdUtils.fastSimpleUUID();
        MfaChallengePayload challengePayload = new MfaChallengePayload(
                challengeId,
                mfaType,
                resolvedTarget,
                totpSecret,
                preAuthCodePayload,
                Instant.now().plus(securityAuthenticationProperties.mfaChallengeTtl()).getEpochSecond()
        );

        cacheService.put(
                SecurityAuthCacheNames.MFA_CHALLENGE,
                challengeId,
                GoyaJson.toJson(challengePayload),
                securityAuthenticationProperties.mfaChallengeTtl()
        );

        return challengeId;
    }

    public String issueChallenge(MfaChallengeRequest request) {
        SecurityUser securityUser = securityUserManager.findUserByUserId(request.userId());
        ClientTypeEnum clientType = request.clientType() == null ? ClientTypeEnum.WEB : request.clientType();
        String sid = StringUtils.isNotBlank(request.sid()) ? request.sid() : "sid_" + GoyaIdUtils.fastSimpleUUID();
        PreAuthCodePayload preAuthCodePayload = PreAuthCodePayload.fromUser(
                securityUser,
                request.tenantId(),
                clientType,
                sid,
                request.deviceId(),
                false,
                null
        );
        return issueChallenge(preAuthCodePayload, request.mfaType(), request.target(), request.totpSecret());
    }

    public AuthResult verifyAndIssuePreAuth(MfaVerifyRequest request, HttpServletRequest servletRequest) {
        String challengeId = request.challengeId();
        if (StringUtils.isBlank(challengeId)) {
            throw new SecurityAuthenticationException(
                    SecurityErrorCode.MFA_CHALLENGE_NOT_FOUND,
                    "MFA挑战不存在或已过期"
            );
        }

        if (cacheService.exists(SecurityAuthCacheNames.MFA_CHALLENGE_CONSUMED, challengeId)) {
            throw new SecurityAuthenticationException(
                    SecurityErrorCode.MFA_CHALLENGE_NOT_FOUND,
                    "MFA挑战不存在或已过期"
            );
        }

        boolean acquired = cacheService.putIfAbsent(
                SecurityAuthCacheNames.MFA_CHALLENGE_PROCESSING,
                challengeId,
                "1",
                resolveMfaProcessingLockTtl()
        );
        if (!acquired) {
            throw new SecurityAuthenticationException(
                    SecurityErrorCode.MFA_CHALLENGE_NOT_FOUND,
                    "MFA挑战正在处理中"
            );
        }

        try {
            MfaChallengePayload challengePayload = readChallenge(challengeId)
                .orElseThrow(() -> new SecurityAuthenticationException(
                        SecurityErrorCode.MFA_CHALLENGE_NOT_FOUND,
                        "MFA挑战不存在或已过期"
                ));

            boolean verified = switch (challengePayload.mfaType()) {
                case SMS -> verifySms(challengePayload, request.code());
                case TOTP -> verifyTotp(challengePayload, request.code(), request.totpSecret());
            };

            if (!verified) {
                throw new SecurityAuthenticationException(SecurityErrorCode.MFA_VERIFICATION_FAILED, "MFA验证码错误");
            }

            boolean markedConsumed = cacheService.putIfAbsent(
                    SecurityAuthCacheNames.MFA_CHALLENGE_CONSUMED,
                    challengeId,
                    "1",
                    securityAuthenticationProperties.mfaChallengeTtl()
            );
            if (!markedConsumed) {
                throw new SecurityAuthenticationException(
                        SecurityErrorCode.MFA_CHALLENGE_NOT_FOUND,
                        "MFA挑战不存在或已过期"
                );
            }

            cacheService.delete(SecurityAuthCacheNames.MFA_CHALLENGE, challengeId);

            PreAuthCodePayload payload = challengePayload.preAuthCodeData().withMfaVerified(true);
            String resolvedDeviceId = deviceTrustService.resolveDeviceId(
                    servletRequest,
                    request.deviceId(),
                    payload.userId()
            );
            payload = payload.withDeviceId(resolvedDeviceId);

            SecurityUser securityUser = securityUserManager.findUserByUserId(payload.userId());
            deviceTrustService.registerOrUpdate(securityUser, resolvedDeviceId, servletRequest);
            securityUserManager.recordLoginSuccess(
                    payload.userId(),
                    payload.username(),
                    payload.tenantId(),
                    WebUtils.getIp(servletRequest),
                    com.ysmjjsy.goya.component.framework.core.web.UserAgent.userAgentParse(servletRequest),
                    servletRequest.getRequestURI()
            );

            String preAuthCode = preAuthCodeService.issue(payload);
            return AuthResult.preAuthIssued(
                    preAuthCode,
                    securityAuthenticationProperties.preAuthCodeTtl().toSeconds(),
                    securityAuthenticationProperties.tokenExchangeGrantType(),
                    payload.sid()
            );
        } finally {
            cacheService.delete(SecurityAuthCacheNames.MFA_CHALLENGE_PROCESSING, challengeId);
        }
    }

    private Optional<MfaChallengePayload> readChallenge(String challengeId) {
        if (StringUtils.isBlank(challengeId)) {
            return Optional.empty();
        }
        String value = cacheService.get(SecurityAuthCacheNames.MFA_CHALLENGE, challengeId, String.class);
        if (StringUtils.isBlank(value)) {
            return Optional.empty();
        }
        return Optional.ofNullable(GoyaJson.fromJson(value, MfaChallengePayload.class));
    }

    private boolean verifySms(MfaChallengePayload challengePayload, String code) {
        IOtpService otpService = otpServiceProvider.getIfAvailable();
        if (otpService == null) {
            throw new SecurityAuthenticationException(SecurityErrorCode.SECURITY_SERVICE_UNAVAILABLE, "OTP服务未配置");
        }
        return otpService.verify(
                challengePayload.preAuthCodeData().tenantId(),
                challengePayload.target(),
                code
        );
    }

    private boolean verifyTotp(MfaChallengePayload challengePayload, String code, String overrideSecret) {
        String secret = StringUtils.defaultIfBlank(overrideSecret, challengePayload.totpSecret());
        if (StringUtils.isBlank(secret) || StringUtils.isBlank(code)) {
            return false;
        }

        CodeVerifier codeVerifier = new DefaultCodeVerifier(
                new DefaultCodeGenerator(HashingAlgorithm.SHA1),
                new SystemTimeProvider()
        );
        return codeVerifier.isValidCode(secret, code);
    }

    private String resolveMfaTarget(PreAuthCodePayload preAuthCodePayload, String target) {
        if (StringUtils.isNotBlank(target)) {
            return target;
        }
        if (StringUtils.isNotBlank(preAuthCodePayload.phoneNumber())) {
            return preAuthCodePayload.phoneNumber();
        }
        return preAuthCodePayload.email();
    }

    private Duration resolveMfaProcessingLockTtl() {
        long maxSeconds = Math.max(10L, securityAuthenticationProperties.mfaChallengeTtl().toSeconds());
        long lockSeconds = Math.min(maxSeconds, 60L);
        return Duration.ofSeconds(lockSeconds);
    }
}
