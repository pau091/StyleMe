package co.styleme.dominio.modelo.catalogo;

import co.styleme.dominio.excepcion.DatoInvalidoException;

/** Dónde se pone una prenda en el probador virtual. Solo aplica a ropa. */
public enum TipoPrenda {

    /** Blusa, camisa, chaqueta. */
    SUPERIOR("superior"),
    /** Pantalón, falda. */
    INFERIOR("inferior"),
    /** Vestido, enterizo. */
    COMPLETA("completa");

    private final String codigo;

    TipoPrenda(String codigo) {
        this.codigo = codigo;
    }

    /** Valor tal como se guarda en la base de datos. */
    public String codigo() {
        return codigo;
    }

    public static TipoPrenda desdeCodigo(String codigo) {
        if (codigo == null) {
            return null;
        }
        for (TipoPrenda tipo : values()) {
            if (tipo.codigo.equals(codigo)) {
                return tipo;
            }
        }
        throw new DatoInvalidoException("Tipo de prenda desconocido: " + codigo);
    }
}
