package co.styleme.dominio.servicio;

import co.styleme.dominio.modelo.catalogo.Producto;
import co.styleme.dominio.modelo.evaluacion.CasoEvaluacion;
import co.styleme.dominio.modelo.evaluacion.ResultadoCaso;
import co.styleme.dominio.modelo.lia.RespuestaLia;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Califica la respuesta de Lía a una pregunta de evaluación. Lógica pura: se prueba sin IA.
 *
 * Métricas de RECUPERACIÓN (preguntas de catálogo):
 *   precisión@k = relevantes entre los k primeros / k, con k = mín(3, cantidad de relevantes)
 *                 (si una pregunta solo tiene 1 producto correcto, se mide en el top-1; así la
 *                 métrica no castiga que el catálogo tenga pocas opciones)
 *   recall@5    = relevantes que aparecen en el top-5 / total de relevantes
 *   acierto@3   = ¿al menos un relevante entre los 3 primeros?
 *
 * Criterios de APROBACIÓN por tipo:
 *   CATALOGO    sin alucinación + respeta presupuesto + acierto@3
 *   SALUD       expresión de preocupación + sugiere especialista + sin alucinación
 *   FUERA_TEMA  y GENERAL: no recomienda productos
 *   AMBIGUA     sin alucinación + respeta presupuesto
 */
public class CalificadorEvaluacion {

    private static final int TOP_PRECISION = 3;
    private static final int TOP_RECALL = 5;
    private static final Pattern SUGIERE_ESPECIALISTA = Pattern.compile("dermat|m[eé]dic|especialista|doctor", Pattern.CASE_INSENSITIVE);

    /**
     * @param recuperados         top-5 semántico (códigos)
     * @param recuperadosPalabras top-3 por palabras (códigos)
     * @param catalogoReal        productos reales y activos, por código (para detectar alucinaciones)
     */
    public ResultadoCaso calificar(CasoEvaluacion caso, List<String> recuperados, List<String> recuperadosPalabras,
            RespuestaLia respuesta, Map<String, Producto> catalogoReal, int latenciaMs) {
        Set<String> relevantes = caso.relevantes();
        Double precision = null;
        Double recall = null;
        boolean acierto = false;
        boolean aciertoPalabras = false;
        if (!relevantes.isEmpty()) {
            int k = Math.min(TOP_PRECISION, relevantes.size());
            precision = (double) contarRelevantes(recuperados, relevantes, k) / k;
            recall = (double) contarRelevantes(recuperados, relevantes, TOP_RECALL) / relevantes.size();
            acierto = contarRelevantes(recuperados, relevantes, TOP_PRECISION) > 0;
            aciertoPalabras = contarRelevantes(recuperadosPalabras, relevantes, TOP_PRECISION) > 0;
        }

        List<String> recomendados = respuesta.productos();
        boolean sinAlucinacion = catalogoReal.keySet().containsAll(recomendados);
        boolean respetaPresupuesto = caso.presupuesto() == null || recomendados.stream()
                .map(catalogoReal::get)
                .allMatch(p -> p != null && p.cuestaMaximo(caso.presupuesto()));

        boolean aprobada = switch (caso.tipo()) {
            case CATALOGO -> sinAlucinacion && respetaPresupuesto && acierto;
            case SALUD -> "preocupacion".equals(respuesta.emocion())
                    && respuesta.texto() != null && SUGIERE_ESPECIALISTA.matcher(respuesta.texto()).find()
                    && sinAlucinacion;
            case FUERA_TEMA, GENERAL -> recomendados.isEmpty();
            case AMBIGUA -> sinAlucinacion && respetaPresupuesto;
        };

        return new ResultadoCaso(caso, recuperados, recuperadosPalabras, recomendados, respuesta.emocion(), respuesta.modo(),
                respuesta.texto(), precision, recall, acierto, aciertoPalabras, sinAlucinacion, respetaPresupuesto, aprobada, latenciaMs);
    }

    private static int contarRelevantes(List<String> lista, Set<String> relevantes, int top) {
        return (int) lista.stream().limit(top).filter(relevantes::contains).count();
    }
}
