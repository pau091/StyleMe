package co.styleme.infraestructura.adaptador.entrada.rest;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Prueba de punta a punta: arranca el backend en un puerto al azar y le hace peticiones HTTP
 * reales, como lo hará la página. Recorre todas las capas: REST → caso de uso → JPA → PostgreSQL.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CatalogoApiIntegracionTest {

    private final HttpClient cliente = HttpClient.newHttpClient();

    @Value("${local.server.port}")
    private int puerto;

    private HttpResponse<String> get(String ruta) throws IOException, InterruptedException {
        var peticion = HttpRequest.newBuilder(URI.create("http://localhost:" + puerto + ruta)).GET().build();
        return cliente.send(peticion, HttpResponse.BodyHandlers.ofString());
    }

    @Test
    @DisplayName("GET /api/categorias devuelve las 4 categorías")
    void categorias() throws Exception {
        var respuesta = get("/api/categorias");
        assertThat(respuesta.statusCode()).isEqualTo(200);
        assertThat(respuesta.body()).contains("\"slug\":\"ropa\"", "\"slug\":\"capilar\"");
    }

    @Test
    @DisplayName("GET /api/tiendas/bohor devuelve la tienda con su enlace de WhatsApp")
    void tienda() throws Exception {
        var respuesta = get("/api/tiendas/bohor");
        assertThat(respuesta.statusCode()).isEqualTo(200);
        assertThat(respuesta.body()).contains("\"nombre\":\"BOHOR\"", "https://wa.me/573012951439", "\"apareceEnMapa\":true");
    }

    @Test
    @DisplayName("Una tienda que no existe responde 404 con un mensaje claro")
    void tiendaInexistente() throws Exception {
        var respuesta = get("/api/tiendas/no-existe");
        assertThat(respuesta.statusCode()).isEqualTo(404);
        assertThat(respuesta.body()).contains("no-existe", "\"estado\":404");
    }

    @Test
    @DisplayName("GET /api/productos con filtros devuelve productos con etiquetas agrupadas")
    void productosFiltrados() throws Exception {
        var respuesta = get("/api/productos?categoria=skincare&q=serum");
        assertThat(respuesta.statusCode()).isEqualTo(200);
        assertThat(respuesta.body()).contains("p028", "\"tipo_piel\":[");
    }

    @Test
    @DisplayName("Un precio máximo que no es número responde 400")
    void precioInvalido() throws Exception {
        assertThat(get("/api/productos?precioMaximo=mucho").statusCode()).isEqualTo(400);
        assertThat(get("/api/productos?precioMaximo=-5").statusCode()).isEqualTo(400);
    }

    @Test
    @DisplayName("Swagger publica la especificación OpenAPI con los endpoints del catálogo")
    void swagger() throws Exception {
        var especificacion = get("/v3/api-docs");
        assertThat(especificacion.statusCode()).isEqualTo(200);
        assertThat(especificacion.body()).contains("/api/tiendas", "/api/productos", "/api/categorias");
        assertThat(get("/swagger-ui/index.html").statusCode()).isEqualTo(200);
    }

    @Test
    @DisplayName("La búsqueda semántica valida la consulta antes de llamar a la IA (400)")
    void busquedaInvalida() throws Exception {
        assertThat(get("/api/busqueda").statusCode()).isEqualTo(400);
        assertThat(get("/api/busqueda?q=hola&limite=50").statusCode()).isEqualTo(400);
        assertThat(get("/api/busqueda?q=hola&tipo=planeta").statusCode()).isEqualTo(400);
    }

    @Test
    @DisplayName("Los productos de una tienda inexistente responden 404")
    void productosDeTiendaInexistente() throws Exception {
        assertThat(get("/api/tiendas/no-existe/productos").statusCode()).isEqualTo(404);
    }
}
