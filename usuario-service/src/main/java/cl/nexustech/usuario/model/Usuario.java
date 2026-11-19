package cl.nexustech.usuario.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Sin puntos ni guion, con dígito verificador (ej. 21456789K)
    private String run;

    private String nombre;

    private String apellidos;

    private String correo;

    // Hash BCrypt: nunca sale en las respuestas de la API
    @JsonIgnore
    private String clave;

    private LocalDate fechaNacimiento;

    @Enumerated(EnumType.STRING)
    private Rol tipo;

    private String region;

    private String comuna;

    private String direccion;
}
