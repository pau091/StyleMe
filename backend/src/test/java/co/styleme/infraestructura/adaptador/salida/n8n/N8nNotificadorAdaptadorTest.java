package co.styleme.infraestructura.adaptador.salida.n8n;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import co.styleme.dominio.modelo.comunidad.Objetivo;
import co.styleme.dominio.modelo.comunidad.Resena;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.test.json.JsonCompareMode;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

/** El aviso a n8n, con un servidor simulado en vez de n8n. */
class N8nNotificadorAdaptadorTest {

    private static final String WEBHOOK = "http://n8n.prueba/webhook/resena-nueva";
    private static final Resena RESENA = new Resena(15L, "s1", "Ana", Objetivo.tienda("bohor"), 5, "Linda tienda",
            Resena.Estado.PENDIENTE, LocalDateTime.now());

    @Test
    @DisplayName("Manda a n8n solo el id de la reseña (ningún dato personal)")
    void avisa() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer servidor = MockRestServiceServer.bindTo(builder).build();
        servidor.expect(requestTo(WEBHOOK)).andExpect(method(HttpMethod.POST))
                // STRICT: el cuerpo trae exactamente estos dos campos, nada más
                .andExpect(content().json("{\"evento\":\"resena_creada\",\"id\":15}", JsonCompareMode.STRICT))
                .andRespond(withSuccess());

        new N8nNotificadorAdaptador(WEBHOOK, builder.build()).resenaCreada(RESENA);
        servidor.verify();
    }

    @Test
    @DisplayName("Si n8n falla, no lanza error (la reseña ya está guardada)")
    void n8nFalla() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer servidor = MockRestServiceServer.bindTo(builder).build();
        servidor.expect(requestTo(WEBHOOK)).andRespond(withServerError());

        assertThatCode(() -> new N8nNotificadorAdaptador(WEBHOOK, builder.build()).resenaCreada(RESENA)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Sin dirección de webhook (sin Docker), no hace nada")
    void sinWebhook() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer servidor = MockRestServiceServer.bindTo(builder).build();
        new N8nNotificadorAdaptador("", builder.build()).resenaCreada(RESENA);
        servidor.verify(); // no se esperaba ninguna petición
    }
}
