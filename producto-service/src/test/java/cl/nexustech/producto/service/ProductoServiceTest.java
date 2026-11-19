package cl.nexustech.producto.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

import cl.nexustech.producto.dto.ItemStock;
import cl.nexustech.producto.dto.ProductoRequest;
import cl.nexustech.producto.model.Categoria;
import cl.nexustech.producto.model.Producto;
import cl.nexustech.producto.repository.CategoriaRepository;
import cl.nexustech.producto.repository.ProductoRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock
    ProductoRepository productos;

    @Mock
    CategoriaRepository categorias;

    @InjectMocks
    ProductoService servicio;

    private static ProductoRequest datos(String codigo) {
        return new ProductoRequest(codigo, " Mouse ", null, "Mouse", 10000, 5, null, null, null, 1L);
    }

    @Test
    void guardaElCodigoEnMayusculasYCompletaLosValoresPorDefecto() {
        Categoria perifericos = new Categoria();
        perifericos.setId(1L);
        when(productos.findByCodigo("MOU001")).thenReturn(Optional.empty());
        when(categorias.findById(1L)).thenReturn(Optional.of(perifericos));
        when(productos.save(any())).thenAnswer(i -> i.getArgument(0));

        Producto creado = servicio.guardar(null, datos(" mou001 "));

        assertThat(creado.getCodigo()).isEqualTo("MOU001");
        assertThat(creado.getNombre()).isEqualTo("Mouse");
        assertThat(creado.getStockCritico()).isZero();
        assertThat(creado.getDescuento()).isZero();
        assertThat(creado.getDescripcion()).isEmpty();
        assertThat(creado.getCategoria()).isSameAs(perifericos);
    }

    @Test
    void rechazaUnCodigoRepetido() {
        Producto otro = new Producto();
        otro.setId(7L);
        when(productos.findByCodigo("MOU001")).thenReturn(Optional.of(otro));

        assertThatThrownBy(() -> servicio.guardar(null, datos("MOU001")))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Ya existe un producto con ese código.");
    }

    @Test
    void elPrecioFinalAplicaElDescuentoYRedondea() {
        Producto producto = new Producto();
        producto.setPrecio(419990);
        producto.setDescuento(10);
        assertThat(producto.getPrecioFinal()).isEqualTo(377991);
    }

    @Test
    void siNoAlcanzaElStockListaLosProductosYLanza409() {
        Producto ryzen = new Producto();
        ryzen.setId(2L);
        ryzen.setNombre("AMD Ryzen 7 7800X3D");
        when(productos.descontarStock(1L, 1)).thenReturn(1);
        when(productos.descontarStock(2L, 50)).thenReturn(0);
        when(productos.findAllById(List.of(2L))).thenReturn(List.of(ryzen));

        // Llegan desordenados: se descuentan por id para evitar bloqueos entre compras
        assertThatThrownBy(() -> servicio.descontarStock(List.of(new ItemStock(2L, 50), new ItemStock(1L, 1))))
                .isInstanceOfSatisfying(ResponseStatusException.class, e -> {
                    assertThat(e.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
                    assertThat(e.getReason()).isEqualTo(
                            "Stock insuficiente para: AMD Ryzen 7 7800X3D. Revisa las cantidades de tu carrito.");
                });
        InOrder orden = inOrder(productos);
        orden.verify(productos).descontarStock(1L, 1);
        orden.verify(productos).descontarStock(2L, 50);
    }
}
