package cl.nexustech.producto.service;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import cl.nexustech.producto.dto.ItemStock;
import cl.nexustech.producto.dto.ProductoRequest;
import cl.nexustech.producto.model.Producto;
import cl.nexustech.producto.repository.CategoriaRepository;
import cl.nexustech.producto.repository.ProductoRepository;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ProductoService {

    private final ProductoRepository productos;
    private final CategoriaRepository categorias;

    // Sin ids devuelve todo el catálogo; con ids, solo esos productos (lo usa pedido-service)
    public List<Producto> listar(List<Long> ids) {
        return ids == null || ids.isEmpty() ? productos.findAll(Sort.by("id")) : productos.findAllById(ids);
    }

    public Producto obtener(Long id) {
        return productos.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "El producto no existe."));
    }

    // id null: crea un producto nuevo. Si no, actualiza el existente.
    @Transactional
    public Producto guardar(Long id, ProductoRequest datos) {
        Producto producto = id == null ? new Producto() : obtener(id);
        String codigo = datos.codigo().trim().toUpperCase();
        productos.findByCodigo(codigo)
                .filter(otro -> !otro.getId().equals(id))
                .ifPresent(otro -> {
                    throw new ResponseStatusException(CONFLICT, "Ya existe un producto con ese código.");
                });

        producto.setCodigo(codigo);
        producto.setNombre(datos.nombre().trim());
        producto.setDescripcion(texto(datos.descripcion()));
        producto.setTipo(datos.tipo().trim());
        producto.setPrecio(datos.precio());
        producto.setStock(datos.stock());
        producto.setStockCritico(Objects.requireNonNullElse(datos.stockCritico(), 0));
        producto.setDescuento(Objects.requireNonNullElse(datos.descuento(), 0));
        producto.setImagen(texto(datos.imagen()));
        producto.setCategoria(categorias.findById(datos.categoriaId())
                .orElseThrow(() -> new ResponseStatusException(BAD_REQUEST, "La categoría no existe.")));
        return productos.save(producto);
    }

    @Transactional
    public void eliminar(Long id) {
        productos.delete(obtener(id));
    }

    // Descuenta el stock de todos los productos o de ninguno: si alguno no alcanza,
    // la excepción deshace la transacción completa.
    @Transactional
    public void descontarStock(List<ItemStock> items) {
        List<Long> sinStock = ordenados(items).stream()
                .filter(item -> productos.descontarStock(item.productoId(), item.cantidad()) == 0)
                .map(ItemStock::productoId)
                .toList();
        if (!sinStock.isEmpty())
            throw new ResponseStatusException(CONFLICT,
                    "Stock insuficiente para: " + nombres(sinStock) + ". Revisa las cantidades de tu carrito.");
    }

    // Devuelve el stock de una compra que no se pudo registrar
    @Transactional
    public void reponerStock(List<ItemStock> items) {
        ordenados(items).forEach(item -> productos.reponerStock(item.productoId(), item.cantidad()));
    }

    // Siempre en el mismo orden para que dos compras simultáneas no se bloqueen entre sí
    private static List<ItemStock> ordenados(List<ItemStock> items) {
        return items.stream().sorted(Comparator.comparing(ItemStock::productoId)).toList();
    }

    private String nombres(List<Long> ids) {
        Map<Long, String> encontrados = productos.findAllById(ids).stream()
                .collect(Collectors.toMap(Producto::getId, Producto::getNombre));
        return ids.stream()
                .map(id -> encontrados.getOrDefault(id, "Producto " + id))
                .collect(Collectors.joining(", "));
    }

    private static String texto(String valor) {
        return valor == null ? "" : valor.trim();
    }
}
