package cl.nexustech.producto.service;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import cl.nexustech.producto.dto.CategoriaRequest;
import cl.nexustech.producto.model.Categoria;
import cl.nexustech.producto.repository.CategoriaRepository;
import cl.nexustech.producto.repository.ProductoRepository;
import java.text.Normalizer;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class CategoriaService {

    private final CategoriaRepository categorias;
    private final ProductoRepository productos;

    public List<Categoria> listar() {
        return categorias.findAll(Sort.by("id"));
    }

    public Categoria obtener(Long id) {
        return categorias.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "La categoría no existe."));
    }

    @Transactional
    public Categoria guardar(Long id, CategoriaRequest datos) {
        Categoria categoria = id == null ? new Categoria() : obtener(id);
        String nombre = datos.nombre().trim();
        String slug = crearSlug(nombre);
        if (slug.isEmpty())
            throw new ResponseStatusException(BAD_REQUEST, "El nombre debe tener letras o números.");
        // Se compara el slug: dos nombres que solo difieren en tildes chocarían en la URL
        categorias.findBySlug(slug)
                .filter(otra -> !otra.getId().equals(id))
                .ifPresent(otra -> {
                    throw new ResponseStatusException(CONFLICT, "Ya existe una categoría con ese nombre.");
                });

        categoria.setNombre(nombre);
        categoria.setSlug(slug);
        categoria.setIcono(datos.icono() == null || datos.icono().isBlank() ? "bi-tag" : datos.icono());
        categoria.setDescripcion(datos.descripcion() == null ? "" : datos.descripcion().trim());
        return categorias.save(categoria);
    }

    @Transactional
    public void eliminar(Long id) {
        Categoria categoria = obtener(id);
        long enUso = productos.countByCategoriaId(id);
        if (enUso > 0)
            throw new ResponseStatusException(CONFLICT,
                    "No se puede eliminar \"" + categoria.getNombre() + "\": tiene " + enUso + " producto(s).");
        categorias.delete(categoria);
    }

    // "Periféricos y Audio" -> "perifericos-y-audio" (igual que crearSlug del frontend)
    static String crearSlug(String texto) {
        return Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("[\\u0300-\\u036f]", "")
                .toLowerCase()
                .trim()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");
    }
}
