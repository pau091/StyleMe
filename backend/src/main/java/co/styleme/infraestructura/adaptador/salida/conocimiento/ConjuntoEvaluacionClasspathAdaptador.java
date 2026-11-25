package co.styleme.infraestructura.adaptador.salida.conocimiento;

import co.styleme.aplicacion.puerto.salida.ia.ConjuntoEvaluacionPuerto;
import co.styleme.dominio.modelo.evaluacion.CasoEvaluacion;
import co.styleme.dominio.modelo.evaluacion.TipoPregunta;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** Adaptador de salida: lee el conjunto de evaluación desde src/main/resources/evaluacion/preguntas.json. */
@Component
public class ConjuntoEvaluacionClasspathAdaptador implements ConjuntoEvaluacionPuerto {

    private static final String ARCHIVO = "evaluacion/preguntas.json";
    private static final JsonMapper JSON = JsonMapper.builder().build();

    @Override
    public List<CasoEvaluacion> casos() {
        try (InputStream entrada = new ClassPathResource(ARCHIVO).getInputStream()) {
            JsonNode raiz = JSON.readTree(entrada);
            List<CasoEvaluacion> casos = new ArrayList<>();
            for (JsonNode p : raiz.path("preguntas")) {
                Set<String> relevantes = new LinkedHashSet<>();
                p.path("relevantes").forEach(r -> relevantes.add(r.asString()));
                JsonNode presupuesto = p.get("presupuesto");
                casos.add(new CasoEvaluacion(
                        p.path("id").asString(),
                        p.path("pregunta").asString(),
                        TipoPregunta.desdeCodigo(p.path("tipo").asString()),
                        relevantes,
                        presupuesto != null && presupuesto.isNumber() ? presupuesto.asInt() : null));
            }
            return casos;
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo leer el conjunto de evaluación " + ARCHIVO, e);
        }
    }
}
