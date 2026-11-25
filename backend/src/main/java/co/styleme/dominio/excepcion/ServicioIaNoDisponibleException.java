package co.styleme.dominio.excepcion;

/**
 * El servicio de inteligencia artificial no respondió (sin clave, sin internet, sin cupo...).
 * El adaptador REST la traduce a HTTP 503; Lía usa su plan B.
 */
public class ServicioIaNoDisponibleException extends RuntimeException {

    public ServicioIaNoDisponibleException(String mensaje) {
        super(mensaje);
    }

    public ServicioIaNoDisponibleException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
