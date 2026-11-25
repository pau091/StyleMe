package co.styleme.infraestructura.adaptador.salida.gemini;

import co.styleme.aplicacion.puerto.salida.ia.ModeloLenguajePuerto;
import co.styleme.dominio.excepcion.ServicioIaNoDisponibleException;
import co.styleme.dominio.modelo.lia.AccionLia;
import co.styleme.dominio.modelo.lia.DatosModelo;
import co.styleme.dominio.modelo.lia.MensajeConversacion;
import co.styleme.dominio.modelo.lia.Prompt;
import co.styleme.dominio.modelo.lia.RespuestaModelo;
import co.styleme.dominio.servicio.LectorRespuestaLia;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Adaptador de salida: implementa ModeloLenguajePuerto con Gemini, POR PARTES (streaming).
 *
 * Usa streamGenerateContent con alt=sse: Gemini manda la respuesta en eventos "data: {...}"
 * a medida que la escribe. Cada pedazo pasa por LectorRespuestaLia (dominio), que deja salir
 * solo el texto visible y guarda los datos que vienen después del separador @@@.
 */
@Component
public class GeminiChatAdaptador implements ModeloLenguajePuerto {

    private static final JsonMapper JSON = JsonMapper.builder().build();
    private static final double TEMPERATURA = 0.6;
    private static final int MAX_TOKENS_SALIDA = 1200;
    /** Si Gemini está saturado (429 o 5xx) y todavía no escribió nada, se reintenta tras esta espera. */
    private static final long[] ESPERAS_REINTENTO_MS = {2_000, 5_000};

    private final PropiedadesGemini propiedades;
    private final RestClient cliente;

    public GeminiChatAdaptador(PropiedadesGemini propiedades, @Qualifier("clienteGemini") RestClient cliente) {
        this.propiedades = propiedades;
        this.cliente = cliente;
    }

    @Override
    public RespuestaModelo generar(Prompt prompt, Consumer<String> alRecibirTexto) {
        if (!propiedades.tieneClave()) {
            throw new ServicioIaNoDisponibleException("Falta configurar GEMINI_API_KEY en el archivo .env");
        }
        boolean[] escribio = {false};
        Consumer<String> contar = texto -> {
            escribio[0] = true;
            alRecibirTexto.accept(texto);
        };
        for (int intento = 0; ; intento++) {
            try {
                return intentar(prompt, contar);
            } catch (GeminiSaturadoException e) {
                // Solo se reintenta si todavía no se mostró nada (si no, el texto quedaría repetido)
                if (escribio[0] || intento >= ESPERAS_REINTENTO_MS.length) {
                    throw new ServicioIaNoDisponibleException(e.getMessage(), e);
                }
                esperar(ESPERAS_REINTENTO_MS[intento]);
            }
        }
    }

    private RespuestaModelo intentar(Prompt prompt, Consumer<String> alRecibirTexto) {
        try {
            return cliente.post()
                    .uri("/models/{modelo}:streamGenerateContent?alt=sse", propiedades.modeloChat())
                    .header("x-goog-api-key", propiedades.apiKey())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(cuerpo(prompt))
                    .exchange((peticion, respuesta) -> {
                        int estado = respuesta.getStatusCode().value();
                        if (estado == 429 || estado >= 500) {
                            throw new GeminiSaturadoException("Gemini respondió " + estado);
                        }
                        if (respuesta.getStatusCode().isError()) {
                            throw new ServicioIaNoDisponibleException("Gemini respondió " + estado);
                        }
                        return leerEventos(respuesta.getBody(), alRecibirTexto);
                    });
        } catch (RestClientException | UncheckedIOException e) {
            throw new ServicioIaNoDisponibleException("No se pudo obtener la respuesta de Gemini: " + e.getMessage(), e);
        }
    }

    private static void esperar(long milisegundos) {
        try {
            Thread.sleep(milisegundos);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ServicioIaNoDisponibleException("Se interrumpió la espera para reintentar");
        }
    }

    /** Gemini está saturado o sin cupo por un momento (429 o 5xx): vale la pena reintentar. */
    private static final class GeminiSaturadoException extends RuntimeException {
        GeminiSaturadoException(String mensaje) {
            super(mensaje);
        }
    }

