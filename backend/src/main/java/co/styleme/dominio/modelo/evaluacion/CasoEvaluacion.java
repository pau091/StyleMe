package co.styleme.dominio.modelo.evaluacion;

import co.styleme.dominio.excepcion.DatoInvalidoException;
import java.util.Objects;
import java.util.Set;

/**
 * Una pregunta del conjunto de evaluación con su respuesta esperada (verdad de referencia).
 *
 * @param relevantes  códigos de los productos REALES que responden bien a la pregunta (solo en CATALOGO)
 * @param presupuesto máximo en pesos pedido en la pregunta, o null
 */
public record CasoEvaluacion(String id, String pregunta, TipoPregunta tipo, Set<String> relevantes, Integer presupuesto) {

    public CasoEvaluacion {
        Objects.requireNonNull(tipo, "El caso necesita un tipo");
        if (id == null || id.isBlank() || pregunta == null || pregunta.isBlank()) {
            throw new DatoInvalidoException("El caso necesita id y pregunta");
        }
        relevantes = relevantes == null ? Set.of() : Set.copyOf(relevantes);
        if (tipo == TipoPregunta.CATALOGO && relevantes.isEmpty()) {
            throw new DatoInvalidoException("El caso " + id + " es de catálogo y necesita productos relevantes");
        }
    }
}
