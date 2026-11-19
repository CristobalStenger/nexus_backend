package cl.nexustech.pedido.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

// Una línea de la boleta: copia del producto al momento de comprar
@Entity
@Getter
@Setter
public class DetalleBoleta {

    @JsonIgnore
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(optional = false)
    @JoinColumn(name = "boleta_numero")
    private Boleta boleta;

    private Long productoId;

    private String codigo;

    private String nombre;

    private String categoria;

    private String imagen;

    // Precio unitario con el descuento del producto ya aplicado
    private int precio;

    private int cantidad;
}
