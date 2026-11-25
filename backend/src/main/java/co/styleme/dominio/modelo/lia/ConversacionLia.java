package co.styleme.dominio.modelo.lia;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * RESUMEN de una conversación con Lía (D-053): NO guarda lo que escribe la persona.
 * Cada intercambio actualiza los contadores y promedios; sirve para el informe de datos
 * (temas, modo IA vs. plan B, calidad del contexto, tokens gastados).
 *
 * Reglas:
 *  - tema: "catalogo" si en algún momento se habló de compras; si no, la intención más reciente.
 *  - modo: IA o PLAN_B; si hubo de las dos, MIXTO.
 *  - similitud y latencia: promedio de todos los intercambios.
 */
public record ConversacionLia(
        Long id,
        String sesionId,
        String tema,
        String categoria,
        String sentimientoFinal,
        int cantidadMensajes,
        String modo,
        double similitudPromedio,
        int latenciaPromedioMs,
        int tokensEntrada,
        int tokensSalida,
        LocalDateTime inicio,
        LocalDateTime fin) {

    public ConversacionLia {
        Objects.requireNonNull(sesionId, "La conversación necesita un identificador de sesión");
    }

    /** Una conversación que empieza con este intercambio. */
    public static ConversacionLia nueva(String sesionId, Intercambio intercambio, LocalDateTime ahora) {
        return new ConversacionLia(null, sesionId, intercambio.intencion(), intercambio.categoria(), intercambio.sentimiento(),
                1, intercambio.modo().name(), intercambio.similitud(), intercambio.latenciaMs(),
                intercambio.tokensEntrada(), intercambio.tokensSalida(), ahora, ahora);
    }

    /** La misma conversación con un intercambio más (el modelo es inmutable: devuelve una copia). */
    public ConversacionLia con(Intercambio intercambio, LocalDateTime ahora) {
        int n = cantidadMensajes;
        String nuevoTema = "catalogo".equals(tema) ? tema : intercambio.intencion();
        String nuevaCategoria = intercambio.categoria() != null ? intercambio.categoria() : categoria;
        String nuevoModo = modo.equals(intercambio.modo().name()) ? modo : "MIXTO";
        return new ConversacionLia(id, sesionId, nuevoTema, nuevaCategoria, intercambio.sentimiento(), n + 1, nuevoModo,
                (similitudPromedio * n + intercambio.similitud()) / (n + 1),
                (int) Math.round(((double) latenciaPromedioMs * n + intercambio.latenciaMs()) / (n + 1)),
                tokensEntrada + intercambio.tokensEntrada(), tokensSalida + intercambio.tokensSalida(), inicio, ahora);
    }

    /**
     * Un intercambio (pregunta + respuesta) resumido, sin el texto.
     *
     * @param categoria pasillo del que se habló (de los productos recomendados), o null
     * @param similitud similitud promedio del contexto recuperado (calidad de la recuperación)
     */
    public record Intercambio(String intencion, String categoria, String sentimiento, RespuestaLia.Modo modo,
                              double similitud, int latenciaMs, int tokensEntrada, int tokensSalida) {
    }
}
