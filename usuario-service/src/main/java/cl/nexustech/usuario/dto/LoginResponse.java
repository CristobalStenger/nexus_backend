package cl.nexustech.usuario.dto;

import cl.nexustech.usuario.model.Usuario;

// token: se envía en cada petición como "Authorization: Bearer <token>"
public record LoginResponse(String token, Usuario usuario) {
}
