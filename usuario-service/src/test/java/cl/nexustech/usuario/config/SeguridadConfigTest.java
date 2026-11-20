package cl.nexustech.usuario.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class SeguridadConfigTest {

    @Test
    void unSecretoJwtCortoImpideQueElServicioArranque() {
        SeguridadConfig config = new SeguridadConfig();
        assertThatThrownBy(() -> config.claveJwt("corto"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("al menos 32 caracteres");
        assertThat(config.claveJwt("x".repeat(32)).getAlgorithm()).isEqualTo("HmacSHA256");
    }
}
