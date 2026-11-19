package cl.nexustech.usuario.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cl.nexustech.usuario.dto.UsuarioRequest;
import cl.nexustech.usuario.model.Rol;
import cl.nexustech.usuario.model.Usuario;
import cl.nexustech.usuario.repository.UsuarioRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    UsuarioRepository usuarios;

    // BCrypt real con costo bajo para que la prueba sea rápida
    PasswordEncoder encoder = new BCryptPasswordEncoder(4);

    UsuarioService servicio;

    @BeforeEach
    void preparar() {
        servicio = new UsuarioService(usuarios, encoder);
    }

    private static UsuarioRequest datos(String run, String correo, String clave, Rol tipo) {
        return new UsuarioRequest(run, "Pedro", "Soto", correo, clave, null, tipo,
                "Región Metropolitana", "Providencia", "Los Crisantemos 789");
    }

    private static Usuario existente(Long id, Rol tipo) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setTipo(tipo);
        usuario.setClave("hash-anterior");
        return usuario;
    }

    @Test
    void validaElDigitoVerificadorDelRun() {
        assertThat(UsuarioService.runEsValido("21456789K")).isTrue();
        assertThat(UsuarioService.runEsValido("190110222")).isTrue();
        assertThat(UsuarioService.runEsValido("123456785")).isTrue();
        assertThat(UsuarioService.runEsValido("123456789")).isFalse();
        assertThat(UsuarioService.runEsValido("12.345.678-5")).isFalse();
    }

    @Test
    void elRegistroCreaSiempreUnClienteConLaClaveCifrada() {
        when(usuarios.findByRun("21456789K")).thenReturn(Optional.empty());
        when(usuarios.findByCorreo("pedro@gmail.com")).thenReturn(Optional.empty());
        when(usuarios.save(any())).thenAnswer(i -> i.getArgument(0));

        // Aunque intente registrarse como administrador
        Usuario creado = servicio.registrar(datos("21456789k", " Pedro@Gmail.com ", "clave1", Rol.Administrador));

        assertThat(creado.getTipo()).isEqualTo(Rol.Cliente);
        assertThat(creado.getRun()).isEqualTo("21456789K");
        assertThat(creado.getCorreo()).isEqualTo("pedro@gmail.com");
        assertThat(creado.getClave()).isNotEqualTo("clave1");
        assertThat(encoder.matches("clave1", creado.getClave())).isTrue();
    }

    @Test
    void rechazaUnRunConDigitoVerificadorIncorrecto() {
        assertThatThrownBy(() -> servicio.registrar(datos("123456789", "pedro@gmail.com", "clave1", null)))
                .hasMessageContaining("El RUN no es válido");
    }

    @Test
    void rechazaUnCorreoRepetido() {
        when(usuarios.findByRun("21456789K")).thenReturn(Optional.empty());
        when(usuarios.findByCorreo("cliente@gmail.com")).thenReturn(Optional.of(existente(3L, Rol.Cliente)));

        assertThatThrownBy(() -> servicio.registrar(datos("21456789K", "cliente@gmail.com", "clave1", null)))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Ya existe un usuario con ese correo.");
    }

    @Test
    void alEditarSinClaveSeMantieneLaAnterior() {
        Usuario usuario = existente(3L, Rol.Cliente);
        when(usuarios.findById(3L)).thenReturn(Optional.of(usuario));
        when(usuarios.findByRun("21456789K")).thenReturn(Optional.of(usuario));
        when(usuarios.findByCorreo("cliente@gmail.com")).thenReturn(Optional.of(usuario));
        when(usuarios.save(any())).thenAnswer(i -> i.getArgument(0));

        Usuario editado = servicio.actualizar(3L, datos("21456789K", "cliente@gmail.com", null, Rol.Vendedor), 1L);

        assertThat(editado.getClave()).isEqualTo("hash-anterior");
        assertThat(editado.getTipo()).isEqualTo(Rol.Vendedor);
    }

    @Test
    void nadieCambiaSuPropioRol() {
        when(usuarios.findById(1L)).thenReturn(Optional.of(existente(1L, Rol.Administrador)));

        assertThatThrownBy(() -> servicio.actualizar(1L, datos("190110222", "admin@duoc.cl", null, Rol.Cliente), 1L))
                .hasMessageContaining("No puedes cambiar tu propio rol.");
        verify(usuarios, never()).save(any());
    }

    @Test
    void nadieEliminaSuPropiaCuenta() {
        assertThatThrownBy(() -> servicio.eliminar(1L, 1L))
                .hasMessageContaining("No puedes eliminar tu propia cuenta");
        verify(usuarios, never()).delete(any());
    }

    @Test
    void elAdministradorDebeElegirElTipo() {
        assertThatThrownBy(() -> servicio.crear(datos("21456789K", "pedro@gmail.com", "clave1", null)))
                .hasMessageContaining("Selecciona el tipo de usuario.");
    }
}
