package cl.nexustech.producto.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

// Un producto y cuántas unidades descontar o reponer (lo usa pedido-service)
public record ItemStock(
        @NotNull Long productoId,
        @NotNull @Min(1) Integer cantidad) {
}
