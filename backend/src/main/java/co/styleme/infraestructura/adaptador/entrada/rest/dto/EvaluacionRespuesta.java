package co.styleme.infraestructura.adaptador.entrada.rest.dto;

import java.util.List;

/** Resultado de una evaluación de la IA en el JSON de la API (porcentajes de 0 a 100). */
public record EvaluacionRespuesta(
        String ejecucion,
        int preguntas,
        double aprobadas,
        Recuperacion recuperacion,
        Respuestas respuestas,
        List<Caso> casos) {

    /** Búsqueda semántica (embeddings) frente a búsqueda por palabras. */
    public record Recuperacion(double aciertoTop3Semantico, double aciertoTop3Palabras, double precisionMedia, double recallMedio) {
    }

    public record Respuestas(double sinAlucinacion, double respetaPresupuesto, double salud, double sinComprasFueraDeTema,
                             long respuestasPlanB, long latenciaMediaMs) {
    }

    public record Caso(String id, String tipo, String pregunta, boolean aprobada, List<String> recomendados, Double precisionK) {
    }
}
