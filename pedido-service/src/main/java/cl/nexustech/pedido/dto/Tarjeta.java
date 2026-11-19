package cl.nexustech.pedido.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// Pago simulado: no se cobra nada. Las tarjetas terminadas en 0000 se rechazan.
public record Tarjeta(
        @NotBlank(message = "La tarjeta debe tener 16 dígitos.")
        @Pattern(regexp = "^(\\d{4} ?){3}\\d{4}$", message = "La tarjeta debe tener 16 dígitos.")
        String numeroTarjeta,

        @NotBlank(message = "El titular es obligatorio.")
        @Size(max = 100, message = "El titular no puede superar los 100 caracteres.")
        String titular,

        @NotBlank(message = "Usa el formato MM/AA.")
        @Pattern(regexp = "^(0[1-9]|1[0-2])/\\d{2}$", message = "Usa el formato MM/AA.")
        String vencimiento,

        @NotBlank(message = "El CVV tiene 3 o 4 dígitos.")
        @Pattern(regexp = "^\\d{3,4}$", message = "El CVV tiene 3 o 4 dígitos.")
        String cvv) {
}
