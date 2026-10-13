package com.mybank.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    /*
     * Define as informações gerais da API
     * e a configuração de autenticação utilizada
     * pela documentação do Swagger.
     */
    @Bean
    public OpenAPI myBankOpenAPI() {

        return new OpenAPI()

                /*
                 * Informações apresentadas no Swagger UI.
                 */
                .info(
                        new Info()
                                .title("MyBank API")
                                .version("1.0.0")
                                .description(
                                        "REST API for the MyBank application."
                                )
                )

                /*
                 * Registra o esquema de autenticação JWT.
                 *
                 * O Swagger utilizará esse esquema para enviar
                 * o Access Token no header Authorization.
                 */
                .components(
                        new Components()
                                .addSecuritySchemes(
                                        "bearerAuth",
                                        new SecurityScheme()
                                                .type(SecurityScheme.Type.HTTP)
                                                .scheme("bearer")
                                                .bearerFormat("JWT")
                                )
                )

                /*
                 * Define JWT como o mecanismo de autenticação
                 * padrão dos endpoints documentados.
                 */
                .addSecurityItem(
                        new SecurityRequirement()
                                .addList("bearerAuth")
                );
    }
}