    /** systemInstruction = reglas + contexto; contents = historial corto + mensaje actual. */
    private static Map<String, Object> cuerpo(Prompt prompt) {
        List<Map<String, Object>> contenidos = new ArrayList<>();
        for (MensajeConversacion m : prompt.historia()) {
            contenidos.add(Map.of("role", m.esDeLaPersona() ? "user" : "model", "parts", List.of(Map.of("text", m.texto()))));
        }
        contenidos.add(Map.of("role", "user", "parts", List.of(Map.of("text", prompt.mensaje()))));
        return Map.of(
                "systemInstruction", Map.of("parts", List.of(Map.of("text", prompt.instrucciones()))),
                "contents", contenidos,
                "generationConfig", Map.of("temperature", TEMPERATURA, "maxOutputTokens", MAX_TOKENS_SALIDA));
    }

    private static RespuestaModelo leerEventos(java.io.InputStream cuerpo, Consumer<String> alRecibirTexto) {
        var lector = new LectorRespuestaLia();
        int tokensEntrada = 0;
        int tokensSalida = 0;
        try (var lineas = new BufferedReader(new InputStreamReader(cuerpo, StandardCharsets.UTF_8))) {
            String linea;
            while ((linea = lineas.readLine()) != null) {
                if (!linea.startsWith("data:")) {
                    continue;
                }
                JsonNode evento = leerJson(linea.substring(5).trim());
                if (evento == null) {
                    continue;
                }
                for (JsonNode parte : evento.path("candidates").path(0).path("content").path("parts")) {
                    // Algunos modelos mandan su "razonamiento" interno: nunca se muestra
                    if (parte.path("thought").asBoolean(false)) {
                        continue;
                    }
                    JsonNode texto = parte.get("text");
                    if (texto != null && texto.isString()) {
                        enviar(alRecibirTexto, lector.agregar(texto.asString()));
                    }
                }
                JsonNode uso = evento.get("usageMetadata");
                if (uso != null) {
                    tokensEntrada = uso.path("promptTokenCount").asInt(tokensEntrada);
                    tokensSalida = uso.path("candidatesTokenCount").asInt(tokensSalida);
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        enviar(alRecibirTexto, lector.terminar());
        DatosModelo datos = lector.jsonDatos().map(GeminiChatAdaptador::leerDatos).orElse(DatosModelo.vacios());
        return new RespuestaModelo(lector.textoVisible(), datos, tokensEntrada, tokensSalida);
    }

    private static void enviar(Consumer<String> alRecibirTexto, String texto) {
        if (!texto.isEmpty()) {
            alRecibirTexto.accept(texto);
        }
    }

    /** El JSON que escribió la IA → DatosModelo. Lo que no tenga la forma esperada queda vacío (y luego el validador decide). */
    static DatosModelo leerDatos(String json) {
        JsonNode n = leerJson(json);
        if (n == null || !n.isObject()) {
            return DatosModelo.vacios();
        }
        List<AccionLia> acciones = new ArrayList<>();
        for (JsonNode a : n.path("acciones")) {
            if (a.isObject()) {
                acciones.add(new AccionLia(texto(a, "tipo"), texto(a, "tienda"), texto(a, "producto"), texto(a, "pasillo")));
            }
        }
        JsonNode intensidad = n.get("intensidad");
        return new DatosModelo(texto(n, "sentimiento"), texto(n, "intencion"), texto(n, "emocion"),
                intensidad != null && intensidad.isNumber() ? intensidad.asDouble() : null,
                textos(n.path("productos")), acciones, textos(n.path("sugerencias")));
    }

    private static JsonNode leerJson(String json) {
        try {
            return JSON.readTree(json);
        } catch (JacksonException e) {
            return null;
        }
    }

    private static String texto(JsonNode nodo, String campo) {
        JsonNode valor = nodo.get(campo);
        return valor != null && valor.isString() ? valor.asString() : null;
    }

    private static List<String> textos(JsonNode lista) {
        List<String> valores = new ArrayList<>();
        for (JsonNode v : lista) {
            if (v.isString()) {
                valores.add(v.asString());
            }
        }
        return valores;
    }
}
