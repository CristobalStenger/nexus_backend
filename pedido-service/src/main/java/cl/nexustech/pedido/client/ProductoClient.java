package cl.nexustech.pedido.client;

import cl.nexustech.pedido.dto.ProductoDto;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

// Comunicación REST con producto-service
@Component
public class ProductoClient {

    private final RestClient rest;
    private final JwtEncoder jwt;

    public ProductoClient(@Value("${nexus.producto-url}") String url, JwtEncoder jwt) {
        JdkClientHttpRequestFactory fabrica = new JdkClientHttpRequestFactory();
        // En la nube el servicio puede estar "dormido" y tardar en responder la primera vez
        fabrica.setReadTimeout(Duration.ofSeconds(30));
        this.rest = RestClient.builder().baseUrl(url).requestFactory(fabrica).build();
        this.jwt = jwt;
    }

    // Precio y stock actuales de los productos de la compra (endpoint público)
    public List<ProductoDto> buscar(Collection<Long> ids) {
        return rest.get()
                .uri(u -> u.path("/api/v1/productos").queryParam("ids", ids.toArray()).build())
                .retrieve()
                .body(new ParameterizedTypeReference<List<ProductoDto>>() {});
    }

    // Descuenta el stock de todos los productos o de ninguno. Si otra compra se llevó
    // el stock entre la consulta y este paso, producto-service responde 409 y se
    // devuelve su mensaje como motivo del rechazo.
    public Optional<String> descontarStock(Map<Long, Integer> cantidades) {
        try {
            rest.post().uri("/api/v1/productos/stock/descontar")
                    .headers(h -> h.setBearerAuth(tokenServicio()))
                    .body(items(cantidades))
                    .retrieve()
                    .toBodilessEntity();
            return Optional.empty();
        } catch (HttpClientErrorException.Conflict e) {
            Map<?, ?> error = e.getResponseBodyAs(Map.class);
            return Optional.of(error != null && error.get("detail") != null
                    ? error.get("detail").toString() : "Stock insuficiente.");
        }
    }

    // Compensación: devuelve el stock si la boleta no se pudo guardar
    public void reponerStock(Map<Long, Integer> cantidades) {
        rest.post().uri("/api/v1/productos/stock/reponer")
                .headers(h -> h.setBearerAuth(tokenServicio()))
                .body(items(cantidades))
                .retrieve()
                .toBodilessEntity();
    }

    private static List<Map<String, Object>> items(Map<Long, Integer> cantidades) {
        return cantidades.entrySet().stream()
                .map(e -> Map.<String, Object>of("productoId", e.getKey(), "cantidad", e.getValue()))
                .toList();
    }

    // Token corto con rol SERVICIO: solo pedido-service puede mover el stock,
    // un cliente con su propio token no puede llamar a esos endpoints.
    private String tokenServicio() {
        Instant ahora = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("pedido-service")
                .subject("pedido-service")
                .issuedAt(ahora)
                .expiresAt(ahora.plusSeconds(60))
                .claim("rol", "SERVICIO")
                .build();
        return jwt.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
                .getTokenValue();
    }
}
