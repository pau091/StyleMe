package co.styleme.dominio.modelo.evaluacion;

import co.styleme.dominio.modelo.lia.RespuestaLia;
import java.util.List;

/**
 * El resultado de evaluar una pregunta.
 *
 * @param recuperados         top-5 de la búsqueda SEMÁNTICA (embeddings + pgvector)
 * @param recuperadosPalabras top-3 de la búsqueda por PALABRAS (para comparar)
 * @param recomendados        productos que Lía recomendó en su respuesta
 * @param precisionK          de los k primeros recuperados, qué fracción es relevante (null si no aplica)
 * @param recallK             de los relevantes, qué fracción aparece en el top-5 (null si no aplica)
 * @param aciertoTop3         ¿algún relevante quedó entre los 3 primeros? (búsqueda semántica)
 * @param aciertoPalabras     lo mismo con la búsqueda por palabras
 * @param sinAlucinacion      ¿todos los productos que recomendó existen y son reales?
 * @param respetaPresupuesto  ¿ninguno se pasa del presupuesto pedido?
 * @param aprobada            ¿cumple todos los criterios de su tipo?
 */
public record ResultadoCaso(
        CasoEvaluacion caso,
        List<String> recuperados,
        List<String> recuperadosPalabras,
        List<String> recomendados,
        String emocion,
        RespuestaLia.Modo modo,
        String texto,
        Double precisionK,
        Double recallK,
        boolean aciertoTop3,
        boolean aciertoPalabras,
        boolean sinAlucinacion,
        boolean respetaPresupuesto,
        boolean aprobada,
        int latenciaMs) {

    public ResultadoCaso {
        recuperados = recuperados == null ? List.of() : List.copyOf(recuperados);
        recuperadosPalabras = recuperadosPalabras == null ? List.of() : List.copyOf(recuperadosPalabras);
        recomendados = recomendados == null ? List.of() : List.copyOf(recomendados);
    }
}
