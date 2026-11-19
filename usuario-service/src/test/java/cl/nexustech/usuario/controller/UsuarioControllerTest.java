package cl.nexustech.usuario.controller;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cl.nexustech.usuario.config.SeguridadConfig;
import cl.nexustech.usuario.model.Rol;
import cl.nexustech.usuario.model.Usuario;
import cl.nexustech.usuario.service.UsuarioService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

// La gestión de usuarios es solo del administrador; el perfil propio, de cualquier rol
@WebMvcTest(UsuarioController.class)
@Import(SeguridadConfig.class)
class UsuarioControllerTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    UsuarioService servicio;

    private static Usuario cliente() {
        Usuario usuario = new Usuario();
        usuario.setId(3L);
        usuario.setNombre("Pedro");
        usuario.setTipo(Rol.Cliente);
        usuario.setClave("$2a$10$hash");
        return usuario;
    }

    @Test
    void elVendedorNoVeLosUsuarios() throws Exception {
        mvc.perform(get("/api/v1/usuarios").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_Vendedor"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void elAdministradorVeLosUsuariosSinSusClaves() throws Exception {
        when(servicio.listar()).thenReturn(List.of(cliente()));

        mvc.perform(get("/api/v1/usuarios").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_Administrador"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Pedro"))
                .andExpect(jsonPath("$[0].clave").doesNotExist());
    }

    @Test
    void elPerfilPropioPideSesion() throws Exception {
        mvc.perform(get("/api/v1/usuarios/me")).andExpect(status().isUnauthorized());
    }

    @Test
    void cualquierRolVeSuPerfil() throws Exception {
        when(servicio.obtener(3L)).thenReturn(cliente());

        mvc.perform(get("/api/v1/usuarios/me").with(jwt().jwt(j -> j.subject("3"))
                        .authorities(new SimpleGrantedAuthority("ROLE_Cliente"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("Cliente"));
    }
}
