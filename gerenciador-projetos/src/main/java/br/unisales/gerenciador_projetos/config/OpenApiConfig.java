package br.unisales.gerenciador_projetos.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@SecurityScheme(
        name = "basicAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "basic"
)
public class OpenApiConfig {

    @Bean
    public OpenAPI gerenciadorProjetosOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Gerenciamento de Projetos API")
                        .version("1.0.0")
                        .description("API REST para gestão de projetos, tarefas, equipes, anexos e usuários."))
                .addSecurityItem(new SecurityRequirement().addList("basicAuth"));
    }
}
