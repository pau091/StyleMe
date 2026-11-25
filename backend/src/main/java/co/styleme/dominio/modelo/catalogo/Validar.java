package co.styleme.dominio.modelo.catalogo;

import co.styleme.dominio.excepcion.DatoInvalidoException;

/** Pequeñas validaciones que comparten los modelos del catálogo. */
final class Validar {

    private Validar() {
    }

    /** El texto es obligatorio: no puede ser null ni estar vacío. */
    static String obligatorio(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new DatoInvalidoException("El campo '" + campo + "' es obligatorio");
        }
        return valor.trim();
    }

    /** El texto es opcional: si viene vacío se guarda como null ("no se sabe"). */
    static String opcional(String valor) {
        return (valor == null || valor.isBlank()) ? null : valor.trim();
    }
}
