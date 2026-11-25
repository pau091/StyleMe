package co.styleme.aplicacion.casodeuso.ia;

import co.styleme.aplicacion.puerto.entrada.ia.BuscarSemanticoCasoUso;
import co.styleme.aplicacion.puerto.salida.ia.AlmacenVectorialPuerto;
import co.styleme.aplicacion.puerto.salida.ia.EmbeddingPuerto;
import co.styleme.dominio.excepcion.DatoInvalidoException;
import co.styleme.dominio.modelo.ia.Embedding;
import co.styleme.dominio.modelo.ia.FiltroBusqueda;
import co.styleme.dominio.modelo.ia.ResultadoBusqueda;
import java.util.List;
import java.util.Objects;

/**
 * Caso de uso: BÚSQUEDA SEMÁNTICA y RECUPERACIÓN VECTORIAL.
 *
 *  1. La consulta de la persona se convierte en embedding (tipo "consulta").
 *  2. El almacén vectorial trae los fragmentos más parecidos (top-k), aplicando los filtros
 *     duros de la búsqueda híbrida (categoría, presupuesto, tipo) y el umbral de similitud.
 *
 * Esta misma recuperación es el primer paso del RAG de Lía (rama feature/rag-lia).
 */
public class BuscarSemanticoServicio implements BuscarSemanticoCasoUso {

    /** Una consulta muy larga cuesta más tokens y no mejora la búsqueda. */
    static final int LARGO_MAXIMO_CONSULTA = 300;

    private final EmbeddingPuerto embeddings;
    private final AlmacenVectorialPuerto almacen;

    public BuscarSemanticoServicio(EmbeddingPuerto embeddings, AlmacenVectorialPuerto almacen) {
        this.embeddings = Objects.requireNonNull(embeddings);
        this.almacen = Objects.requireNonNull(almacen);
    }

    @Override
    public List<ResultadoBusqueda> buscar(String consulta, FiltroBusqueda filtro) {
        if (consulta == null || consulta.isBlank()) {
            throw new DatoInvalidoException("Escribe qué quieres buscar");
        }
        String limpia = consulta.trim().replaceAll("\\s+", " ");
        if (limpia.length() > LARGO_MAXIMO_CONSULTA) {
            throw new DatoInvalidoException("La búsqueda no puede superar " + LARGO_MAXIMO_CONSULTA + " caracteres");
        }
        Embedding vector = embeddings.deConsulta(limpia);
        return almacen.buscarParecidos(vector, filtro == null ? FiltroBusqueda.porDefecto() : filtro);
    }
}
