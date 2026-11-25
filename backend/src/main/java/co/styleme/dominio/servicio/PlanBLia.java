package co.styleme.dominio.servicio;

import co.styleme.dominio.modelo.catalogo.Producto;
import co.styleme.dominio.modelo.lia.AccionLia;
import co.styleme.dominio.modelo.lia.RespuestaLia;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * PLAN B: si el modelo de lenguaje no responde (sin internet, sin cupo, sin clave), Lía sigue
 * funcionando con REGLAS: detecta la categoría por palabras clave y muestra productos reales
 * del catálogo. La respuesta se marca como PLAN_B (la página avisa "modo básico").
 */
public class PlanBLia {

    /** Palabras clave → categoría (en orden: la primera que coincide gana). */
    private static final Map<String, String> PALABRAS = new LinkedHashMap<>();

    static {
        for (String p : List.of("shampoo", "champu", "acondicionador", "cabello", "pelo", "rizo", "frizz", "capilar", "cuero cabelludo")) {
            PALABRAS.put(p, "capilar");
        }
        for (String p : List.of("serum", "piel", "crema", "hidratante", "protector", "bloqueador", "limpiador", "tonico", "manchas", "acne", "skincare")) {
            PALABRAS.put(p, "skincare");
        }
        for (String p : List.of("maquillaje", "labial", "base", "pestanina", "mascara", "sombra", "rubor", "corrector", "polvo", "delineador")) {
            PALABRAS.put(p, "maquillaje");
        }
        for (String p : List.of("ropa", "vestido", "blusa", "pantalon", "falda", "jean", "camisa", "chaqueta", "outfit", "boda", "enterizo")) {
            PALABRAS.put(p, "ropa");
        }
    }

    private static final Map<String, String> NOMBRE = Map.of(
            "ropa", "ropa y moda", "maquillaje", "maquillaje", "skincare", "cuidado de la piel", "capilar", "cuidado capilar");

    /** ¿De qué categoría habla el mensaje? */
    public Optional<String> detectarCategoria(String mensaje) {
        String limpio = sinTildes(mensaje);
        return PALABRAS.entrySet().stream()
                .filter(e -> limpio.contains(e.getKey()))
                .map(Map.Entry::getValue)
                .findFirst();
    }

    /**
     * @param categoria categoría detectada (o vacía)
     * @param productos productos reales de esa categoría (ya filtrados por presupuesto)
     */
    public RespuestaLia responder(Optional<String> categoria, List<Producto> productos) {
        if (categoria.isEmpty()) {
            return new RespuestaLia(
                    "En este momento estoy en modo básico, pero puedo ayudarte a encontrar ropa, maquillaje y cuidado "
                            + "para tu piel o tu cabello. ¿Qué estás buscando?",
                    "neutra", 0.5, "neutral", "aclaracion", List.of(), List.of(),
                    List.of("Busco maquillaje", "Rutina para mi piel", "Algo para mi cabello"), RespuestaLia.Modo.PLAN_B);
        }
        String nombre = NOMBRE.get(categoria.get());
        List<Producto> elegidos = productos.stream().filter(p -> !p.esEjemplo()).limit(ValidadorRecomendaciones.MAX_PRODUCTOS).toList();
        List<AccionLia> acciones = new ArrayList<>();
        acciones.add(new AccionLia("ir_a_pasillo", null, null, categoria.get()));
        if (!elegidos.isEmpty()) {
            acciones.add(new AccionLia("ver_tienda", elegidos.get(0).tienda().slug(), null, null));
        }
        String texto = elegidos.isEmpty()
                ? "Estoy en modo básico y no encontré productos de " + nombre + " con esas condiciones. Puedes ver todo el catálogo de " + nombre + "."
                : "Estoy en modo básico, pero aquí tienes algunas opciones de " + nombre + " de tiendas de Bucaramanga. Confirma el precio con la tienda antes de ir.";
        return new RespuestaLia(texto, "neutra", 0.5, "neutral", "catalogo",
                elegidos.stream().map(Producto::codigo).toList(), acciones,
                List.of("Ver otra opción", "Armar la salida de compras"), RespuestaLia.Modo.PLAN_B);
    }

    private static String sinTildes(String texto) {
        return texto == null ? "" : Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}", "").toLowerCase();
    }
}
