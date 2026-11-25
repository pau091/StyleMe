package co.styleme.infraestructura.adaptador.entrada.rest;

import co.styleme.aplicacion.puerto.entrada.ia.ChatearConLiaCasoUso;
import co.styleme.dominio.modelo.lia.AccionLia;
import co.styleme.dominio.modelo.lia.MensajeConversacion;
import co.styleme.dominio.modelo.lia.PreguntaLia;
import co.styleme.dominio.modelo.lia.RespuestaLia;
import co.styleme.infraestructura.adaptador.entrada.rest.dto.ErrorRespuesta;
import co.styleme.infraestructura.adaptador.entrada.rest.dto.SolicitudChat;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import tools.jackson.databind.json.JsonMapper;

/**
 * Adaptador de entrada REST de Lía: POST /api/lia/chat
 *
 * Responde POR PARTES con NDJSON (una línea JSON por evento), el mismo formato que usaba la
 * función de Netlify, así la página no cambia su forma de leer la respuesta:
 *   {"tipo":"texto","delta":"Te recomiendo..."}   ← mientras Lía "habla"
 *   {"tipo":"fin","emocion":"felicidad",...}       ← al final: emoción, productos, acciones, sugerencias
 *   {"tipo":"error","mensaje":"fallo"}             ← si algo falla (la página usa su plan B)
 */
@RestController
@RequestMapping("/api/lia")
@Tag(name = "Lía", description = "Asesora virtual con RAG: recuperar → aumentar → generar → validar")
public class LiaControlador {

    private static final Logger LOG = LoggerFactory.getLogger(LiaControlador.class);
    private static final JsonMapper JSON = JsonMapper.builder().build();
    private static final MediaType NDJSON = new MediaType("application", "x-ndjson", StandardCharsets.UTF_8);

    private final ChatearConLiaCasoUso chatearConLia;
    private final LimitadorPeticiones limitador;

    public LiaControlador(ChatearConLiaCasoUso chatearConLia, LimitadorPeticiones limitador) {
        this.chatearConLia = chatearConLia;
        this.limitador = limitador;
    }

    @PostMapping("/chat")
    @Operation(summary = "Conversar con Lía",
            description = "Respuesta en streaming (NDJSON). Lía solo recomienda productos recuperados de la base vectorial.")
    @ApiResponse(responseCode = "200", description = "Eventos NDJSON: texto, fin o error")
    @ApiResponse(responseCode = "400", description = "Mensaje vacío o demasiado largo")
    @ApiResponse(responseCode = "429", description = "Demasiados mensajes en un minuto")
    // El tipo debe ser ResponseEntity<StreamingResponseBody> (no genérico) para que Spring lo envíe por partes
    public ResponseEntity<StreamingResponseBody> chatear(@RequestBody SolicitudChat solicitud, HttpServletRequest peticion) {
        if (!limitador.permitir(peticion.getRemoteAddr())) {
            ErrorRespuesta error = ErrorRespuesta.de(429, "Too Many Requests",
                    "Demasiados mensajes seguidos. Espera un minuto.", peticion.getRequestURI());
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(salida -> salida.write(JSON.writeValueAsBytes(error)));
        }
        // Se valida ANTES de empezar a responder: si el mensaje es inválido, es un 400 normal
        PreguntaLia pregunta = new PreguntaLia(solicitud.mensaje(), aHistoria(solicitud.historia()), solicitud.sesionId());

        StreamingResponseBody cuerpo = salida -> {
            try {
                RespuestaLia respuesta = chatearConLia.conversar(pregunta, texto -> escribir(salida, Map.of("tipo", "texto", "delta", texto)));
                escribir(salida, eventoFin(respuesta));
            } catch (RuntimeException e) {
                LOG.error("Lía no pudo responder", e);
                escribir(salida, Map.of("tipo", "error", "mensaje", "fallo"));
            }
        };
        return ResponseEntity.ok().contentType(NDJSON).cacheControl(CacheControl.noStore()).body(cuerpo);
    }

    private static List<MensajeConversacion> aHistoria(List<SolicitudChat.Turno> turnos) {
        List<MensajeConversacion> historia = new ArrayList<>();
        if (turnos == null) {
            return historia;
        }
        for (SolicitudChat.Turno t : turnos) {
            if (t == null || t.texto() == null || t.texto().isBlank()) {
                continue;
            }
            if ("persona".equals(t.rol())) {
                historia.add(new MensajeConversacion(MensajeConversacion.Rol.PERSONA, t.texto()));
            } else if ("lia".equals(t.rol())) {
                historia.add(new MensajeConversacion(MensajeConversacion.Rol.LIA, t.texto()));
            }
        }
        return historia;
    }

    private static Map<String, Object> eventoFin(RespuestaLia r) {
        Map<String, Object> evento = new LinkedHashMap<>();
        evento.put("tipo", "fin");
        evento.put("emocion", r.emocion());
        evento.put("intensidad", r.intensidad());
        evento.put("sentimiento", r.sentimiento());
        evento.put("intencion", r.intencion());
        evento.put("productos", r.productos());
        evento.put("acciones", r.acciones().stream().map(LiaControlador::accion).toList());
        evento.put("sugerencias", r.sugerencias());
        evento.put("basico", r.modo() == RespuestaLia.Modo.PLAN_B); // la página muestra "modo básico"
        return evento;
    }

    /** Solo los campos que tienen valor (la página espera {"tipo":"ver_tienda","tienda":"bohor"}). */
    private static Map<String, String> accion(AccionLia a) {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("tipo", a.tipo());
        if (a.tienda() != null) {
            m.put("tienda", a.tienda());
        }
        if (a.producto() != null) {
            m.put("producto", a.producto());
        }
        if (a.pasillo() != null) {
            m.put("pasillo", a.pasillo());
        }
        return m;
    }

    /** Escribe un evento (una línea JSON) y lo envía de inmediato al navegador. */
    private static void escribir(OutputStream salida, Map<String, ?> evento) {
        try {
            salida.write(JSON.writeValueAsBytes(evento));
            salida.write('\n');
            salida.flush();
        } catch (IOException e) {
            // La persona cerró la página: no hay a quién responder
            throw new java.io.UncheckedIOException(e);
        }
    }
}
