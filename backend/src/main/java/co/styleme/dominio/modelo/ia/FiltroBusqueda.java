package co.styleme.dominio.modelo.ia;

import co.styleme.dominio.excepcion.DatoInvalidoException;

/**
 * Filtros de la búsqueda HÍBRIDA: primero se aplican estos filtros "duros" (categoría,
 * presupuesto, tipo de fuente) y entre lo que queda se ordena por similitud de significado.
 *
 * @param categoria       slug de la categoría ("skincare"), o null
 * @param precioMaximo    presupuesto en pesos (solo aplica a productos), o null
 * @param tipo            limitar a tiendas, productos o guías, o null para todo
 * @param limite          cuántos resultados devolver (top-k)
 * @param similitudMinima umbral: por debajo de este valor un resultado no se considera relevante
 */
public record FiltroBusqueda(String categoria, Integer precioMaximo, TipoFuente tipo, int limite, double similitudMinima) {

    public static final int LIMITE_POR_DEFECTO = 5;
    public static final int LIMITE_MAXIMO = 20;
    /** Umbral por defecto. Con gemini-embedding-001, por debajo de ~0.5 los textos casi no tienen relación. */
    public static final double SIMILITUD_MINIMA_POR_DEFECTO = 0.5;

    public FiltroBusqueda {
        categoria = (categoria == null || categoria.isBlank()) ? null : categoria.trim();
        if (precioMaximo != null && precioMaximo <= 0) {
            throw new DatoInvalidoException("El precio máximo debe ser mayor que 0");
        }
        if (limite < 1 || limite > LIMITE_MAXIMO) {
            throw new DatoInvalidoException("El límite debe estar entre 1 y " + LIMITE_MAXIMO);
        }
        if (similitudMinima < 0 || similitudMinima > 1) {
            throw new DatoInvalidoException("La similitud mínima debe estar entre 0 y 1");
        }
    }

    public static FiltroBusqueda porDefecto() {
        return new FiltroBusqueda(null, null, null, LIMITE_POR_DEFECTO, SIMILITUD_MINIMA_POR_DEFECTO);
    }
}
