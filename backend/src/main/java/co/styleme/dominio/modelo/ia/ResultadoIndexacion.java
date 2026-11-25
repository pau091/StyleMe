package co.styleme.dominio.modelo.ia;

/**
 * Resumen de una indexación: cuántos fragmentos hubo y qué se hizo con cada uno.
 *
 * @param total      fragmentos vigentes (tiendas + productos + guías)
 * @param nuevos     no existían: se calculó su embedding
 * @param cambiados  existían pero su texto cambió: se recalculó su embedding
 * @param sinCambios su hash no cambió: no se gastó ninguna llamada a la IA
 * @param eliminados ya no existen en el catálogo: se borraron del índice
 * @param modelo     modelo de embeddings usado
 */
public record ResultadoIndexacion(int total, int nuevos, int cambiados, int sinCambios, int eliminados, String modelo) {

    /** Embeddings que se pidieron a la IA en esta indexación. */
    public int embeddingsCalculados() {
        return nuevos + cambiados;
    }
}
