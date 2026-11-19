package cl.nexustech.usuario.controller;

import cl.nexustech.usuario.dto.LoginRequest;
import cl.nexustech.usuario.dto.LoginResponse;
import cl.nexustech.usuario.dto.UsuarioRequest;
import cl.nexustech.usuario.model.Usuario;
import cl.nexustech.usuario.service.AuthService;
import cl.nexustech.usuario.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Login (entrega el JWT) y registro de clientes")
public class AuthController {

    private final AuthService auth;
    private final UsuarioService usuarios;

    @PostMapping("/login")
    @Operation(summary = "Inicia sesión y devuelve el token JWT (público)")
    public LoginResponse login(@Valid @RequestBody LoginRequest datos) {
        return auth.login(datos);
    }

    @PostMapping("/registro")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crea una cuenta de Cliente desde la tienda (público)")
    public Usuario registro(@Valid @RequestBody UsuarioRequest datos) {
        return usuarios.registrar(datos);
    }
}
