package cl.nexustech.usuario.dto;

import cl.nexustech.usuario.model.Rol;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

// Registro, creación/edición por el administrador y edición del propio perfil.
// La contraseña es opcional al editar (vacía = se mantiene) y el tipo solo lo usa el administrador.
public record UsuarioRequest(
        @NotBlank(message = "El RUN es obligatorio.")
        @Pattern(regexp = "^[0-9]{6,8}[0-9Kk]$",
                message = "Escribe el RUN sin puntos ni guion, entre 7 y 9 caracteres. Ejemplo: 21456789K")
        String run,

        @NotBlank(message = "El nombre es obligatorio.")
        @Size(max = 50, message = "El nombre no puede superar los 50 caracteres.")
        String nombre,

        @NotBlank(message = "El apellido es obligatorio.")
        @Size(max = 100, message = "El apellido no puede superar los 100 caracteres.")
        String apellidos,

        @NotBlank(message = "El correo es obligatorio.")
        @Size(max = 100, message = "El correo no puede superar los 100 caracteres.")
        @Pattern(regexp = "(?i)^[^\\s@]+@(duoc\\.cl|profesor\\.duoc\\.cl|gmail\\.com)$",
                message = "Solo se aceptan correos @duoc.cl, @profesor.duoc.cl o @gmail.com")
        String correo,

        @Size(min = 4, max = 10, message = "La contraseña debe tener entre 4 y 10 caracteres.")
        String clave,

        @PastOrPresent(message = "Ingresa una fecha de nacimiento válida.")
        LocalDate fechaNacimiento,

        Rol tipo,

        @NotBlank(message = "Selecciona una región.")
        @Size(max = 100, message = "La región no puede superar los 100 caracteres.")
        String region,

        @NotBlank(message = "Selecciona una comuna.")
        @Size(max = 100, message = "La comuna no puede superar los 100 caracteres.")
        String comuna,

        @NotBlank(message = "La dirección es obligatoria.")
        @Size(max = 300, message = "La dirección no puede superar los 300 caracteres.")
        String direccion) {
}
