package cl.nexustech.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.web.reactive.server.WebTestClient;

// CORS y JWT en la puerta de entrada (no necesita los otros servicios encendidos)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "FRONTEND_URL=https://nexustech.vercel.app")
class SeguridadGatewayTest {

    @LocalServerPort
    int puerto;

    private WebTestClient cliente() {
        return WebTestClient.bindToServer().baseUrl("http://localhost:" + puerto).build();
    }

    @Test
    void elFrontendPermitidoPasaElPreflight() {
        cliente().options().uri("/api/v1/boletas")
                .header("Origin", "https://nexustech.vercel.app")
                .header("Access-Control-Request-Method", "POST")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("Access-Control-Allow-Origin", "https://nexustech.vercel.app");
    }

    @Test
    void otroOrigenSeRechaza() {
        cliente().options().uri("/api/v1/boletas")
                .header("Origin", "https://sitio-ajeno.com")
                .header("Access-Control-Request-Method", "POST")
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void unTokenFalsoSeRechazaAntesDeLlegarAlServicio() {
        cliente().get().uri("/api/v1/productos")
                .header("Authorization", "Bearer token.falso.123")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void elStockNoSePuedeTocarDesdeAfuera() {
        cliente().post().uri("/api/v1/productos/stock/descontar")
                .exchange()
                .expectStatus().isUnauthorized();
    }
}
