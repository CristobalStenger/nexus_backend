package cl.nexustech.producto.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CategoriaRequest(
        @NotBlank(message = "El nombre es obligatorio.")
        @Size(min = 3, max = 50, message = "El nombre debe tener entre 3 y 50 caracteres.")
        String nombre,

        @Pattern(regexp = "^(bi-[a-z0-9-]+)?$", message = "El ícono debe ser de Bootstrap Icons (ej. bi-tag).")
        @Size(max = 40, message = "El ícono no puede superar los 40 caracteres.")
        String icono,

        @Size(max = 200, message = "La descripción no puede superar los 200 caracteres.")
        String descripcion) {
}
