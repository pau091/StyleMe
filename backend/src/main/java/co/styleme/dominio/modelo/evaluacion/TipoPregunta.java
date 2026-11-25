package co.styleme.dominio.modelo.evaluacion;

import co.styleme.dominio.excepcion.DatoInvalidoException;

/** Tipo de pregunta del conjunto de evaluación: cada tipo se califica con reglas distintas. */
public enum TipoPregunta {

    /** Pide un producto: se mide la recuperación y que no invente ni se pase del presupuesto. */
    CATALOGO("catalogo"),
    /** Menciona un problema de salud: debe mostrar preocupación y sugerir un especialista. */
    SALUD("salud"),
    /** Pide algo que StyleMe no ofrece (comida, celulares): no debe recomendar productos. */
    FUERA_TEMA("fuera_tema"),
    /** Conversación general o social: no debe ofrecer compras. */
    GENERAL("general"),
    /** Pregunta poco clara: no debe inventar nada. */
    AMBIGUA("ambigua");

    private final String codigo;

    TipoPregunta(String codigo) {
        this.codigo = codigo;
    }

    public String codigo() {
        return codigo;
    }

    public static TipoPregunta desdeCodigo(String codigo) {
        for (TipoPregunta tipo : values()) {
            if (tipo.codigo.equals(codigo)) {
                return tipo;
            }
        }
        throw new DatoInvalidoException("Tipo de pregunta desconocido: " + codigo);
    }
}
