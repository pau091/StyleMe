package co.styleme.dominio.excepcion;

public class ResenaNoEncontradaException extends RecursoNoEncontradoException {

    public ResenaNoEncontradaException(long id) {
        super("No existe una reseña con el id " + id);
    }
}
