package cl.nexustech.producto.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String codigo;

    private String nombre;

    private String descripcion;

    private String tipo;

    private int precio;

    private int stock;

    private int stockCritico;

    // Porcentaje de descuento (0 a 90)
    private int descuento;

    private String imagen;

    @JsonIgnore
    @ManyToOne(optional = false)
    private Categoria categoria;

    // En el JSON la categoría va como texto, igual que en el frontend
    @JsonProperty("categoria")
    public String getNombreCategoria() {
        return categoria.getNombre();
    }

    public Long getCategoriaId() {
        return categoria.getId();
    }

    // Precio con el descuento aplicado (la misma fórmula que precioFinal en el frontend)
    public int getPrecioFinal() {
        return (int) Math.round(precio * (1 - descuento / 100.0));
    }
}
