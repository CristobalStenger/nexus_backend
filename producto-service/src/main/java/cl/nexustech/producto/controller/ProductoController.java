package cl.nexustech.producto.controller;

import cl.nexustech.producto.dto.ItemStock;
import cl.nexustech.producto.dto.ProductoRequest;
import cl.nexustech.producto.model.Producto;
import cl.nexustech.producto.service.ProductoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/productos")
@RequiredArgsConstructor
@Tag(name = "Productos", description = "Catálogo de la tienda")
public class ProductoController {

    private final ProductoService servicio;

    @GetMapping
    @Operation(summary = "Lista los productos (público). Con ?ids=1,2 devuelve solo esos")
    public List<Producto> listar(@RequestParam(required = false) List<Long> ids) {
        return servicio.listar(ids);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalle de un producto (público)")
    public Producto obtener(@PathVariable Long id) {
        return servicio.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crea un producto (Administrador)")
    public Producto crear(@Valid @RequestBody ProductoRequest datos) {
        return servicio.guardar(null, datos);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualiza un producto (Administrador)")
    public Producto actualizar(@PathVariable Long id, @Valid @RequestBody ProductoRequest datos) {
        return servicio.guardar(id, datos);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Elimina un producto (Administrador)")
    public void eliminar(@PathVariable Long id) {
        servicio.eliminar(id);
    }

    @PostMapping("/stock/descontar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Tag(name = "Stock (interno)")
    @Operation(summary = "Descuenta el stock de una compra pagada (solo pedido-service). 409 si no alcanza")
    public void descontarStock(@RequestBody @NotEmpty List<@Valid ItemStock> items) {
        servicio.descontarStock(items);
    }

    @PostMapping("/stock/reponer")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Tag(name = "Stock (interno)")
    @Operation(summary = "Devuelve el stock de una compra que no se pudo registrar (solo pedido-service)")
    public void reponerStock(@RequestBody @NotEmpty List<@Valid ItemStock> items) {
        servicio.reponerStock(items);
    }
}
