package cl.nexustech.pedido.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

// Solo se guarda la tarjeta enmascarada (**** 4242), nunca el número completo ni el CVV
@Embeddable
public record Pago(@Column(name = "pago_tarjeta") String tarjeta) {
}
