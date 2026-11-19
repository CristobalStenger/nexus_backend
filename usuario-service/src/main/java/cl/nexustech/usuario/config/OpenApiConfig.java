package cl.nexustech.usuario.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Swagger: /swagger-ui.html. El botón "Authorize" recibe el token del login.
@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI openApi() {
        return new OpenAPI()
                .info(new Info().title("NexusTech - usuario-service").version("v1")
                        .description("Usuarios, registro e inicio de sesión (emite el JWT)"))
                // Ruta relativa: funciona igual desde el servicio y desde el API Gateway
                .addServersItem(new Server().url("/"))
                .components(new Components().addSecuritySchemes("jwt",
                        new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList("jwt"));
    }
}
