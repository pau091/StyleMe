package co.styleme.dominio.modelo.evaluacion;

import co.styleme.dominio.modelo.lia.RespuestaLia;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Las métricas de una ejecución completa de la evaluación (lo que va al informe de calidad de IA).
 * Todos los porcentajes van de 0 a 1.
 */
public record InformeEvaluacion(String ejecucion, List<ResultadoCaso> resultados) {

    public InformeEvaluacion {
        Objects.requireNonNull(ejecucion);
        resultados = resultados == null ? List.of() : List.copyOf(resultados);
    }

    public int total() {
        return resultados.size();
    }

    public double porcentajeAprobadas() {
        return proporcion(resultados, ResultadoCaso::aprobada);
    }

    // ---------- Recuperación (solo preguntas de catálogo) ----------

    /** ¿El producto correcto aparece entre los 3 primeros? Búsqueda semántica (embeddings). */
    public double aciertoTop3Semantico() {
        return proporcion(deTipo(TipoPregunta.CATALOGO), ResultadoCaso::aciertoTop3);
    }

    /** Lo mismo con la búsqueda por palabras: sirve para demostrar cuánto mejoran los embeddings. */
    public double aciertoTop3Palabras() {
        return proporcion(deTipo(TipoPregunta.CATALOGO), ResultadoCaso::aciertoPalabras);
    }

    public double precisionMedia() {
        return deTipo(TipoPregunta.CATALOGO).stream().map(ResultadoCaso::precisionK).filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue).average().orElse(0);
    }

    public double recallMedio() {
        return deTipo(TipoPregunta.CATALOGO).stream().map(ResultadoCaso::recallK).filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue).average().orElse(0);
    }

    // ---------- Respuestas de Lía ----------

    /** Respuestas sin productos inventados ni de ejemplo. La meta es 100 %. */
    public double porcentajeSinAlucinacion() {
        return proporcion(resultados, ResultadoCaso::sinAlucinacion);
    }

    /** De las preguntas con presupuesto, cuántas lo respetaron. La meta es 100 %. */
    public double porcentajeRespetaPresupuesto() {
        return proporcion(resultados.stream().filter(r -> r.caso().presupuesto() != null).toList(),
                ResultadoCaso::respetaPresupuesto);
    }

    /** Preguntas de salud bien manejadas (preocupación + sugiere especialista). */
    public double porcentajeSalud() {
        return proporcion(deTipo(TipoPregunta.SALUD), ResultadoCaso::aprobada);
    }

    /** Preguntas fuera de tema o generales en las que NO ofreció productos. */
    public double porcentajeSinComprasFueraDeTema() {
        return proporcion(resultados.stream()
                .filter(r -> r.caso().tipo() == TipoPregunta.FUERA_TEMA || r.caso().tipo() == TipoPregunta.GENERAL)
                .toList(), ResultadoCaso::aprobada);
    }

    public long respuestasConPlanB() {
        return resultados.stream().filter(r -> r.modo() == RespuestaLia.Modo.PLAN_B).count();
    }

    public double latenciaMediaMs() {
        return resultados.stream().mapToInt(ResultadoCaso::latenciaMs).average().orElse(0);
    }

    public List<ResultadoCaso> deTipo(TipoPregunta tipo) {
        return resultados.stream().filter(r -> r.caso().tipo() == tipo).toList();
    }

    private static double proporcion(List<ResultadoCaso> lista, Predicate<ResultadoCaso> condicion) {
        return lista.isEmpty() ? 0 : (double) lista.stream().filter(condicion).count() / lista.size();
    }
}
