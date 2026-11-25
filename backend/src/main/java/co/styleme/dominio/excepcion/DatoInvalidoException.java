package co.styleme.dominio.excepcion;

/**
 * Un dato no cumple una regla del negocio (por ejemplo, un precio negativo).
 * El adaptador REST la traduce a HTTP 400.
 */
public class DatoInvalidoException extends RuntimeException {

    public DatoInvalidoException(String mensaje) {
        super(mensaje);
    }
}
