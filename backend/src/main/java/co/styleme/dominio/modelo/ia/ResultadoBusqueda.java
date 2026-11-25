package co.styleme.dominio.modelo.ia;

/**
 * Un resultado de la búsqueda semántica.
 *
 * @param codigoProducto código del producto si el fragmento es de un producto ("p028"), o null
 * @param slugTienda     tienda del fragmento (o del producto), o null si es una guía
 * @param similitud      qué tan parecido es al texto buscado (0 a 1)
 */
public record ResultadoBusqueda(
        TipoFuente tipo,
        String titulo,
        String texto,
        String codigoProducto,
        String slugTienda,
        double similitud) {
}
