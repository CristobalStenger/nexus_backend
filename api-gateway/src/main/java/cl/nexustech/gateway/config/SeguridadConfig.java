package cl.nexustech.gateway.config;

import java.nio.charset.StandardCharsets;
import java.util.List;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

// Primera barrera: CORS para el frontend y rechazo temprano de tokens inválidos o vencidos.
// Los permisos por rol los vuelve a revisar cada microservicio (sus URLs también son públicas).
@Configuration
public class SeguridadConfig {

    @Bean
    SecurityWebFilterChain seguridad(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .cors(Customizer.withDefaults())
                .authorizeExchange(reglas -> reglas
                        // El stock solo lo mueve pedido-service, nunca desde afuera
                        .pathMatchers("/api/v1/productos/stock/**").denyAll()
                        .anyExchange().permitAll())
                // Si llega un "Authorization: Bearer ..." se valida firma y vencimiento (401 si falla)
                .oauth2ResourceServer(o -> o.jwt(Customizer.withDefaults()))
                .build();
    }

    @Bean
    ReactiveJwtDecoder jwtDecoder(@Value("${nexus.jwt.secret}") String secreto) {
        byte[] bytes = secreto.getBytes(StandardCharsets.UTF_8);
        // Sin esto el servicio arranca igual y recién falla al firmar o validar el primer token
        if (bytes.length < 32)
            throw new IllegalStateException("JWT_SECRET debe tener al menos 32 caracteres (HS256).");
        return NimbusReactiveJwtDecoder.withSecretKey(new SecretKeySpec(bytes, "HmacSHA256")).build();
    }

    // CORS solo aquí: si los servicios también lo agregaran, el navegador vería cabeceras duplicadas
    @Bean
    CorsConfigurationSource corsConfigurationSource(@Value("${nexus.frontend-url}") List<String> origenes) {
        CorsConfiguration cors = new CorsConfiguration();
        cors.setAllowedOrigins(origenes);
        cors.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        cors.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        cors.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource fuente = new UrlBasedCorsConfigurationSource();
        fuente.registerCorsConfiguration("/**", cors);
        return fuente;
    }
}
