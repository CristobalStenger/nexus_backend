package cl.nexustech.pedido.service;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;

import cl.nexustech.pedido.client.ProductoClient;
import cl.nexustech.pedido.dto.CompraRequest;
import cl.nexustech.pedido.dto.ItemCompra;
import cl.nexustech.pedido.dto.ProductoDto;
import cl.nexustech.pedido.model.Boleta;
import cl.nexustech.pedido.model.Cliente;
import cl.nexustech.pedido.model.Cupon;
import cl.nexustech.pedido.model.DetalleBoleta;
import cl.nexustech.pedido.model.Entrega;
import cl.nexustech.pedido.model.Pago;
import cl.nexustech.pedido.repository.BoletaRepository;
import java.time.Clock;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class BoletaService {

    // Reglas fijas del negocio (las mismas del frontend: data/configuracion.js y data/cupones.js)
    public static final int COSTO_DESPACHO = 3990;
    public static final String SUFIJO_TARJETA_RECHAZADA = "0000";
    public static final Map<String, Integer> CUPONES =
            Map.of("FELIZ18", 10, "CUTURRUFO70", 25, "BOOTSTRAP", 5, "XAVICRIS", 15);

    private final BoletaRepository boletas;
    private final ProductoClient productos;
    private final Clock reloj;

    public List<Boleta> listar() {
        return boletas.findAllByOrderByNumeroDesc();
    }

    public Boleta obtener(Long numero) {
        return boletas.findByNumero(numero)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "La boleta no existe."));
    }

    public Optional<Cupon> buscarCupon(String codigo) {
        String escrito = codigo == null ? "" : codigo.trim().toUpperCase();
        return Optional.ofNullable(CUPONES.get(escrito)).map(descuento -> new Cupon(escrito, descuento));
    }

    // Checkout. Siempre registra la boleta: "pagada" (descuenta stock) o "rechazada"
    // (por stock o tarjeta, sin tocar el stock), igual que la tienda del frontend.
    // token: null si el cliente compra sin iniciar sesión.
    public Boleta comprar(CompraRequest datos, Jwt token) {
        validarVencimiento(datos.pago().vencimiento());
        Cupon cupon = datos.cupon() == null || datos.cupon().isBlank() ? null
                : buscarCupon(datos.cupon()).orElseThrow(() -> new ResponseStatusException(BAD_REQUEST,
                        "El cupón \"" + datos.cupon().trim().toUpperCase() + "\" no existe."));

        // Si el mismo producto viene dos veces se suman las cantidades
        Map<Long, Integer> cantidades = datos.items().stream()
                .collect(Collectors.toMap(ItemCompra::productoId, ItemCompra::cantidad, Integer::sum, LinkedHashMap::new));

        try {
            Map<Long, ProductoDto> catalogo = productos.buscar(cantidades.keySet()).stream()
                    .collect(Collectors.toMap(ProductoDto::id, Function.identity()));

            Boleta boleta = armarBoleta(datos, token, cupon, cantidades, catalogo);
            String motivo = motivoRechazo(cantidades, catalogo, datos.pago().numeroTarjeta());
            if (motivo.isEmpty())
                motivo = productos.descontarStock(cantidades).orElse("");
            boleta.setEstado(motivo.isEmpty() ? Boleta.PAGADA : Boleta.RECHAZADA);
            boleta.setMotivo(motivo);

            try {
                return boletas.save(boleta);
            } catch (RuntimeException e) {
                // El stock ya se descontó pero la boleta no quedó guardada: se devuelve
                if (motivo.isEmpty()) productos.reponerStock(cantidades);
                throw e;
            }
        } catch (RestClientException e) {
            throw new ResponseStatusException(SERVICE_UNAVAILABLE,
                    "No se pudo conectar con el servicio de productos. Intenta nuevamente.", e);
        }
    }

    private Boleta armarBoleta(CompraRequest datos, Jwt token, Cupon cupon, Map<Long, Integer> cantidades,
            Map<Long, ProductoDto> catalogo) {
        Cliente c = datos.cliente();
        Entrega e = datos.entrega();
        Boleta boleta = new Boleta();
        boleta.setFecha(reloj.instant().truncatedTo(ChronoUnit.MILLIS));
        boleta.setUsuarioId(token == null ? null : Long.valueOf(token.getSubject()));
        boleta.setCliente(new Cliente(token == null ? "" : token.getClaimAsString("run"),
                c.nombre().trim(), c.apellidos().trim(), c.correo().trim().toLowerCase()));
        boleta.setEntrega(new Entrega(e.tipo(), e.calle().trim(), texto(e.departamento()), e.region(),
                e.comuna(), texto(e.indicaciones())));
        boleta.setPago(new Pago("**** " + digitos(datos.pago().numeroTarjeta()).substring(12)));
        boleta.setCupon(cupon);

        // Los productos que ya no existen quedan fuera de la boleta (y la rechazan)
        cantidades.forEach((id, cantidad) -> {
            ProductoDto p = catalogo.get(id);
            if (p == null) return;
            DetalleBoleta item = new DetalleBoleta();
            item.setProductoId(p.id());
            item.setCodigo(p.codigo());
            item.setNombre(p.nombre());
            item.setCategoria(p.categoria());
            item.setImagen(p.imagen());
            item.setPrecio(p.precioFinal());
            item.setCantidad(cantidad);
            boleta.agregarItem(item);
        });

        // Mismos cálculos que resumenCarrito en el frontend
        long subtotal = boleta.getItems().stream().mapToLong(i -> (long) i.getPrecio() * i.getCantidad()).sum();
        long descuento = cupon == null ? 0 : Math.round(subtotal * cupon.descuento() / 100.0);
        long envio = "despacho".equals(e.tipo()) && !boleta.getItems().isEmpty() ? COSTO_DESPACHO : 0;
        boleta.setSubtotal(Math.toIntExact(subtotal));
        boleta.setDescuento(Math.toIntExact(descuento));
        boleta.setEnvio(Math.toIntExact(envio));
        boleta.setTotal(Math.toIntExact(subtotal - descuento + envio));
        return boleta;
    }

    // Devuelve por qué se rechaza el pago, o "" si se puede cobrar
    static String motivoRechazo(Map<Long, Integer> cantidades, Map<Long, ProductoDto> catalogo, String tarjeta) {
        String sinStock = cantidades.entrySet().stream()
                .filter(e -> !catalogo.containsKey(e.getKey()) || e.getValue() > catalogo.get(e.getKey()).stock())
                .map(e -> catalogo.containsKey(e.getKey()) ? catalogo.get(e.getKey()).nombre() : "Producto " + e.getKey())
                .collect(Collectors.joining(", "));
        if (!sinStock.isEmpty())
            return "Stock insuficiente para: " + sinStock + ". Revisa las cantidades de tu carrito.";
        if (digitos(tarjeta).endsWith(SUFIJO_TARJETA_RECHAZADA))
            return "La tarjeta fue rechazada por el banco emisor.";
        return "";
    }

    // La tarjeta vale hasta el último día del mes indicado (MM/AA)
    private void validarVencimiento(String vencimiento) {
        YearMonth mes = YearMonth.parse(vencimiento, DateTimeFormatter.ofPattern("MM/yy"));
        if (mes.isBefore(YearMonth.now(reloj.withZone(Boleta.ZONA_CHILE))))
            throw new ResponseStatusException(BAD_REQUEST, "La tarjeta está vencida.");
    }

    private static String digitos(String tarjeta) {
        return tarjeta.replaceAll("\\D", "");
    }

    private static String texto(String valor) {
        return valor == null ? "" : valor.trim();
    }
}
