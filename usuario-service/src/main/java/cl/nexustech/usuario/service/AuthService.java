package cl.nexustech.usuario.service;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

import cl.nexustech.usuario.dto.LoginRequest;
import cl.nexustech.usuario.dto.LoginResponse;
import cl.nexustech.usuario.model.Usuario;
import cl.nexustech.usuario.repository.UsuarioRepository;
import java.time.Duration;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;
    private final JwtEncoder jwt;
    private final Duration duracion;

    public AuthService(UsuarioRepository usuarios, PasswordEncoder encoder, JwtEncoder jwt,
            @Value("${nexus.jwt.minutos}") long minutos) {
        this.usuarios = usuarios;
        this.encoder = encoder;
        this.jwt = jwt;
        this.duracion = Duration.ofMinutes(minutos);
    }

    public LoginResponse login(LoginRequest datos) {
        Usuario usuario = usuarios.findByCorreo(datos.correo().trim().toLowerCase())
                .filter(u -> encoder.matches(datos.clave(), u.getClave()))
                .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED,
                        "Correo o contraseña incorrectos. Revisa tus datos."));
        return new LoginResponse(crearToken(usuario), usuario);
    }

    // JWT firmado con HS256 y el secreto compartido. Los otros servicios leen
    // "rol" para los permisos y "sub" (id) / "run" para asociar las compras.
    String crearToken(Usuario usuario) {
        Instant ahora = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("usuario-service")
                .subject(String.valueOf(usuario.getId()))
                .issuedAt(ahora)
                .expiresAt(ahora.plus(duracion))
                .claim("rol", usuario.getTipo().name())
                .claim("run", usuario.getRun())
                .claim("correo", usuario.getCorreo())
                .claim("nombre", usuario.getNombre())
                .build();
        return jwt.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
                .getTokenValue();
    }
}
