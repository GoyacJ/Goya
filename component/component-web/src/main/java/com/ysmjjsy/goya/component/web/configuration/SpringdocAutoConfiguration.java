package com.ysmjjsy.goya.component.web.configuration;

import com.google.common.collect.ImmutableList;
import com.ysmjjsy.goya.component.common.service.IPlatformService;
import com.ysmjjsy.goya.component.web.doc.IOpenApiServerResolver;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.*;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * <p></p>
 *
 * @author goya
 * @since 2025/6/13 18:00
 */
@Slf4j
@AutoConfiguration
@SecuritySchemes({
        @SecurityScheme(name = "GOYA_AUTH", type = SecuritySchemeType.OAUTH2, bearerFormat = "JWT", scheme = "bearer",
                flows = @OAuthFlows(
                        password = @OAuthFlow(authorizationUrl = "${platform.security.endpoint.authorization-uri}", tokenUrl = "${platform.security.endpoint.access-token-uri}", refreshUrl = "${platform.security.endpoint.access-token-uri}", scopes = @OAuthScope(name = "all")),
                        clientCredentials = @OAuthFlow(authorizationUrl = "${platform.security.endpoint.authorization-uri}", tokenUrl = "${platform.security.endpoint.access-token-uri}", refreshUrl = "${platform.security.endpoint.access-token-uri}", scopes = @OAuthScope(name = "all"))
//                        authorizationCode = @OAuthFlow(authorizationUrl = "${platform.platform.endpoint.user-authorization-uri}", tokenUrl = "${goya.platform.endpoint.access-token-uri}", refreshUrl = "${goya.platform.endpoint.access-token-uri}", scopes = @OAuthScope(name = "all"))
                )),
})
public class SpringdocAutoConfiguration {

    @PostConstruct
    public void postConstruct() {
        log.debug("[GOYA] |- component [web] SpringdocAutoConfiguration auto configure.");
    }

    @Bean
    @ConditionalOnMissingBean
    public IOpenApiServerResolver openApiServerResolver(IPlatformService iPlatformService) {
        IOpenApiServerResolver resolver = () -> {
            Server server = new Server();
            server.setUrl(iPlatformService.getUrl());
            return ImmutableList.of(server);
        };
        log.trace("[GOYA] |- component [web] SpringdocAutoConfiguration |- bean [openApiServerResolver] register.");
        return resolver;
    }

    @Bean
    @ConditionalOnMissingBean
    public OpenAPI createOpenApi(IOpenApiServerResolver openApiServerResolver, IPlatformService iPlatformService) {
        OpenAPI openApi = new OpenAPI()
                .servers(openApiServerResolver.getServers())
                .info(new Info().title(iPlatformService.getPlatformInfo().projectName())
                        .description("Goya Cloud Microservices Architecture")
                        .version("Swagger V3")
                        .license(new License().url(iPlatformService.getPlatformInfo().website()).name(iPlatformService.getPlatformInfo().projectName())));
        log.trace("[GOYA] |- component [web] SpringdocAutoConfiguration |- bean [createOpenApi] register.");
        return openApi;
    }
}
