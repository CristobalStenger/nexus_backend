package cl.nexustech.pedido.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

// descuento: porcentaje sobre el subtotal
@Embeddable
public record Cupon(
        @Column(name = "cupon_codigo") String codigo,
        @Column(name = "cupon_descuento") Integer descuento) {
}
