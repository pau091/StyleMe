package co.styleme.infraestructura.adaptador.salida.gemini;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import co.styleme.dominio.excepcion.ServicioIaNoDisponibleException;
import co.styleme.dominio.modelo.lia.AccionLia;
import co.styleme.dominio.modelo.lia.DatosModelo;
import co.styleme.dominio.modelo.lia.MensajeConversacion;
import co.styleme.dominio.modelo.lia.Prompt;
import co.styleme.dominio.modelo.lia.RespuestaModelo;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

/** El adaptador de chat con un Gemini SIMULADO que responde por eventos (SSE). */
class GeminiChatAdaptadorTest {

    private static final String BASE = "https://gemini.prueba/v1beta";
    private static final String URL = BASE + "/models/modelo-chat:streamGenerateContent?alt=sse";

    private MockRestServiceServer servidor;
    private GeminiChatAdaptador adaptador;

    private static String evento(String texto) {
        return "data: {\"candidates\":[{\"content\":{\"parts\":[{\"text\":" + texto + "}]}}]}\n\n";
    }

    @BeforeEach
    void preparar() {
        RestClient.Builder builder = RestClient.builder().baseUrl(BASE);
        servidor = MockRestServiceServer.bindTo(builder).build();
        adaptador = new GeminiChatAdaptador(
                new PropiedadesGemini("clave", BASE, "gemini-embedding-001", "modelo-chat", Duration.ofSeconds(5)), builder.build());
    }

    @Test
    @DisplayName("Reenvía el texto por partes, oculta los datos y lee los tokens")
    void streaming() {
        String sse = evento("\"Te recomiendo \"")
                + evento("\"el Suero Detox.\\n@@\"")
                + evento("\"@\\n{\\\"emocion\\\":\\\"felicidad\\\",\\\"productos\\\":[\\\"p035\\\"],"
                        + "\\\"acciones\\\":[{\\\"tipo\\\":\\\"ver_tienda\\\",\\\"tienda\\\":\\\"mirada-de-angel\\\"}]}\"")
                + "data: {\"usageMetadata\":{\"promptTokenCount\":850,\"candidatesTokenCount\":95}}\n\n";
        servidor.expect(requestTo(URL))
                .andExpect(content().string(containsString("\"systemInstruction\"")))
                .andExpect(content().string(containsString("\"role\":\"model\"")))
                .andRespond(withSuccess(sse, MediaType.TEXT_EVENT_STREAM));

        List<String> pedazos = new ArrayList<>();
        var prompt = new Prompt("reglas", List.of(new MensajeConversacion(MensajeConversacion.Rol.LIA, "Hola")), "piel grasa");
        RespuestaModelo r = adaptador.generar(prompt, pedazos::add);

        assertThat(String.join("", pedazos).trim()).isEqualTo("Te recomiendo el Suero Detox.");
        assertThat(String.join("", pedazos)).doesNotContain("@", "emocion");
        assertThat(r.datos().emocion()).isEqualTo("felicidad");
        assertThat(r.datos().productos()).containsExactly("p035");
        assertThat(r.datos().acciones()).containsExactly(new AccionLia("ver_tienda", "mirada-de-angel", null, null));
        assertThat(r.tokensEntrada()).isEqualTo(850);
        assertThat(r.tokensSalida()).isEqualTo(95);
        servidor.verify();
    }

    @Test
    @DisplayName("Si Gemini responde con error, lanza ServicioIaNoDisponibleException (y entra el plan B)")
    void error() {
        servidor.expect(requestTo(URL)).andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS));
        assertThatThrownBy(() -> adaptador.generar(new Prompt("r", List.of(), "hola"), t -> { }))
                .isInstanceOf(ServicioIaNoDisponibleException.class);
    }

    @Test
    @DisplayName("Un JSON roto de la IA no rompe nada: los datos quedan vacíos")
    void jsonRoto() {
        assertThat(GeminiChatAdaptador.leerDatos("{esto no es json")).isEqualTo(DatosModelo.vacios());
        assertThat(GeminiChatAdaptador.leerDatos("{\"productos\":[\"p1\", 7, null],\"intensidad\":\"alta\"}").productos())
                .containsExactly("p1");
    }
}
