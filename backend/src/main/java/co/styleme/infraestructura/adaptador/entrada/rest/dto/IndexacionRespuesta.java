package co.styleme.infraestructura.adaptador.entrada.rest.dto;

/** Resumen de una indexación en el JSON de la API. */
public record IndexacionRespuesta(
        int total,
        int nuevos,
        int cambiados,
        int sinCambios,
        int eliminados,
        int embeddingsCalculados,
        String modelo,
        long duracionMs) {
}
