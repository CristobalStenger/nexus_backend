package cl.nexustech.usuario.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import cl.nexustech.usuario.dto.LoginRequest;
import cl.nexustech.usuario.dto.LoginResponse;
import cl.nexustech.usuario.model.Rol;
import cl.nexustech.usuario.model.Usuario;
import cl.nexustech.usuario.repository.UsuarioRepository;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static final SecretKey CLAVE = new SecretKeySpec(
            "secreto-solo-para-las-pruebas-unitarias-0123456789".getBytes(StandardCharsets.UTF_8), "HmacSHA256");

    @Mock
    UsuarioRepository usuarios;

    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(4);

    AuthService auth;

    @BeforeEach
    void preparar() {
        auth = new AuthService(usuarios, encoder, new NimbusJwtEncoder(new ImmutableSecret<>(CLAVE)), 120);
    }

    private Usuario vendedora() {
        Usuario usuario = new Usuario();
        usuario.setId(2L);
        usuario.setRun("123456785");
        usuario.setNombre("Valentina");
        usuario.setCorreo("vendedor@duoc.cl");
        usuario.setTipo(Rol.Vendedor);
        usuario.setClave(encoder.encode("vende123"));
        return usuario;
    }

    @Test
    void elLoginEntregaUnTokenFirmadoConElRol() {
        when(usuarios.findByCorreo("vendedor@duoc.cl")).thenReturn(Optional.of(vendedora()));

        LoginResponse respuesta = auth.login(new LoginRequest(" VENDEDOR@duoc.cl ", "vende123"));

        // Lo mismo que hacen los otros microservicios: validar la firma con el secreto compartido
        Jwt token = NimbusJwtDecoder.withSecretKey(CLAVE).build().decode(respuesta.token());
        assertThat(token.getSubject()).isEqualTo("2");
        assertThat(token.getClaimAsString("rol")).isEqualTo("Vendedor");
        assertThat(token.getClaimAsString("run")).isEqualTo("123456785");
        assertThat(token.getExpiresAt()).isAfter(token.getIssuedAt());
        assertThat(respuesta.usuario().getNombre()).isEqualTo("Valentina");
    }

    @Test
    void unaClaveIncorrectaDevuelve401() {
        when(usuarios.findByCorreo("vendedor@duoc.cl")).thenReturn(Optional.of(vendedora()));

        assertThatThrownBy(() -> auth.login(new LoginRequest("vendedor@duoc.cl", "otra")))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        e -> assertThat(e.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED));
    }
}
