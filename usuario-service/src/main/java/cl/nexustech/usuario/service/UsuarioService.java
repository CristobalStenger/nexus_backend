package cl.nexustech.usuario.service;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import cl.nexustech.usuario.dto.UsuarioRequest;
import cl.nexustech.usuario.model.Rol;
import cl.nexustech.usuario.model.Usuario;
import cl.nexustech.usuario.repository.UsuarioRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;

    public List<Usuario> listar() {
        return usuarios.findAll(Sort.by("id"));
    }

    public Usuario obtener(Long id) {
        return usuarios.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "El usuario no existe."));
    }

    // Registro desde la tienda: siempre queda como Cliente
    @Transactional
    public Usuario registrar(UsuarioRequest datos) {
        return guardar(null, datos, Rol.Cliente);
    }

    @Transactional
    public Usuario crear(UsuarioRequest datos) {
        return guardar(null, datos, tipoObligatorio(datos));
    }

    // idSesion: quien hace el cambio. Nadie cambia su propio rol, así siempre queda
    // al menos el administrador que está editando.
    @Transactional
    public Usuario actualizar(Long id, UsuarioRequest datos, Long idSesion) {
        Rol tipo = tipoObligatorio(datos);
        if (id.equals(idSesion) && tipo != obtener(id).getTipo())
            throw new ResponseStatusException(BAD_REQUEST, "No puedes cambiar tu propio rol.");
        return guardar(id, datos, tipo);
    }

    // Perfil propio: cualquier usuario con sesión, sin poder cambiar su rol
    @Transactional
    public Usuario actualizarPerfil(Long idSesion, UsuarioRequest datos) {
        return guardar(idSesion, datos, obtener(idSesion).getTipo());
    }

    @Transactional
    public void eliminar(Long id, Long idSesion) {
        if (id.equals(idSesion))
            throw new ResponseStatusException(CONFLICT, "No puedes eliminar tu propia cuenta mientras la estás usando.");
        usuarios.delete(obtener(id));
    }

    // id null: usuario nuevo
    private Usuario guardar(Long id, UsuarioRequest datos, Rol tipo) {
        Usuario usuario = id == null ? new Usuario() : obtener(id);
        String run = datos.run().trim().toUpperCase();
        String correo = datos.correo().trim().toLowerCase();

        if (!runEsValido(run))
            throw new ResponseStatusException(BAD_REQUEST, "El RUN no es válido, revisa el dígito verificador.");
        usuarios.findByRun(run).filter(otro -> !otro.getId().equals(id)).ifPresent(otro -> {
            throw new ResponseStatusException(CONFLICT, "Ya existe un usuario con ese RUN.");
        });
        usuarios.findByCorreo(correo).filter(otro -> !otro.getId().equals(id)).ifPresent(otro -> {
            throw new ResponseStatusException(CONFLICT, "Ya existe un usuario con ese correo.");
        });

        // Al editar, una contraseña vacía mantiene la anterior
        if (datos.clave() != null && !datos.clave().isEmpty())
            usuario.setClave(encoder.encode(datos.clave()));
        else if (id == null)
            throw new ResponseStatusException(BAD_REQUEST, "La contraseña es obligatoria.");

        usuario.setRun(run);
        usuario.setNombre(datos.nombre().trim());
        usuario.setApellidos(datos.apellidos().trim());
        usuario.setCorreo(correo);
        usuario.setFechaNacimiento(datos.fechaNacimiento());
        usuario.setTipo(tipo);
        usuario.setRegion(datos.region().trim());
        usuario.setComuna(datos.comuna().trim());
        usuario.setDireccion(datos.direccion().trim());
        return usuarios.save(usuario);
    }

    private static Rol tipoObligatorio(UsuarioRequest datos) {
        if (datos.tipo() == null)
            throw new ResponseStatusException(BAD_REQUEST, "Selecciona el tipo de usuario.");
        return datos.tipo();
    }

    // RUN chileno con dígito verificador (módulo 11). Ejemplo válido: 21456789K
    static boolean runEsValido(String run) {
        if (!run.matches("^[0-9]{6,8}[0-9K]$")) return false;
        String cuerpo = run.substring(0, run.length() - 1);
        int suma = 0;
        int multiplo = 2;
        // Se recorre el cuerpo de derecha a izquierda multiplicando por 2..7
        for (int i = cuerpo.length() - 1; i >= 0; i--) {
            suma += (cuerpo.charAt(i) - '0') * multiplo;
            multiplo = multiplo == 7 ? 2 : multiplo + 1;
        }
        int resto = 11 - (suma % 11);
        char esperado = resto == 11 ? '0' : resto == 10 ? 'K' : (char) ('0' + resto);
        return run.charAt(run.length() - 1) == esperado;
    }
}
