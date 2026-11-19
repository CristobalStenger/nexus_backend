package cl.nexustech.usuario.controller;

import cl.nexustech.usuario.dto.UsuarioRequest;
import cl.nexustech.usuario.model.Usuario;
import cl.nexustech.usuario.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/usuarios")
@RequiredArgsConstructor
@Tag(name = "Usuarios")
public class UsuarioController {

    private final UsuarioService servicio;

    @GetMapping("/me")
    @Operation(summary = "Datos del usuario con sesión iniciada (cualquier rol)")
    public Usuario perfil(@AuthenticationPrincipal Jwt token) {
        return servicio.obtener(idSesion(token));
    }

    @PutMapping("/me")
    @Operation(summary = "Edita el propio perfil, sin cambiar el rol (cualquier rol)")
    public Usuario editarPerfil(@AuthenticationPrincipal Jwt token, @Valid @RequestBody UsuarioRequest datos) {
        return servicio.actualizarPerfil(idSesion(token), datos);
    }

    @GetMapping
    @Operation(summary = "Lista los usuarios (Administrador)")
    public List<Usuario> listar() {
        return servicio.listar();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalle de un usuario (Administrador)")
    public Usuario obtener(@PathVariable Long id) {
        return servicio.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crea un usuario con cualquier rol (Administrador)")
    public Usuario crear(@Valid @RequestBody UsuarioRequest datos) {
        return servicio.crear(datos);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualiza un usuario; contraseña vacía = se mantiene (Administrador)")
    public Usuario actualizar(@PathVariable Long id, @Valid @RequestBody UsuarioRequest datos,
            @AuthenticationPrincipal Jwt token) {
        return servicio.actualizar(id, datos, idSesion(token));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Elimina un usuario, salvo la propia cuenta (Administrador)")
    public void eliminar(@PathVariable Long id, @AuthenticationPrincipal Jwt token) {
        servicio.eliminar(id, idSesion(token));
    }

    // El "sub" del token es el id del usuario
    private static Long idSesion(Jwt token) {
        return Long.valueOf(token.getSubject());
    }
}
