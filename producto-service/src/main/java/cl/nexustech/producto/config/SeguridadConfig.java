package cl.nexustech.producto.config;

import java.nio.charset.StandardCharsets;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

// Valida el JWT que emite usuario-service y aplica los permisos de cada rol
@Configuration
public class SeguridadConfig {

    @Bean
    SecurityFilterChain seguridad(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable) // API sin cookies: el token viaja en el header
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(reglas -> reglas
                        .requestMatchers("/error", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/api/v1/productos/stock/**").hasRole("SERVICIO")
                        // Tienda, vendedor y administrador ven el catálogo
                        .requestMatchers(HttpMethod.GET, "/api/v1/productos/**", "/api/v1/categorias/**").permitAll()
                        // Crear, editar y eliminar: solo el administrador
                        .anyRequest().hasRole("Administrador"))
                .oauth2ResourceServer(o -> o.jwt(Customizer.withDefaults()))
                .build();
    }

    @Bean
    JwtDecoder jwtDecoder(@Value("${nexus.jwt.secret}") String secreto) {
        byte[] bytes = secreto.getBytes(StandardCharsets.UTF_8);
        // Sin esto el servicio arranca igual y recién falla al firmar o validar el primer token
        if (bytes.length < 32)
            throw new IllegalStateException("JWT_SECRET debe tener al menos 32 caracteres (HS256).");
        return NimbusJwtDecoder.withSecretKey(new SecretKeySpec(bytes, "HmacSHA256")).build();
    }
}
