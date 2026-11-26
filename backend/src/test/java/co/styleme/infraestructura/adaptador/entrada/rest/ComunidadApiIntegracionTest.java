package co.styleme.infraestructura.adaptador.entrada.rest;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Collections;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Reseñas, favoritos, eventos e indicadores por HTTP real, como los usará la página.
 * Estas peticiones SÍ guardan en la base; por eso cada prueba usa una sesión propia y la borra al final.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ComunidadApiIntegracionTest {

    private final HttpClient cliente = HttpClient.newHttpClient();
    private final String sesion = "api-" + UUID.randomUUID().toString().substring(0, 30);

    @Value("${local.server.port}")
    private int puerto;

    @Value("${styleme.admin.clave}")
    private String claveAdmin;

    @Autowired
    private JdbcTemplate jdbc;

    @AfterEach
    void limpiar() {
        String correo = sesion + "@sesion.styleme";
        jdbc.update("DELETE FROM resena WHERE usuario_id IN (SELECT id FROM usuario WHERE correo = ?)", correo);
        jdbc.update("DELETE FROM favorito WHERE usuario_id IN (SELECT id FROM usuario WHERE correo = ?)", correo);
        jdbc.update("DELETE FROM usuario WHERE correo = ?", correo);
        jdbc.update("DELETE FROM evento WHERE sesion_id = ?", sesion);
    }

    @Test
    @DisplayName("Reseña: 201 PENDIENTE → repetida 409 → se publica con PATCH → aparece en el GET")
    void cicloDeUnaResena() throws Exception {
        String cuerpo = """
                {"sesionId":"%s","autor":"Prueba API","producto":"p049","corazones":4,"comentario":"Buena textura"}
                """.formatted(sesion);
        var creada = enviar("POST", "/api/resenas", cuerpo);
        assertThat(creada.statusCode()).isEqualTo(201);
        assertThat(creada.body()).contains("\"estado\":\"PENDIENTE\"").doesNotContain(sesion);

        assertThat(enviar("POST", "/api/resenas", cuerpo).statusCode()).isEqualTo(409);

        String id = extraer("\"id\":(\\d+)", creada.body());
        var moderada = enviar("PATCH", "/api/admin/resenas/" + id, "{\"estado\":\"PUBLICADA\"}");
        assertThat(moderada.statusCode()).isEqualTo(200);
        assertThat(moderada.body()).contains("\"estado\":\"PUBLICADA\"");

        var publicadas = enviar("GET", "/api/resenas?producto=p049", null);
        assertThat(publicadas.statusCode()).isEqualTo(200);
        assertThat(publicadas.body()).contains("\"promedio\":", "Prueba API", "Buena textura");

        assertThat(enviar("PATCH", "/api/admin/resenas/" + id, "{\"estado\":\"RECHAZADA\"}").statusCode())
                .as("una reseña ya moderada no se vuelve a moderar").isEqualTo(400);
    }

    @Test
    @DisplayName("Reseñas inválidas: 404 tienda inexistente, 400 corazones fuera de rango o tienda y producto a la vez")
    void resenasInvalidas() throws Exception {
        assertThat(enviar("POST", "/api/resenas", """
                {"sesionId":"%s","tienda":"no-existe","corazones":5}""".formatted(sesion)).statusCode()).isEqualTo(404);
        assertThat(enviar("POST", "/api/resenas", """
                {"sesionId":"%s","tienda":"bohor","corazones":7}""".formatted(sesion)).statusCode()).isEqualTo(400);
        assertThat(enviar("POST", "/api/resenas", """
                {"sesionId":"%s","tienda":"bohor","producto":"p049","corazones":5}""".formatted(sesion)).statusCode()).isEqualTo(400);
        assertThat(enviar("GET", "/api/resenas", null).statusCode()).isEqualTo(400);
        assertThat(enviar("PATCH", "/api/admin/resenas/999999999", "{\"estado\":\"PUBLICADA\"}").statusCode()).isEqualTo(404);
    }

    @Test
    @DisplayName("Favoritos: POST 204 (sin duplicar) → GET los lista → DELETE lo quita")
    void favoritos() throws Exception {
        String cuerpo = "{\"sesionId\":\"%s\",\"tienda\":\"bohor\"}".formatted(sesion);
        assertThat(enviar("POST", "/api/favoritos", cuerpo).statusCode()).isEqualTo(204);
        assertThat(enviar("POST", "/api/favoritos", cuerpo).statusCode()).isEqualTo(204);

        var lista = enviar("GET", "/api/favoritos?sesionId=" + sesion, null);
        assertThat(lista.statusCode()).isEqualTo(200);
        assertThat(lista.body()).contains("\"referencia\":\"bohor\"", "\"tipo\":\"TIENDA\"");
        assertThat(lista.body().split("\"referencia\"")).hasSize(2); // un solo favorito

        assertThat(enviar("DELETE", "/api/favoritos?sesionId=" + sesion + "&tienda=bohor", null).statusCode()).isEqualTo(204);
        assertThat(enviar("GET", "/api/favoritos?sesionId=" + sesion, null).body()).isEqualTo("[]");
        assertThat(enviar("GET", "/api/favoritos", null).statusCode()).as("falta sesionId").isEqualTo(400);
    }

    @Test
    @DisplayName("Eventos: lote válido 202; tipo desconocido o más de 50 eventos → 400 y no se guarda nada")
    void eventos() throws Exception {
        var aceptado = enviar("POST", "/api/eventos", """
                [{"tipo":"VER_TIENDA","sesionId":"%1$s","tienda":"bohor","dispositivo":"celular"},
                 {"tipo":"CLIC_WHATSAPP","sesionId":"%1$s","tienda":"bohor"}]""".formatted(sesion));
        assertThat(aceptado.statusCode()).isEqualTo(202);
        assertThat(aceptado.body()).contains("\"registrados\":2");

        assertThat(enviar("POST", "/api/eventos", """
                [{"tipo":"INVENTADO","sesionId":"%s"}]""".formatted(sesion)).statusCode()).isEqualTo(400);

        String uno = "{\"tipo\":\"ABRIR_LIA\",\"sesionId\":\"%s\"}".formatted(sesion);
        String lote = "[" + String.join(",", Collections.nCopies(51, uno)) + "]";
        assertThat(enviar("POST", "/api/eventos", lote).statusCode()).isEqualTo(400);

        assertThat(jdbc.queryForObject("SELECT count(*) FROM evento WHERE sesion_id = ?", Integer.class, sesion)).isEqualTo(2);
    }

    @Test
    @DisplayName("Indicadores: GET de todos y de uno; un código desconocido responde 400")
    void indicadores() throws Exception {
        var todos = enviar("GET", "/api/admin/indicadores", null);
        assertThat(todos.statusCode()).isEqualTo(200);
        assertThat(todos.body()).contains("\"codigo\":\"conversion_tiendas\"", "\"codigo\":\"calidad_datos\"");

        var uno = enviar("GET", "/api/admin/indicadores/categorias", null);
        assertThat(uno.statusCode()).isEqualTo(200);
        assertThat(uno.body()).contains("\"filas\":[");

        assertThat(enviar("GET", "/api/admin/indicadores/inventado", null).statusCode()).isEqualTo(400);
    }

    @Test
    @DisplayName("Portada: reseñas recientes (máximo 12) y tendencias; un periodo fuera de 1 a 90 días es 400")
    void portada() throws Exception {
        var recientes = enviar("GET", "/api/resenas/recientes?limite=100", null);
        assertThat(recientes.statusCode()).isEqualTo(200);
        assertThat(recientes.body()).startsWith("[").doesNotContain("sesion", "PENDIENTE", "RECHAZADA");
        assertThat(recientes.body().split("\"corazones\"").length - 1).isLessThanOrEqualTo(12);
        assertThat(recientes.headers().firstValue("Cache-Control")).hasValueSatisfying(v -> assertThat(v).contains("max-age"));

        var tendencias = enviar("GET", "/api/tendencias?dias=30", null);
        assertThat(tendencias.statusCode()).isEqualTo(200);
        assertThat(tendencias.body()).contains("\"dias\":30", "\"busquedas\":[", "\"tiendas\":[", "\"productos\":[");
        assertThat(enviar("GET", "/api/tendencias?dias=0", null).statusCode()).isEqualTo(400);
        assertThat(enviar("GET", "/api/tendencias?dias=91", null).statusCode()).isEqualTo(400);
    }

    private HttpResponse<String> enviar(String metodo, String ruta, String json) throws IOException, InterruptedException {
        var peticion = HttpRequest.newBuilder(URI.create("http://localhost:" + puerto + ruta))
                .header("Content-Type", "application/json")
                .method(metodo, json == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(json));
        if (ruta.startsWith("/api/admin")) {
            peticion.header(FiltroClaveAdmin.CABECERA, claveAdmin);
        }
        return cliente.send(peticion.build(), HttpResponse.BodyHandlers.ofString());
    }

    private static String extraer(String regex, String texto) {
        Matcher m = Pattern.compile(regex).matcher(texto);
        assertThat(m.find()).as("no se encontró %s en %s", regex, texto).isTrue();
        return m.group(1);
    }
}
