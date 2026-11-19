package cl.nexustech.producto.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cl.nexustech.producto.config.SeguridadConfig;
import cl.nexustech.producto.model.Categoria;
import cl.nexustech.producto.model.Producto;
import cl.nexustech.producto.service.ProductoService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

// Permisos por rol de los endpoints de productos
@WebMvcTest(ProductoController.class)
@Import(SeguridadConfig.class)
class ProductoControllerTest {

    private static final String PRODUCTO = """
            {"codigo":"MOU001","nombre":"Mouse","tipo":"Mouse","precio":10000,"stock":5,"categoriaId":1}""";
    private static final String STOCK = """
            [{"productoId":1,"cantidad":1}]""";

    @Autowired
    MockMvc mvc;

    @MockitoBean
    ProductoService servicio;

    private static Producto producto() {
        Categoria categoria = new Categoria();
        categoria.setId(1L);
        categoria.setNombre("Componentes");
        Producto producto = new Producto();
        producto.setId(1L);
        producto.setNombre("AMD Ryzen 5 7600X");
        producto.setPrecio(1000);
        producto.setDescuento(10);
        producto.setCategoria(categoria);
        return producto;
    }

    private static MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder peticion, String cuerpo) {
        return peticion.contentType(MediaType.APPLICATION_JSON).content(cuerpo);
    }

    private static SimpleGrantedAuthority rol(String rol) {
        return new SimpleGrantedAuthority("ROLE_" + rol);
    }

    @Test
    void elCatalogoEsPublicoYTraeLaCategoriaComoTexto() throws Exception {
        when(servicio.listar(null)).thenReturn(List.of(producto()));

        mvc.perform(get("/api/v1/productos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].categoria").value("Componentes"))
                .andExpect(jsonPath("$[0].categoriaId").value(1))
                .andExpect(jsonPath("$[0].precioFinal").value(900));
    }

    @Test
    void crearSinTokenDevuelve401() throws Exception {
        mvc.perform(json(post("/api/v1/productos"), PRODUCTO)).andExpect(status().isUnauthorized());
    }

    @Test
    void elVendedorSoloPuedeVer() throws Exception {
        mvc.perform(json(post("/api/v1/productos"), PRODUCTO).with(jwt().authorities(rol("Vendedor"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void elAdministradorCreaProductos() throws Exception {
        when(servicio.guardar(eq(null), any())).thenReturn(producto());

        mvc.perform(json(post("/api/v1/productos"), PRODUCTO).with(jwt().authorities(rol("Administrador"))))
                .andExpect(status().isCreated());
    }

    @Test
    void losErroresDeValidacionVienenPorCampo() throws Exception {
        mvc.perform(json(post("/api/v1/productos"), "{\"precio\":-1}").with(jwt().authorities(rol("Administrador"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.precio").value("El precio no puede ser negativo."))
                .andExpect(jsonPath("$.errores.codigo").value("El código es obligatorio."));
    }

    @Test
    void unClienteNoPuedeDescontarStock() throws Exception {
        mvc.perform(json(post("/api/v1/productos/stock/descontar"), STOCK).with(jwt().authorities(rol("Cliente"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void pedidoServiceDescuentaStockConSuTokenDeServicio() throws Exception {
        mvc.perform(json(post("/api/v1/productos/stock/descontar"), STOCK).with(jwt().authorities(rol("SERVICIO"))))
                .andExpect(status().isNoContent());
    }
}
