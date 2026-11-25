package co.styleme.dominio.modelo.catalogo;

import java.util.Objects;

/**
 * Una característica de un producto: (tipo_piel, grasa), (objetivo, hidratar), (paso, serum)...
 * Un mismo modelo sirve para las 6 listas (por eso en la base es una sola tabla).
 */
public record Etiqueta(GrupoEtiqueta grupo, String valor) {

    /** Valores que significan "sirve para cualquiera". */
    private static final String TODAS = "todas";
    private static final String TODOS = "todos";

    public Etiqueta {
        Objects.requireNonNull(grupo, "La etiqueta necesita un grupo");
        valor = Validar.obligatorio(valor, "valor de la etiqueta").toLowerCase();
    }

    /** ¿Esta etiqueta es "para todos" (tipo de piel "todas", tipo de cabello "todos")? */
    public boolean esUniversal() {
        return TODAS.equals(valor) || TODOS.equals(valor);
    }
}
