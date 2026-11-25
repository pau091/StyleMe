package co.styleme.dominio.modelo.catalogo;

import co.styleme.dominio.excepcion.DatoInvalidoException;

/**
 * Criterios para buscar productos en el catálogo. Todos son opcionales:
 * un filtro vacío trae todos los productos activos.
 *
 * @param categoria    slug de la categoría ("skincare")
 * @param tienda       slug de la tienda ("pandora-makeup")
 * @param precioMaximo presupuesto en pesos
 * @param texto        palabras a buscar en el nombre o la marca
 */
public record FiltroProductos(String categoria, String tienda, Integer precioMaximo, String texto) {

    public FiltroProductos {
        categoria = Validar.opcional(categoria);
        tienda = Validar.opcional(tienda);
        texto = Validar.opcional(texto);
        if (precioMaximo != null && precioMaximo <= 0) {
            throw new DatoInvalidoException("El precio máximo debe ser mayor que 0");
        }
    }

    public static FiltroProductos todos() {
        return new FiltroProductos(null, null, null, null);
    }

    /** Copia del filtro limitada a una tienda. */
    public FiltroProductos deLaTienda(String slugTienda) {
        return new FiltroProductos(categoria, slugTienda, precioMaximo, texto);
    }
}
