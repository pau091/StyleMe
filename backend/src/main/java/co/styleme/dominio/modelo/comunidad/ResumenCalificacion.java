package co.styleme.dominio.modelo.comunidad;

/**
 * Promedio de corazones y cantidad de reseñas PUBLICADAS de una tienda o un producto.
 *
 * @param promedio 0 si todavía no tiene reseñas publicadas
 */
public record ResumenCalificacion(double promedio, int cantidad) {

    public boolean tieneResenas() {
        return cantidad > 0;
    }
}
