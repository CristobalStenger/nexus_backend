package cl.nexustech.pedido.controller;

import cl.nexustech.pedido.dto.CompraRequest;
import cl.nexustech.pedido.model.Boleta;
import cl.nexustech.pedido.service.BoletaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/boletas")
@RequiredArgsConstructor
@Tag(name = "Boletas", description = "Órdenes de compra")
public class BoletaController {

    private final BoletaService servicio;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Checkout (público, con o sin sesión). Devuelve la boleta con estado pagada o rechazada")
    public Boleta comprar(@Valid @RequestBody CompraRequest datos, @AuthenticationPrincipal Jwt token) {
        return servicio.comprar(datos, token);
    }

    @GetMapping
    @Operation(summary = "Lista las boletas, de la más nueva a la más antigua (Administrador y Vendedor)")
    public List<Boleta> listar() {
        return servicio.listar();
    }

    @GetMapping("/{numero}")
    @Operation(summary = "Detalle de una boleta (Administrador y Vendedor)")
    public Boleta obtener(@PathVariable Long numero) {
        return servicio.obtener(numero);
    }
}
