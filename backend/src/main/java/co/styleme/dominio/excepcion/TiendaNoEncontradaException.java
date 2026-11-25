package co.styleme.dominio.excepcion;

public class TiendaNoEncontradaException extends RecursoNoEncontradoException {

    public TiendaNoEncontradaException(String slug) {
        super("No existe una tienda con el identificador '" + slug + "'");
    }
}
