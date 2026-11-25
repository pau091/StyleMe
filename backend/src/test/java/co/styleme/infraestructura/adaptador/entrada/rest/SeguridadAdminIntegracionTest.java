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

/** La clave de administración (D-066): /api/admin/* la exige; el resto de la API sigue abierta. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SeguridadAdminIntegracionTest {

    private final HttpClient cliente = HttpClient.newHttpClient();

    @Value("${local.server.port}")
    private int puerto;

    @Value("${styleme.admin.clave}")
    private String clave;

    @Test
    @DisplayName("Sin clave: 401 con el formato de error de siempre")
    void sinClave() throws Exception {
        var respuesta = get("/api/admin/indicadores", null);
        assertThat(respuesta.statusCode()).isEqualTo(401);
        assertThat(respuesta.body()).contains("\"estado\":401", "X-Admin-Key", "\"ruta\":\"/api/admin/indicadores\"");
    }

    @Test
    @DisplayName("Con una clave equivocada: 401 (también en POST, sin ejecutar la acción)")
    void claveEquivocada() throws Exception {
        assertThat(get("/api/admin/resenas/pendientes", "otra-clave").statusCode()).isEqualTo(401);
        var peticion = HttpRequest.newBuilder(URI.create(url("/api/admin/indexacion")))
                .header(FiltroClaveAdmin.CABECERA, "otra-clave").POST(HttpRequest.BodyPublishers.noBody()).build();
        assertThat(cliente.send(peticion, HttpResponse.BodyHandlers.ofString()).statusCode()).isEqualTo(401);
    }

    @Test
    @DisplayName("Con la clave correcta: 200")
    void conClave() throws Exception {
        assertThat(get("/api/admin/indicadores/categorias", clave).statusCode()).isEqualTo(200);
        assertThat(get("/api/admin/resenas/pendientes?limite=1", clave).statusCode()).isEqualTo(200);
    }

    @Test
    @DisplayName("Las rutas públicas no piden clave")
    void rutasPublicas() throws Exception {
        assertThat(get("/api/categorias", null).statusCode()).isEqualTo(200);
        assertThat(get("/api/resenas?tienda=bohor", null).statusCode()).isEqualTo(200);
        assertThat(get("/v3/api-docs", null).body()).contains("claveAdmin", "X-Admin-Key");
    }

    @Test
    @DisplayName("El permiso CORS del navegador (OPTIONS) pasa sin clave y autoriza la cabecera X-Admin-Key")
    void permisoCors() throws Exception {
        var peticion = HttpRequest.newBuilder(URI.create(url("/api/admin/indicadores")))
                .method("OPTIONS", HttpRequest.BodyPublishers.noBody())
                .header("Origin", "http://127.0.0.1:5500")
                .header("Access-Control-Request-Method", "GET")
                .header("Access-Control-Request-Headers", "x-admin-key")
                .build();
        var respuesta = cliente.send(peticion, HttpResponse.BodyHandlers.ofString());
        assertThat(respuesta.statusCode()).isEqualTo(200);
        assertThat(respuesta.headers().firstValue("Access-Control-Allow-Headers").orElse("")).containsIgnoringCase("x-admin-key");
    }

    private HttpResponse<String> get(String ruta, String claveEnviada) throws IOException, InterruptedException {
        var peticion = HttpRequest.newBuilder(URI.create(url(ruta))).GET();
        if (claveEnviada != null) {
            peticion.header(FiltroClaveAdmin.CABECERA, claveEnviada);
        }
        return cliente.send(peticion.build(), HttpResponse.BodyHandlers.ofString());
    }

    private String url(String ruta) {
        return "http://localhost:" + puerto + ruta;
    }
}
