package com.ysmjjsy.goya.component.framework.servlet.autoconfigure;

import com.google.common.collect.ImmutableList;
import com.ysmjjsy.goya.component.framework.core.context.GoyaContext;
import com.ysmjjsy.goya.component.framework.servlet.doc.IOpenApiServerResolver;
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
public class ServletSpringdocAutoConfiguration {

    @PostConstruct
    public void postConstruct() {
        log.debug("[Goya] |- component [web] SpringdocAutoConfiguration auto configure.");
    }

    @Bean
    @ConditionalOnMissingBean
    public IOpenApiServerResolver openApiServerResolver(GoyaContext goyaContext) {
        IOpenApiServerResolver resolver = () -> {
            Server server = new Server();
            server.setUrl(goyaContext.getUrl());
            return ImmutableList.of(server);
        };
        log.trace("[Goya] |- component [web] SpringdocAutoConfiguration |- bean [openApiServerResolver] register.");
        return resolver;
    }

    @Bean
    @ConditionalOnMissingBean
    public OpenAPI createOpenApi(IOpenApiServerResolver openApiServerResolver, GoyaContext goyaContext) {
        OpenAPI openApi = new OpenAPI()
                .servers(openApiServerResolver.getServers())
                .info(new Info().title(goyaContext.getPlatformInfo().projectName())
                        .description("Goya Cloud Microservices Architecture")
                        .version("Swagger V3")
                        .license(new License().url(goyaContext.getPlatformInfo().website()).name(goyaContext.getPlatformInfo().projectName())));
        log.trace("[Goya] |- component [web] SpringdocAutoConfiguration |- bean [createOpenApi] register.");
        return openApi;
    }
}
