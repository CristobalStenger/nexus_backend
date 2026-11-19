package cl.nexustech.producto.repository;

import cl.nexustech.producto.model.Producto;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    Optional<Producto> findByCodigo(String codigo);

    long countByCategoriaId(Long categoriaId);

    // Descuenta solo si alcanza el stock. Devuelve 0 si no se pudo, así dos compras
    // simultáneas nunca dejan el stock negativo.
    @Modifying
    @Query("update Producto p set p.stock = p.stock - :cantidad where p.id = :id and p.stock >= :cantidad")
    int descontarStock(Long id, int cantidad);

    @Modifying
    @Query("update Producto p set p.stock = p.stock + :cantidad where p.id = :id")
    int reponerStock(Long id, int cantidad);
}
