package cl.nexustech.pedido.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cl.nexustech.pedido.client.ProductoClient;
import cl.nexustech.pedido.dto.CompraRequest;
import cl.nexustech.pedido.dto.ItemCompra;
import cl.nexustech.pedido.dto.ProductoDto;
import cl.nexustech.pedido.dto.Tarjeta;
import cl.nexustech.pedido.model.Boleta;
import cl.nexustech.pedido.model.Cliente;
import cl.nexustech.pedido.model.Entrega;
import cl.nexustech.pedido.repository.BoletaRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class BoletaServiceTest {

    private static final String TARJETA_OK = "4242 4242 4242 4242";
    private static final ProductoDto RYZEN =
            new ProductoDto(2L, "CPU002", "AMD Ryzen 7 7800X3D", "Componentes", "img", 8, 377991);
    private static final ProductoDto RAM =
            new ProductoDto(9L, "RAM001", "Corsair Vengeance DDR5", "Componentes", "img", 20, 139990);

    @Mock
    BoletaRepository boletas;

    @Mock
    ProductoClient productos;

    BoletaService servicio;

    @BeforeEach
    void preparar() {
        Clock reloj = Clock.fixed(Instant.parse("2026-11-19T15:00:00Z"), ZoneOffset.UTC);
        servicio = new BoletaService(boletas, productos, reloj);
    }

    private static CompraRequest compra(String entrega, String cupon, String tarjeta, String vencimiento,
            ItemCompra... items) {
        return new CompraRequest(
                new Cliente("ignorado", " Ana ", "Pérez", " ANA@gmail.com "),
                new Entrega(entrega, "Calle 1", null, "Región Metropolitana", "Santiago", null),
                List.of(items), cupon, new Tarjeta(tarjeta, "Ana Pérez", vencimiento, "123"));
    }

    private void guardarDevuelveLaBoleta() {
        when(boletas.save(any())).thenAnswer(i -> {
            Boleta boleta = i.getArgument(0);
            boleta.setNumero(1006L); // lo asigna la base de datos
            return boleta;
        });
    }

    @Test
    void compraPagadaConCuponYDespacho() {
        when(productos.buscar(Set.of(2L, 9L))).thenReturn(List.of(RYZEN, RAM));
        when(productos.descontarStock(Map.of(2L, 2, 9L, 1))).thenReturn(Optional.empty());
        guardarDevuelveLaBoleta();
        Jwt token = Jwt.withTokenValue("t").header("alg", "HS256").subject("3").claim("run", "21456789K").build();

        // El mismo producto dos veces: se suman las cantidades
        Boleta boleta = servicio.comprar(compra("despacho", "feliz18", TARJETA_OK, "12/30",
                new ItemCompra(2L, 1), new ItemCompra(9L, 1), new ItemCompra(2L, 1)), token);

        assertThat(boleta.getEstado()).isEqualTo(Boleta.PAGADA);
        assertThat(boleta.getMotivo()).isEmpty();
        assertThat(boleta.getSubtotal()).isEqualTo(377991 * 2 + 139990);
        assertThat(boleta.getDescuento()).isEqualTo(89597); // 10% de 895.972, redondeado
        assertThat(boleta.getEnvio()).isEqualTo(BoletaService.COSTO_DESPACHO);
        assertThat(boleta.getTotal()).isEqualTo(895972 - 89597 + 3990);
        assertThat(boleta.getItems()).hasSize(2);
        assertThat(boleta.getPago().tarjeta()).isEqualTo("**** 4242");
        // El run sale del token, no de lo que envió el navegador
        assertThat(boleta.getCliente().run()).isEqualTo("21456789K");
        assertThat(boleta.getCliente().correo()).isEqualTo("ana@gmail.com");
        assertThat(boleta.getUsuarioId()).isEqualTo(3L);
        assertThat(boleta.getCodigo()).isEqualTo("ORD-20261119-1006");
    }

    @Test
    void laTarjetaTerminadaEn0000SeRechazaSinTocarElStock() {
        when(productos.buscar(Set.of(2L))).thenReturn(List.of(RYZEN));
        guardarDevuelveLaBoleta();

        Boleta boleta = servicio.comprar(compra("retiro", null, "4242424242420000", "12/30", new ItemCompra(2L, 1)), null);

        assertThat(boleta.getEstado()).isEqualTo(Boleta.RECHAZADA);
        assertThat(boleta.getMotivo()).isEqualTo("La tarjeta fue rechazada por el banco emisor.");
        assertThat(boleta.getEnvio()).isZero();
        assertThat(boleta.getCliente().run()).isEmpty();
        verify(productos, never()).descontarStock(anyMap());
    }

    @Test
    void sinStockSuficienteSeRechazaConElNombreDelProducto() {
        when(productos.buscar(Set.of(2L))).thenReturn(List.of(RYZEN));
        guardarDevuelveLaBoleta();

        Boleta boleta = servicio.comprar(compra("retiro", null, TARJETA_OK, "12/30", new ItemCompra(2L, 50)), null);

        assertThat(boleta.getEstado()).isEqualTo(Boleta.RECHAZADA);
        assertThat(boleta.getMotivo())
                .isEqualTo("Stock insuficiente para: AMD Ryzen 7 7800X3D. Revisa las cantidades de tu carrito.");
        verify(productos, never()).descontarStock(anyMap());
    }

    @Test
    void siOtraCompraSeLlevoElStockSeRechazaConElMotivoDeProductoService() {
        when(productos.buscar(Set.of(2L))).thenReturn(List.of(RYZEN));
        when(productos.descontarStock(Map.of(2L, 1))).thenReturn(Optional.of("Stock insuficiente para: AMD Ryzen 7 7800X3D."));
        guardarDevuelveLaBoleta();

        Boleta boleta = servicio.comprar(compra("retiro", null, TARJETA_OK, "12/30", new ItemCompra(2L, 1)), null);

        assertThat(boleta.getEstado()).isEqualTo(Boleta.RECHAZADA);
        assertThat(boleta.getMotivo()).startsWith("Stock insuficiente");
    }

    @Test
    void siLaBoletaNoSePuedeGuardarSeDevuelveElStock() {
        when(productos.buscar(Set.of(2L))).thenReturn(List.of(RYZEN));
        when(productos.descontarStock(Map.of(2L, 1))).thenReturn(Optional.empty());
        when(boletas.save(any())).thenThrow(new IllegalStateException("base de datos caída"));

        assertThatThrownBy(() -> servicio.comprar(compra("retiro", null, TARJETA_OK, "12/30", new ItemCompra(2L, 1)), null))
                .isInstanceOf(IllegalStateException.class);
        verify(productos).reponerStock(Map.of(2L, 1));
    }

    @Test
    void unCuponInexistenteDevuelve400() {
        assertThatThrownBy(() -> servicio.comprar(compra("retiro", "nada", TARJETA_OK, "12/30", new ItemCompra(2L, 1)), null))
                .isInstanceOfSatisfying(ResponseStatusException.class, e -> {
                    assertThat(e.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(e.getReason()).isEqualTo("El cupón \"NADA\" no existe.");
                });
    }

    @Test
    void unaTarjetaVencidaDevuelve400() {
        // El reloj marca noviembre de 2026: 11/26 aún sirve, 10/26 no
        assertThatThrownBy(() -> servicio.comprar(compra("retiro", null, TARJETA_OK, "10/26", new ItemCompra(2L, 1)), null))
                .hasMessageContaining("La tarjeta está vencida.");
    }

    @Test
    void siProductoServiceNoRespondeDevuelve503() {
        when(productos.buscar(Set.of(2L))).thenThrow(new ResourceAccessException("timeout"));

        assertThatThrownBy(() -> servicio.comprar(compra("retiro", null, TARJETA_OK, "11/26", new ItemCompra(2L, 1)), null))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        e -> assertThat(e.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE));
        verify(boletas, never()).save(any());
    }
}
