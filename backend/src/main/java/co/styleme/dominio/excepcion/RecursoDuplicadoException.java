package co.styleme.dominio.excepcion;

/**
 * Se intentó crear algo que ya existe (por ejemplo, reseñar dos veces la misma tienda).
 * El adaptador REST la traduce a HTTP 409 (conflicto).
 */
public class RecursoDuplicadoException extends RuntimeException {

    public RecursoDuplicadoException(String mensaje) {
        super(mensaje);
    }
}
