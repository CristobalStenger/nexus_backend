package cl.nexustech.pedido.dto;

import cl.nexustech.pedido.model.Cliente;
import cl.nexustech.pedido.model.Entrega;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

// Lo que envía el checkout. Los precios NO vienen del navegador:
// el backend los consulta en producto-service.
public record CompraRequest(
        @NotNull(message = "Faltan los datos del cliente.") @Valid Cliente cliente,
        @NotNull(message = "Faltan los datos de entrega.") @Valid Entrega entrega,
        @NotEmpty(message = "El carrito está vacío.") List<@Valid ItemCompra> items,
        String cupon,
        @NotNull(message = "Faltan los datos de la tarjeta.") @Valid Tarjeta pago) {
}
