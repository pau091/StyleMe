package co.styleme.dominio.servicio;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Lee la respuesta del modelo MIENTRAS LLEGA (streaming) y separa sus dos partes:
 *
 *     Para piel grasa te recomiendo el Suero Detox...     ← texto visible (se muestra mientras llega)
 *     @@@                                                  ← separador, en una línea sola
 *     {"emocion":"felicidad","productos":["p035"],...}     ← datos (nunca se muestran)
 *
 * Como el separador puede llegar partido entre dos pedazos ("@" y "@@"), se retienen los
 * últimos caracteres hasta estar seguros de que no son el comienzo del separador.
 * No crea objetos (no cuesta nada) y no depende de la IA: es lógica pura y se prueba sola.
 */
public class LectorRespuestaLia {

    public static final String SEPARADOR = "@@@";
    /** Línea sola con dos o más "@" (la IA a veces escribe @@ o @@@@). */
    private static final Pattern LINEA_SEPARADORA = Pattern.compile("(?:^|\\n)@{2,}[ \\t]*\\r?(?=\\n|$)");
    private static final int RETENER = 8;

    private final StringBuilder completo = new StringBuilder();
    private int enviado = 0;

    /**
     * Agrega un pedazo recibido y devuelve el texto NUEVO que ya se puede mostrar
     * (vacío si todavía no hay nada seguro que mostrar).
     */
    public String agregar(String pedazo) {
        if (pedazo != null) {
            completo.append(pedazo);
        }
        int corte = posicionSeparador();
        int hasta = corte >= 0 ? corte : Math.max(enviado, completo.length() - RETENER);
        return avanzarHasta(hasta);
    }

    /** Al terminar: si no hubo separador, devuelve lo que quedaba retenido. */
    public String terminar() {
        return posicionSeparador() >= 0 ? "" : avanzarHasta(completo.length());
    }

    /** Todo el texto visible (sin el separador ni los datos). */
    public String textoVisible() {
        int corte = posicionSeparador();
        return (corte >= 0 ? completo.substring(0, corte) : completo.toString()).trim();
    }

    /** El JSON de datos que va después del separador, si lo hay (sin ```json ni texto alrededor). */
    public Optional<String> jsonDatos() {
        Matcher m = LINEA_SEPARADORA.matcher(completo);
        if (!m.find()) {
            return Optional.empty();
        }
        String resto = completo.substring(m.end()).replace("```json", "").replace("```", "");
        int abre = resto.indexOf('{');
        int cierra = resto.lastIndexOf('}');
        return (abre >= 0 && cierra > abre) ? Optional.of(resto.substring(abre, cierra + 1)) : Optional.empty();
    }

    private int posicionSeparador() {
        Matcher m = LINEA_SEPARADORA.matcher(completo);
        return m.find() ? m.start() : -1;
    }

    private String avanzarHasta(int hasta) {
        if (hasta <= enviado) {
            return "";
        }
        String nuevo = completo.substring(enviado, hasta);
        enviado = hasta;
        return nuevo;
    }
}
