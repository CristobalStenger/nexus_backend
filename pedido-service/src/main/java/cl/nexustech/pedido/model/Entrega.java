package cl.nexustech.pedido.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Embeddable
public record Entrega(
        @Column(name = "entrega_tipo")
        @NotBlank(message = "Elige una opción de entrega.")
        @Pattern(regexp = "despacho|retiro", message = "Elige una opción de entrega.")
        String tipo,

        @Column(name = "entrega_calle")
        @NotBlank(message = "La calle es obligatoria.")
        @Size(max = 300, message = "La calle no puede superar los 300 caracteres.")
        String calle,

        @Column(name = "entrega_departamento")
        @Size(max = 50, message = "Máximo 50 caracteres.")
        String departamento,

        @Column(name = "entrega_region")
        @NotBlank(message = "Selecciona una región.")
        @Size(max = 100, message = "La región no puede superar los 100 caracteres.")
        String region,

        @Column(name = "entrega_comuna")
        @NotBlank(message = "Selecciona una comuna.")
        @Size(max = 100, message = "La comuna no puede superar los 100 caracteres.")
        String comuna,

        @Column(name = "entrega_indicaciones")
        @Size(max = 300, message = "Máximo 300 caracteres.")
        String indicaciones) {
}
