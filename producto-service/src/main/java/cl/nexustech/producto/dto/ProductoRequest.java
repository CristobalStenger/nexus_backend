package cl.nexustech.producto.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

// Datos que envía el administrador al crear o editar un producto
public record ProductoRequest(
        @NotBlank(message = "El código es obligatorio.")
        @Size(min = 3, max = 30, message = "El código debe tener entre 3 y 30 caracteres.")
        String codigo,

        @NotBlank(message = "El nombre es obligatorio.")
        @Size(max = 100, message = "El nombre no puede superar los 100 caracteres.")
        String nombre,

        @Size(max = 500, message = "La descripción no puede superar los 500 caracteres.")
        String descripcion,

        @NotBlank(message = "El tipo es obligatorio.")
        @Size(max = 50, message = "El tipo no puede superar los 50 caracteres.")
        String tipo,

        @NotNull(message = "El precio es obligatorio.")
        @PositiveOrZero(message = "El precio no puede ser negativo.")
        Integer precio,

        @NotNull(message = "El stock es obligatorio.")
        @PositiveOrZero(message = "El stock no puede ser negativo.")
        Integer stock,

        @PositiveOrZero(message = "El stock crítico no puede ser negativo.")
        Integer stockCritico,

        @Min(value = 0, message = "El descuento no puede ser negativo.")
        @Max(value = 90, message = "El descuento no puede superar 90.")
        Integer descuento,

        @Size(max = 500, message = "La URL de la imagen no puede superar los 500 caracteres.")
        @Pattern(regexp = "^(https?://\\S+)?$", message = "Ingresa una URL de imagen http o https.")
        String imagen,

        @NotNull(message = "Selecciona una categoría.")
        Long categoriaId) {
}
