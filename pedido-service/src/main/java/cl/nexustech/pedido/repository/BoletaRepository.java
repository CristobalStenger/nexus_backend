package cl.nexustech.pedido.repository;

import cl.nexustech.pedido.model.Boleta;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoletaRepository extends JpaRepository<Boleta, Long> {

    // Trae los items en la misma consulta (evita una consulta por boleta)
    @EntityGraph(attributePaths = "items")
    List<Boleta> findAllByOrderByNumeroDesc();

    @EntityGraph(attributePaths = "items")
    Optional<Boleta> findByNumero(Long numero);
}
