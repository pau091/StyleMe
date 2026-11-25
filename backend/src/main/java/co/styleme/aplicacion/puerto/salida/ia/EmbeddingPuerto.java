package co.styleme.aplicacion.puerto.salida.ia;

import co.styleme.dominio.modelo.ia.Embedding;
import java.util.List;

/**
 * Puerto de salida: convertir textos en embeddings.
 * La aplicación no sabe si detrás está Gemini u otro proveedor: eso lo decide el adaptador.
 */
public interface EmbeddingPuerto {

    /** Embeddings de textos que se van a GUARDAR (fragmentos de conocimiento), en el mismo orden. */
    List<Embedding> deDocumentos(List<String> textos);

    /** Embedding de una PREGUNTA o búsqueda de una persona. */
    Embedding deConsulta(String texto);

    /** Nombre del modelo, para saber con qué se calculó cada embedding guardado. */
    String modelo();
}
