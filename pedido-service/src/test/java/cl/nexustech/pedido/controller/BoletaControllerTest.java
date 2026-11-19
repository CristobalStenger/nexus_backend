package cl.nexustech.pedido.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cl.nexustech.pedido.config.SeguridadConfig;
import cl.nexustech.pedido.service.BoletaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

// Quién puede comprar y quién puede ver las órdenes
@WebMvcTest(BoletaController.class)
@Import(SeguridadConfig.class)
class BoletaControllerTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    BoletaService servicio;

    private static SimpleGrantedAuthority rol(String rol) {
        return new SimpleGrantedAuthority("ROLE_" + rol);
    }

    @Test
    void sinSesionNoSeVenLasOrdenes() throws Exception {
        mvc.perform(get("/api/v1/boletas")).andExpect(status().isUnauthorized());
    }

    @Test
    void elClienteNoVeLasOrdenesDelPanel() throws Exception {
        mvc.perform(get("/api/v1/boletas").with(jwt().authorities(rol("Cliente")))).andExpect(status().isForbidden());
    }

    @Test
    void elVendedorYElAdministradorVenLasOrdenes() throws Exception {
        mvc.perform(get("/api/v1/boletas").with(jwt().authorities(rol("Vendedor")))).andExpect(status().isOk());
        mvc.perform(get("/api/v1/boletas").with(jwt().authorities(rol("Administrador")))).andExpect(status().isOk());
    }

    @Test
    void cualquieraPuedeComprarPeroLosDatosSeValidan() throws Exception {
        mvc.perform(post("/api/v1/boletas").contentType(MediaType.APPLICATION_JSON).content("{\"items\":[]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.items").value("El carrito está vacío."));
    }
}
