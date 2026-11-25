package co.styleme.dominio.servicio;

import co.styleme.dominio.modelo.lia.AccionLia;
import co.styleme.dominio.modelo.lia.DatosModelo;
import co.styleme.dominio.modelo.lia.RespuestaLia;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * ANTI-ALUCINACIÓN: revisa lo que propuso la IA antes de que llegue a la persona.
 *
 * Regla de oro: Lía solo puede recomendar productos que estaban en el CONTEXTO recuperado
 * (los candidatos). Un código inventado o de otro producto se descarta, aunque la IA insista.
 *
 * También:
 *  - Valores fuera de lista (emoción, intención, sentimiento) → valor seguro por defecto.
 *  - Acciones con tienda o producto inexistente → se quitan.
 *  - En conversación general o social → sin productos ni acciones (no se ofrecen compras).
 *  - Temas de salud (ardor, alergia, dermatitis...) → expresión de preocupación, aunque la IA diga otra cosa.
 *  - Máximo 3 productos, 4 acciones y 3 sugerencias cortas.
 */
public class ValidadorRecomendaciones {

    public static final int MAX_PRODUCTOS = 3;
    public static final int MAX_ACCIONES = 4;
    public static final int MAX_SUGERENCIAS = 3;

    private static final Set<String> EMOCIONES = Set.of("felicidad", "preocupacion", "tristeza", "duda", "asombro", "neutra");
    /** Emociones que la IA puede proponer y se muestran con otra expresión del personaje. */
    private static final Map<String, String> EXPRESION = Map.of("entusiasmo", "felicidad", "comprension", "preocupacion");
    private static final Set<String> INTENCIONES = Set.of("general", "social", "catalogo", "aclaracion");
    private static final Set<String> SENTIMIENTOS = Set.of("positivo", "negativo", "neutral");
    private static final Set<String> TIPOS_ACCION = Set.of("ver_tienda", "contactar_tienda", "abrir_probador", "armar_salida", "ir_a_pasillo");
    private static final Set<String> PASILLOS = Set.of("ropa", "maquillaje", "skincare", "capilar");
    private static final Pattern SALUD = Pattern.compile(
            "ardor|arde|alergi|sangr|herida|dermatitis|infecci|hong|ca[ií]da (fuerte|del cabello|de pelo)"
                    + "|se me cae el (pelo|cabello)|ronch|quemadura|psoriasis|ros[aá]cea|acn[eé] (severo|fuerte|qu[ií]stico)",
            Pattern.CASE_INSENSITIVE);

    /**
     * @param datos      lo que escribió la IA (sin revisar)
     * @param candidatos códigos de los productos que estaban en el contexto recuperado
     * @param tiendas    slugs de las tiendas que existen
     * @param mensaje    lo que escribió la persona (para detectar temas de salud)
     * @param texto      lo que Lía le dijo a la persona
     */
    public RespuestaLia revisar(DatosModelo datos, Set<String> candidatos, Set<String> tiendas, String mensaje, String texto) {
        String sentimiento = contiene(SENTIMIENTOS, datos.sentimiento()) ? datos.sentimiento() : "neutral";
        String intencion = contiene(INTENCIONES, datos.intencion()) ? datos.intencion() : null;
        boolean deCompras = intencion == null || "catalogo".equals(intencion);

        List<String> productos = new ArrayList<>(new LinkedHashSet<>(lista(datos.productos()))).stream()
                .filter(codigo -> contiene(candidatos, codigo))   // ← la regla de oro
                .limit(MAX_PRODUCTOS)
                .toList();

        List<AccionLia> acciones = lista(datos.acciones()).stream()
                .filter(a -> a != null && contiene(TIPOS_ACCION, a.tipo()))
                .map(a -> new AccionLia(a.tipo(),
                        contiene(tiendas, a.tienda()) ? a.tienda() : null,
                        contiene(candidatos, a.producto()) ? a.producto() : null,
                        contiene(PASILLOS, a.pasillo()) ? a.pasillo() : null))
                .filter(a -> !(a.tipo().equals("ver_tienda") || a.tipo().equals("contactar_tienda")) || a.tienda() != null)
                .filter(a -> !a.tipo().equals("abrir_probador") || a.producto() != null)
                .filter(a -> !a.tipo().equals("ir_a_pasillo") || a.pasillo() != null)
                .distinct()
                .limit(MAX_ACCIONES)
                .toList();

        String emocion = datos.emocion() != null && EXPRESION.containsKey(datos.emocion()) ? EXPRESION.get(datos.emocion())
                : contiene(EMOCIONES, datos.emocion()) ? datos.emocion() : "neutra";
        double intensidad = datos.intensidad() == null ? 0.6 : Math.max(0, Math.min(1, datos.intensidad()));
        if ("negativo".equals(sentimiento)) {
            emocion = "preocupacion";
        }
        if (mensaje != null && SALUD.matcher(mensaje).find()) {
            emocion = "preocupacion";
            intensidad = Math.max(intensidad, 0.6);
        }

        List<String> sugerencias = lista(datos.sugerencias()).stream()
                .filter(s -> s != null && !s.isBlank())
                .map(s -> s.trim().length() > 60 ? s.trim().substring(0, 60) : s.trim())
                .limit(MAX_SUGERENCIAS)
                .toList();

        return new RespuestaLia(texto, emocion, intensidad, sentimiento, intencion == null ? "catalogo" : intencion,
                deCompras ? productos : List.of(), deCompras ? acciones : List.of(), sugerencias, RespuestaLia.Modo.IA);
    }

    /** La IA puede mandar valores vacíos: un null nunca "está" en la lista (y no hace caer el validador). */
    private static boolean contiene(Set<String> permitidos, String valor) {
        return valor != null && permitidos.contains(valor);
    }

    private static <T> List<T> lista(List<T> valores) {
        return valores == null ? List.of() : valores;
    }
}
