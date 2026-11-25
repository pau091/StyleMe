package co.styleme.infraestructura.adaptador.entrada.rest.dto;

/**
 * Un resultado de la búsqueda semántica en el JSON de la API.
 *
 * @param similitud qué tan parecido es a lo buscado, de 0 a 1 (redondeado a 3 decimales)
 */
public record ResultadoBusquedaRespuesta(
        String tipo,
        String titulo,
        String texto,
        String codigoProducto,
        String slugTienda,
        double similitud) {
}
