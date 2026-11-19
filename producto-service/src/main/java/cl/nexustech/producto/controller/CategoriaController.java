package cl.nexustech.producto.controller;

import cl.nexustech.producto.dto.CategoriaRequest;
import cl.nexustech.producto.model.Categoria;
import cl.nexustech.producto.service.CategoriaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/categorias")
@RequiredArgsConstructor
@Tag(name = "Categorías")
public class CategoriaController {

    private final CategoriaService servicio;

    @GetMapping
    @Operation(summary = "Lista las categorías (público)")
    public List<Categoria> listar() {
        return servicio.listar();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalle de una categoría (público)")
    public Categoria obtener(@PathVariable Long id) {
        return servicio.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crea una categoría (Administrador)")
    public Categoria crear(@Valid @RequestBody CategoriaRequest datos) {
        return servicio.guardar(null, datos);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualiza una categoría (Administrador)")
    public Categoria actualizar(@PathVariable Long id, @Valid @RequestBody CategoriaRequest datos) {
        return servicio.guardar(id, datos);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Elimina una categoría sin productos (Administrador). 409 si tiene productos")
    public void eliminar(@PathVariable Long id) {
        servicio.eliminar(id);
    }
}
