package co.styleme.dominio.excepcion;

public class ProductoNoEncontradoException extends RecursoNoEncontradoException {

    public ProductoNoEncontradoException(String codigo) {
        super("No existe un producto con el código '" + codigo + "'");
    }
}
