package cl.nexustech.pedido.controller;

import static org.springframework.http.HttpStatus.NOT_FOUND;

import cl.nexustech.pedido.model.Cupon;
import cl.nexustech.pedido.service.BoletaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/cupones")
@RequiredArgsConstructor
@Tag(name = "Cupones")
public class CuponController {

    private final BoletaService servicio;

    @GetMapping("/{codigo}")
    @Operation(summary = "Valida un cupón de descuento para el carrito (público)")
    public Cupon obtener(@PathVariable String codigo) {
        return servicio.buscarCupon(codigo).orElseThrow(() -> new ResponseStatusException(NOT_FOUND,
                "El cupón \"" + codigo.trim().toUpperCase() + "\" no existe."));
    }
}
