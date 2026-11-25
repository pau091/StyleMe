package co.styleme.dominio.modelo.catalogo;

import co.styleme.dominio.excepcion.DatoInvalidoException;

/** Los 6 grupos de características que describen un producto de cuidado o maquillaje. */
public enum GrupoEtiqueta {

    /** Paso de la rutina: limpiador, sérum, shampoo... */
    PASO("paso"),
    /** grasa, seca, mixta, normal, sensible, todas. */
    TIPO_PIEL("tipo_piel"),
    /** liso, ondulado, rizado, afro, todos. */
    TIPO_CABELLO("tipo_cabello"),
    /** Cuero cabelludo: graso, seco, normal, sensible, todos. */
    CUERO("cuero"),
    /** hidratar, manchas, frizz... */
    OBJETIVO("objetivo"),
    /** fragancia, sulfatos, parabenos... */
    LIBRE_DE("libre_de");

    private final String codigo;

    GrupoEtiqueta(String codigo) {
        this.codigo = codigo;
    }

    /** Valor tal como se guarda en la base de datos. */
    public String codigo() {
        return codigo;
    }

    public static GrupoEtiqueta desdeCodigo(String codigo) {
        for (GrupoEtiqueta grupo : values()) {
            if (grupo.codigo.equals(codigo)) {
                return grupo;
            }
        }
        throw new DatoInvalidoException("Grupo de etiqueta desconocido: " + codigo);
    }
}
