package co.styleme.dominio.modelo.ia;

import co.styleme.dominio.excepcion.DatoInvalidoException;

/** De dónde sale un fragmento de conocimiento. */
public enum TipoFuente {

    TIENDA("tienda"),
    PRODUCTO("producto"),
    /** Guías escritas para StyleMe: rutina de skincare, cuidado capilar, estilo. */
    GUIA("guia");

    private final String codigo;

    TipoFuente(String codigo) {
        this.codigo = codigo;
    }

    /** Valor tal como se guarda en la base de datos. */
    public String codigo() {
        return codigo;
    }

    public static TipoFuente desdeCodigo(String codigo) {
        for (TipoFuente tipo : values()) {
            if (tipo.codigo.equalsIgnoreCase(codigo)) {
                return tipo;
            }
        }
        throw new DatoInvalidoException("Tipo de fuente desconocido: " + codigo);
    }
}
