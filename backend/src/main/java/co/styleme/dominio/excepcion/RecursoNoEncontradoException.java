package co.styleme.dominio.excepcion;

/**
 * Se buscó algo que no existe (una tienda, un producto...).
 * El adaptador REST la traduce a HTTP 404.
 */
public abstract class RecursoNoEncontradoException extends RuntimeException {

    protected RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
