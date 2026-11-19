package cl.nexustech.pedido.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// Lo que pedido-service necesita de un producto (respuesta de producto-service)
@JsonIgnoreProperties(ignoreUnknown = true)
public record ProductoDto(Long id, String codigo, String nombre, String categoria, String imagen,
        int stock, int precioFinal) {
}
