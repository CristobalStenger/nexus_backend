package cl.nexustech.producto.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cl.nexustech.producto.dto.CategoriaRequest;
import cl.nexustech.producto.model.Categoria;
import cl.nexustech.producto.repository.CategoriaRepository;
import cl.nexustech.producto.repository.ProductoRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class CategoriaServiceTest {

    @Mock
    CategoriaRepository categorias;

    @Mock
    ProductoRepository productos;

    @InjectMocks
    CategoriaService servicio;

    @Test
    void elSlugQuitaTildesEspaciosYSignos() {
        assertThat(CategoriaService.crearSlug("Periféricos y Audio")).isEqualTo("perifericos-y-audio");
        assertThat(CategoriaService.crearSlug("  ¡Ofertas!  ")).isEqualTo("ofertas");
    }

    @Test
    void creaLaCategoriaConSlugEIconoPorDefecto() {
        when(categorias.findBySlug("consolas-retro")).thenReturn(Optional.empty());
        when(categorias.save(any())).thenAnswer(i -> i.getArgument(0));

        Categoria creada = servicio.guardar(null, new CategoriaRequest(" Consolas Retro ", null, null));

        assertThat(creada.getNombre()).isEqualTo("Consolas Retro");
        assertThat(creada.getSlug()).isEqualTo("consolas-retro");
        assertThat(creada.getIcono()).isEqualTo("bi-tag");
        assertThat(creada.getDescripcion()).isEmpty();
    }

    @Test
    void rechazaUnNombreQueSoloDifiereEnTildes() {
        Categoria existente = new Categoria();
        existente.setId(2L);
        when(categorias.findBySlug("perifericos")).thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> servicio.guardar(null, new CategoriaRequest("Perifericos", null, null)))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Ya existe una categoría con ese nombre.");
    }

    @Test
    void rechazaUnNombreSinLetrasNiNumeros() {
        assertThatThrownBy(() -> servicio.guardar(null, new CategoriaRequest("!!!", null, null)))
                .hasMessageContaining("El nombre debe tener letras o números.");
    }

    @Test
    void noEliminaUnaCategoriaConProductos() {
        Categoria categoria = new Categoria();
        categoria.setId(1L);
        categoria.setNombre("Componentes");
        when(categorias.findById(1L)).thenReturn(Optional.of(categoria));
        when(productos.countByCategoria_Id(1L)).thenReturn(3L);

        assertThatThrownBy(() -> servicio.eliminar(1L))
                .hasMessageContaining("No se puede eliminar \"Componentes\": tiene 3 producto(s).");
        verify(categorias, never()).delete(any());
    }
}
