package cl.nexustech.pedido.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// Datos del comprador copiados en la boleta. El run no lo envía el cliente:
// se toma del token (queda vacío si compra sin iniciar sesión).
@Embeddable
public record Cliente(
        @Column(name = "cliente_run")
        String run,

        @Column(name = "cliente_nombre")
        @NotBlank(message = "El nombre es obligatorio.")
        @Size(max = 50, message = "El nombre no puede superar los 50 caracteres.")
        String nombre,

        @Column(name = "cliente_apellidos")
        @NotBlank(message = "El apellido es obligatorio.")
        @Size(max = 100, message = "El apellido no puede superar los 100 caracteres.")
        String apellidos,

        @Column(name = "cliente_correo")
        @NotBlank(message = "El correo es obligatorio.")
        @Size(max = 100, message = "El correo no puede superar los 100 caracteres.")
        @Pattern(regexp = "(?i)^[^\\s@]+@(duoc\\.cl|profesor\\.duoc\\.cl|gmail\\.com)$",
                message = "Solo se aceptan correos @duoc.cl, @profesor.duoc.cl o @gmail.com")
        String correo) {
}
