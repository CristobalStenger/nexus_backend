package cl.nexustech.pedido.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

// Boleta (orden de compra). El JSON tiene la misma forma que las órdenes del frontend.
@Entity
@Getter
@Setter
public class Boleta {

    public static final String PAGADA = "pagada";
    public static final String RECHAZADA = "rechazada";
    public static final ZoneId ZONA_CHILE = ZoneId.of("America/Santiago");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long numero;

    private Instant fecha;

    private String estado;

    // Por qué se rechazó el pago (vacío si se pagó)
    private String motivo;

    // Id del usuario en usuario-service; null si compró sin iniciar sesión
    private Long usuarioId;

    @Embedded
    private Cliente cliente;

    @Embedded
    private Entrega entrega;

    @Embedded
    private Pago pago;

    @Embedded
    private Cupon cupon;

    @OneToMany(mappedBy = "boleta", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id")
    private List<DetalleBoleta> items = new ArrayList<>();

    private int subtotal;

    private int descuento;

    private int envio;

    private int total;

    // ORD-20260914-1001: fecha (hora de Chile) y número
    public String getCodigo() {
        return "ORD-" + DateTimeFormatter.ofPattern("yyyyMMdd").format(fecha.atZone(ZONA_CHILE)) + "-" + numero;
    }

    public void agregarItem(DetalleBoleta item) {
        item.setBoleta(this);
        items.add(item);
    }
}
