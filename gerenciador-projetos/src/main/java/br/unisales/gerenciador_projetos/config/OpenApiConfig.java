package br.unisales.gerenciador_projetos.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

        private static final String ESQUEMA = "bearerAuth";

        @Bean
        public OpenAPI openAPI() {
                return new OpenAPI()
                                .info(new Info()
                                                .title("Gerenciamento de Projetos")
                                                .version("1.0"))
                                .addSecurityItem(new SecurityRequirement().addList(ESQUEMA))
                                .components(new Components()
                                                .addSecuritySchemes(ESQUEMA, new SecurityScheme()
                                                                .name(ESQUEMA)
                                                                .type(SecurityScheme.Type.HTTP)
                                                                .scheme("bearer")
                                                                .bearerFormat("JWT")));
        }
}